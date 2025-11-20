package es.unizar.eina.notepad.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.util.Log;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.widget.CheckBox;
import android.widget.LinearLayout;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.unizar.eina.notepad.database.Quad;
import es.unizar.eina.notepad.database.QuadRepository;
import es.unizar.eina.notepad.database.ReservaQuad;
import es.unizar.eina.notepad.database.ReservaQuadRepository;

import es.unizar.eina.notepad.R;

/**
 * Activity para crear o editar una reserva.
 * <p>
 * Permite al usuario:
 * - Seleccionar fechas de recogida y devolución mediante DatePickers
 * - Ingresar nombre del cliente
 * - Seleccionar uno o más quads disponibles mediante checkboxes
 * - Especificar número de cascos para cada quad
 * - Ver el precio total calculado automáticamente
 * </p>
 * <p>
 * Valida:
 * - Que el nombre del cliente no esté vacío
 * - Que la fecha de devolución no sea anterior a la de recogida
 * - Que se haya seleccionado al menos un quad
 * - Que se haya especificado el número de cascos para cada quad seleccionado
 * </p>
 * <p>
 * El precio total se calcula como la suma de: (precio_quad × días) para
 * cada quad seleccionado.
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see es.unizar.eina.notepad.database.Reserva
 * @see Quad
 * @see ReservaQuad
 */
public class ReservaEdit extends AppCompatActivity {

    public static final String RESERVA_QUAD_ID = "quad_id";
    public static final String RESERVA_CUSTOMER = "customer";
    public static final String RESERVA_START = "start_date";
    public static final String RESERVA_END = "end_date";
    public static final String RESERVA_ID = "id";

    // Nuevas claves (modelo Reserva)
    public static final String RESERVA_FECHA_RECOGIDA = "fecha_recogida";
    public static final String RESERVA_FECHA_DEVOLUCION = "fecha_devolucion";
    public static final String RESERVA_PRECIO_TOTAL = "precio_total";
    public static final String RESERVA_NUM_CASCOS = "num_cascos";
    public static final String RESERVA_TELEFONO = "telefono";
    public static final String RESERVA_NOM_CLIENTE = "nom_cliente";

    private EditText mCustomerText;
    private EditText mStartText;
    private EditText mEndText;
    private TextView mPrecioDisplay;
    private LinearLayout mQuadsContainer;

    private long mStartMillis = -1L;
    private long mEndMillis = -1L;

    private Integer mRowId;
    private List<Quad> mAllQuads = new ArrayList<>();
    private Map<Integer, CheckBox> mQuadCheckboxes = new HashMap<>();
    private Map<Integer, EditText> mCascosFields = new HashMap<>();
    // Selections passed by intent or loaded from DB to pre-check boxes (quadId -> numCascos)
    private Map<Integer, Integer> mInitialSelections = new HashMap<>();

    Button mSaveButton;
    private QuadViewModel mQuadViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservaedit);

        mCustomerText = findViewById(R.id.customer);
        mStartText = findViewById(R.id.start_date);
        mEndText = findViewById(R.id.end_date);
        mPrecioDisplay = findViewById(R.id.price_total);
        mQuadsContainer = findViewById(R.id.quads_container);

        // Abrir DatePicker al pulsar los campos (son non-editable en layout)
        mStartText.setOnClickListener(v -> showDatePicker(true));
        mEndText.setOnClickListener(v -> showDatePicker(false));

        // Cargar datos de fechas y campos de texto desde extras (si es edición)
        // Se hace antes de crear los checkboxes para que mRowId esté disponible
        populateTextFields();

        // Cargar lista de quads disponibles y crear checkboxes
        mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);
        mQuadViewModel.getAllQuads().observe(this, quads -> {
            if (quads != null) {
                mAllQuads = quads;
                populateQuadCheckboxes();
                // Intentar poblar las checkboxes basadas en la reserva (si estamos en modo edición)
                populateCheckboxes();
            }
        });

        mSaveButton = findViewById(R.id.button_save);
        mSaveButton.setOnClickListener(view -> {
            Intent replyIntent = new Intent();
            if (TextUtils.isEmpty(mCustomerText.getText())) {
                setResult(RESULT_CANCELED, replyIntent);
                Toast.makeText(getApplicationContext(), R.string.empty_not_saved, Toast.LENGTH_LONG).show();
                return;
            }
            String customer = mCustomerText.getText().toString();

            // Ensure we have millis for start/end. If not, try parsing from the text shown.
            if (mStartMillis <= 0) mStartMillis = parseDateString(mStartText.getText().toString());
            if (mEndMillis <= 0) mEndMillis = parseDateString(mEndText.getText().toString());

            // Validate dates
            if (mStartMillis > 0 && mEndMillis > 0 && mEndMillis < mStartMillis) {
                Toast.makeText(getApplicationContext(), "La fecha de devolución no puede ser anterior a la de recogida", Toast.LENGTH_LONG).show();
                return;
            }

            // Recoger quads seleccionados (checkboxes marcadas) y validar cascos
            List<Integer> selectedQuadIds = new ArrayList<>();
            Map<Integer, Integer> quadCascos = new HashMap<>();
            for (Map.Entry<Integer, CheckBox> entry : mQuadCheckboxes.entrySet()) {
                if (entry.getValue().isChecked()) {
                    int quadId = entry.getKey();
                    selectedQuadIds.add(quadId);
                    EditText cascosField = mCascosFields.get(quadId);
                    if (cascosField == null || TextUtils.isEmpty(cascosField.getText())) {
                        Toast.makeText(getApplicationContext(), "Indica el número de cascos para cada quad seleccionado", Toast.LENGTH_LONG).show();
                        return;
                    }
                    int cascos;
                    try {
                        cascos = Integer.parseInt(cascosField.getText().toString());
                        if (cascos < 0) throw new NumberFormatException();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Número de cascos inválido para el quad " + quadId, Toast.LENGTH_LONG).show();
                        return;
                    }
                    quadCascos.put(quadId, cascos);
                }
            }

            if (selectedQuadIds.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Debes seleccionar al menos un quad", Toast.LENGTH_LONG).show();
                return;
            }

            // Calcular precio total: suma de (precio por día * días) de todos los quads
            double precioTotal = calculateTotalPrice(selectedQuadIds);

            // Compatibilidad: claves antiguas (usar formatted strings)
            String startStr = formatDateForDisplay(mStartMillis);
            String endStr = formatDateForDisplay(mEndMillis);
            replyIntent.putExtra(ReservaEdit.RESERVA_CUSTOMER, customer);
            replyIntent.putExtra(ReservaEdit.RESERVA_START, startStr);
            replyIntent.putExtra(ReservaEdit.RESERVA_END, endStr);

            // Nuevas claves: pasar los millis para fechas
            replyIntent.putExtra(ReservaEdit.RESERVA_NOM_CLIENTE, customer);
            replyIntent.putExtra(ReservaEdit.RESERVA_FECHA_RECOGIDA, mStartMillis);
            replyIntent.putExtra(ReservaEdit.RESERVA_FECHA_DEVOLUCION, mEndMillis);
            replyIntent.putExtra(ReservaEdit.RESERVA_PRECIO_TOTAL, precioTotal);
            replyIntent.putExtra(ReservaEdit.RESERVA_TELEFONO, 0);
            
            // Pass selected quads and cascos as comma-separated strings for compatibility
            StringBuilder quadIdsBuilder = new StringBuilder();
            StringBuilder cascosBuilder = new StringBuilder();
            for (int i = 0; i < selectedQuadIds.size(); i++) {
                int qid = selectedQuadIds.get(i);
                quadIdsBuilder.append(qid);
                cascosBuilder.append(quadCascos.get(qid));
                if (i < selectedQuadIds.size() - 1) {
                    quadIdsBuilder.append(",");
                    cascosBuilder.append(",");
                }
            }
            replyIntent.putExtra("SELECTED_QUAD_IDS", quadIdsBuilder.toString());
            replyIntent.putExtra("SELECTED_CASCOS", cascosBuilder.toString());

            if (mRowId != null) {
                replyIntent.putExtra(ReservaEdit.RESERVA_ID, mRowId.intValue());
            }
            setResult(RESULT_OK, replyIntent);
            finish();
        });

    }

    // Aplica mInitialSelections a los checkboxes existentes
    private void applyInitialSelections() {
        Log.d("ReservaEdit", "applyInitialSelections llamado. mInitialSelections.size = " + mInitialSelections.size() + ", mQuadCheckboxes.size = " + mQuadCheckboxes.size());
        for (Map.Entry<Integer, Integer> e : mInitialSelections.entrySet()) {
            CheckBox cb = mQuadCheckboxes.get(e.getKey());
            EditText et = mCascosFields.get(e.getKey());
            if (cb != null) {
                cb.setChecked(true);
                Log.d("ReservaEdit", "Marcando checkbox para quadId " + e.getKey());
            } else {
                Log.w("ReservaEdit", "No se encontró checkbox para quadId " + e.getKey());
            }
            if (et != null && e.getValue() != null) {
                et.setText(String.valueOf(e.getValue()));
            }
        }
        updatePriceDisplay();
    }

    private void showDatePicker(boolean isStart) {
        Calendar c = Calendar.getInstance();
        if (isStart && mStartMillis > 0) c.setTimeInMillis(mStartMillis);
        if (!isStart && mEndMillis > 0) c.setTimeInMillis(mEndMillis);
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(year, month, dayOfMonth, 0, 0, 0);
            sel.set(Calendar.MILLISECOND, 0);
            long millis = sel.getTimeInMillis();
            if (isStart) {
                mStartMillis = millis;
                mStartText.setText(formatDateForDisplay(millis));
                updatePriceDisplay();
            } else {
                mEndMillis = millis;
                mEndText.setText(formatDateForDisplay(millis));
                updatePriceDisplay();
            }
        }, y, m, d);
        dp.show();
    }

    private long parseDateString(String s) {
        if (s == null) return -1L;
        s = s.trim();
        if (s.isEmpty()) return -1L;
        // Try numeric millis
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ignored) {}
        // Try known patterns
        String[] patterns = new String[] {"yyyy-MM-dd", "dd/MM/yyyy"};
        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.getDefault());
                Date dt = sdf.parse(s);
                if (dt != null) return dt.getTime();
            } catch (ParseException ignored) {}
        }
        // Try locale short
        try {
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            Date dt = df.parse(s);
            if (dt != null) return dt.getTime();
        } catch (ParseException ignored) {}
        return -1L;
    }

    private String formatDateForDisplay(long millis) {
        if (millis <= 0) return "";
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        return df.format(new Date(millis));
    }

    private void populateQuadCheckboxes() {
        Log.d("ReservaEdit", "populateQuadCheckboxes llamado. mAllQuads.size = " + mAllQuads.size());
        mQuadsContainer.removeAllViews();
        mQuadCheckboxes.clear();
        mCascosFields.clear();

        for (Quad quad : mAllQuads) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(8, 8, 8, 8);

            CheckBox cb = new CheckBox(this);
            cb.setText(String.format("%s (%s) - %d €/día", quad.getMatricula(), quad.getTipo(), quad.getPrecio()));
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> updatePriceDisplay());
            
            LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            cb.setLayoutParams(cbParams);

            EditText cascosField = new EditText(this);
            cascosField.setHint("Cascos");
            cascosField.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            cascosField.setMinWidth(100);
            LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            etParams.setMarginStart(16);
            cascosField.setLayoutParams(etParams);
            cascosField.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
                @Override public void afterTextChanged(Editable s) { if(cb.isChecked()) updatePriceDisplay(); }
            });

            row.addView(cb);
            row.addView(cascosField);
            mQuadsContainer.addView(row);

            mQuadCheckboxes.put(quad.getId(), cb);
            mCascosFields.put(quad.getId(), cascosField);
        }

        // Después de crear todos los checkboxes, aplicar selecciones iniciales
        applyInitialSelections();
    }

    private void updatePriceDisplay() {
        if (mStartMillis <= 0 || mEndMillis <= 0) {
            mPrecioDisplay.setText("Precio total: 0.00 €");
            return;
        }
        List<Integer> selectedIds = new ArrayList<>();
        for (Map.Entry<Integer, CheckBox> entry : mQuadCheckboxes.entrySet()) {
            if (entry.getValue().isChecked()) {
                selectedIds.add(entry.getKey());
            }
        }
        double total = calculateTotalPrice(selectedIds);
        mPrecioDisplay.setText(String.format(Locale.getDefault(), "Precio total: %.2f €", total));
    }

    private double calculateTotalPrice(List<Integer> quadIds) {
        if (mStartMillis <= 0 || mEndMillis <= 0 || quadIds.isEmpty()) return 0.0;
        long diff = mEndMillis - mStartMillis;
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days <= 0) days = 1;

        double total = 0.0;
        for (Quad quad : mAllQuads) {
            if (quadIds.contains(quad.getId())) {
                total += ((double) quad.getPrecio()) * (double) days;
            }
        }
        return total;
    }

    private void populateTextFields() {
        mRowId = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Cargar nombre del cliente
            String nom = extras.getString(ReservaEdit.RESERVA_NOM_CLIENTE);
            if (nom != null) mCustomerText.setText(nom);
            else mCustomerText.setText(extras.getString(ReservaEdit.RESERVA_CUSTOMER, ""));

            // Fecha recogida: preferir long
            if (extras.containsKey(ReservaEdit.RESERVA_FECHA_RECOGIDA)) {
                try {
                    mStartMillis = extras.getLong(ReservaEdit.RESERVA_FECHA_RECOGIDA, -1L);
                    if (mStartMillis > 0) mStartText.setText(formatDateForDisplay(mStartMillis));
                } catch (Exception e) {
                    // ignore
                }
            } else {
                String start = extras.getString(ReservaEdit.RESERVA_FECHA_RECOGIDA);
                if (start != null) {
                    mStartMillis = parseDateString(start);
                    mStartText.setText(start);
                } else {
                    mStartText.setText(extras.getString(ReservaEdit.RESERVA_START, ""));
                }
            }

            // Fecha devolucion: preferir long
            if (extras.containsKey(ReservaEdit.RESERVA_FECHA_DEVOLUCION)) {
                try {
                    mEndMillis = extras.getLong(ReservaEdit.RESERVA_FECHA_DEVOLUCION, -1L);
                    if (mEndMillis > 0) mEndText.setText(formatDateForDisplay(mEndMillis));
                } catch (Exception e) {
                    // ignore
                }
            } else {
                String end = extras.getString(ReservaEdit.RESERVA_FECHA_DEVOLUCION);
                if (end != null) {
                    mEndMillis = parseDateString(end);
                    mEndText.setText(end);
                } else {
                    mEndText.setText(extras.getString(ReservaEdit.RESERVA_END, ""));
                }
            }

            // Obtener el ID de la reserva si es edición
            mRowId = extras.getInt(ReservaEdit.RESERVA_ID, -1);
            // Si el Intent incluye la lista de quads seleccionados (viene de ThisReservaActivity), parsearlos ahora
            String selIds = extras.getString("SELECTED_QUAD_IDS", "");
            String selCascos = extras.getString("SELECTED_CASCOS", "");
            if (selIds != null && !selIds.isEmpty()) {
                String[] ids = selIds.split(",");
                String[] cas = selCascos != null ? selCascos.split(",") : new String[ids.length];
                for (int i = 0; i < ids.length; i++) {
                    try {
                        int qid = Integer.parseInt(ids[i].trim());
                        int nc = 0;
                        if (i < cas.length) {
                            try { nc = Integer.parseInt(cas[i].trim()); } catch (Exception ignored) {}
                        }
                        mInitialSelections.put(qid, nc);
                    } catch (Exception ignored) {}
                }
                // Si los checkboxes ya existen, aplicarlos inmediatamente
                if (!mQuadCheckboxes.isEmpty()) applyInitialSelections();
            }
            // Si hay un id válido, estamos editando: ajustar título y botón
            if (mRowId != null && mRowId >= 0) {
                TextView titleView = findViewById(R.id.title);
                if (titleView != null) titleView.setText("Editar reserva");
                if (mSaveButton != null) mSaveButton.setText("GUARDAR");
                
                // Cargar quads asociados en background y aplicar selecciones cuando acabe
                new Thread(() -> {
                    try {
                        ReservaQuadRepository rqRepo = new ReservaQuadRepository(getApplication());
                        List<ReservaQuad> linkedQuads = rqRepo.getQuadsByReservaId(mRowId);
                        if (linkedQuads != null) {
                            for (ReservaQuad rq : linkedQuads) {
                                // No sobrescribir si ya venían por Intent (SELECTED_QUAD_IDS)
                                if (!mInitialSelections.containsKey(rq.getQuadId())) {
                                    mInitialSelections.put(rq.getQuadId(), rq.getNumCascos());
                                }
                            }
                            Log.d("ReservaEdit", "populateTextFields: Cargados " + linkedQuads.size() + " quads");
                        }
                        // Asegurar que las selecciones se apliquen en la UI cuando terminen
                        runOnUiThread(() -> applyInitialSelections());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    // Consulta ReservaQuad para la reserva actual y aplica las selecciones a los checkboxes
    private void populateCheckboxes() {
        if (mRowId == null || mRowId < 0) return;
        // Reintentos por si la inserción de ReservaQuad está en curso desde la creación reciente
        final int MAX_ATTEMPTS = 6;
        final long RETRY_MS = 300;
        new Thread(() -> {
            try {
                ReservaQuadRepository rqRepo = new ReservaQuadRepository(getApplication());
                int attempts = 0;
                while (attempts < MAX_ATTEMPTS) {
                    List<ReservaQuad> linked = rqRepo.getQuadsByReservaId(mRowId);
                    if (linked != null && !linked.isEmpty()) {
                        for (ReservaQuad rq : linked) {
                            if (!mInitialSelections.containsKey(rq.getQuadId())) {
                                mInitialSelections.put(rq.getQuadId(), rq.getNumCascos());
                            }
                        }
                        Log.d("ReservaEdit", "populateCheckboxes: cargados " + linked.size() + " entradas de ReservaQuad");
                        runOnUiThread(() -> applyInitialSelections());
                        return;
                    }
                    attempts++;
                    try { Thread.sleep(RETRY_MS); } catch (InterruptedException ignored) {}
                }
                Log.d("ReservaEdit", "populateCheckboxes: no se encontraron entradas de ReservaQuad tras " + attempts + " intentos");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
