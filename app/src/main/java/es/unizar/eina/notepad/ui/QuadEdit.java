package es.unizar.eina.notepad.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import es.unizar.eina.notepad.R;
import es.unizar.eina.notepad.database.Quad;
import es.unizar.eina.notepad.ui.QuadViewModel;

/**
 * Activity para crear o editar un quad.
 * <p>
 * Proporciona un formulario con campos para:
 * - Matrícula (obligatorio)
 * - Tipo (uniplaza/biplaza)
 * - Precio por día
 * - Descripción
 * </p>
 * <p>
 * Valida que la matrícula no esté vacía antes de guardar.
 * Devuelve los datos mediante un Intent al Activity padre.
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see Quad
 * @see Bookuad
 */
public class QuadEdit extends AppCompatActivity {

    /** Clave para pasar el título/matrícula (compatibilidad) */
    public static final String QUAD_TITLE = "title";
    /** Clave para pasar el cuerpo/descripción (compatibilidad) */
    public static final String QUAD_BODY = "body";
    /** Clave para pasar el identificador del quad */
    public static final String QUAD_ID = "id";

    /** Clave para pasar el tipo de quad */
    public static final String QUAD_TIPO = "tipo";
    /** Clave para pasar el precio por día */
    public static final String QUAD_PRECIO = "precio";
    /** Clave para pasar la matrícula */
    public static final String QUAD_MATRICULA = "matricula";
    /** Clave para pasar la descripción */
    public static final String QUAD_DESCRIPCION = "descripcion";

    private EditText mTitleText;

    private EditText mBodyText;

    private Integer mRowId;

    private EditText mMatricula;
    private EditText mPrecio;
    private EditText mDescripcion;
    private RadioGroup mRadioTipo;
    private MaterialButton mConfirmButton;

    private QuadViewModel mQuadViewModel;

    /**
     * Inicializa la activity.
     * <p>
     * Configura los campos del formulario, el botón de confirmación
     * y carga los datos si es una edición.
     * </p>
     *
     * @param savedInstanceState Estado guardado de la instancia.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quadedit);

        mMatricula = findViewById(R.id.edit_matricula);
        mPrecio = findViewById(R.id.edit_precio);
        mDescripcion = findViewById(R.id.edit_descripcion);
        mRadioTipo = findViewById(R.id.radio_tipo);
        mConfirmButton = findViewById(R.id.button_confirm);

        mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);

        mConfirmButton.setOnClickListener(view -> {
            Intent replyIntent = new Intent();
            String matricula = mMatricula.getText().toString().trim();
            if (TextUtils.isEmpty(matricula)) {
                setResult(RESULT_CANCELED, replyIntent);
                Toast.makeText(getApplicationContext(), R.string.empty_not_saved, Toast.LENGTH_LONG).show();
                return;
            }

            String descripcion = mDescripcion.getText().toString();
            int precio = 0;
            try {
                String p = mPrecio.getText().toString().trim();
                if (!TextUtils.isEmpty(p)) precio = Integer.parseInt(p);
            } catch (NumberFormatException e) { precio = 0; }

            int checked = mRadioTipo.getCheckedRadioButtonId();
            Quad.Tipo tipo = Quad.Tipo.UNIPLAZA;
            if (checked == R.id.radio_biplaza) tipo = Quad.Tipo.BIPLAZA;

            Quad quad = new Quad(tipo, precio, matricula, descripcion);
            // Nota: no insertar aquí. Devolver los datos al padre para que inserte/actualice.
            android.util.Log.d("QuadEdit", "ready to return matricula=" + matricula + " tipo=" + tipo.name() + " precio=" + precio);

            // Compatibilidad: también devolver extras
            replyIntent.putExtra(QUAD_MATRICULA, matricula);
            replyIntent.putExtra(QUAD_DESCRIPCION, descripcion);
            replyIntent.putExtra(QUAD_TIPO, tipo.name());
            replyIntent.putExtra(QUAD_PRECIO, precio);
            // Si estamos editando, devolver también el id para claridad
            if (mRowId != null && mRowId >= 0) replyIntent.putExtra(QUAD_ID, mRowId);

            setResult(RESULT_OK, replyIntent);

            // Volver al menú de quads
            finish();
        });

        populateFields();

    }

    /**
     * Rellena los campos del formulario con los datos del quad a editar.
     * <p>
     * Si no hay datos en el Intent, inicializa los campos vacíos para
     * crear un nuevo quad.
     * </p>
     */
    private void populateFields () {
        mRowId = null;
        Bundle extras = getIntent().getExtras();
        if (extras!=null) {
            // Preferir nuevas claves si están presentes
            String matricula = extras.getString(QuadEdit.QUAD_MATRICULA);
            String descripcion = extras.getString(QuadEdit.QUAD_DESCRIPCION);
            if (matricula!=null) mMatricula.setText(matricula);
            else mMatricula.setText(extras.getString(QuadEdit.QUAD_TITLE, ""));

            if (descripcion!=null) mDescripcion.setText(descripcion);
            else mDescripcion.setText(extras.getString(QuadEdit.QUAD_BODY, ""));

            int precio = extras.getInt(QuadEdit.QUAD_PRECIO, -1);
            if (precio>=0) mPrecio.setText(String.valueOf(precio));

            String tipoName = extras.getString(QuadEdit.QUAD_TIPO);
            if (tipoName!=null) {
                try {
                    Quad.Tipo t = Quad.Tipo.valueOf(tipoName);
                    if (t == Quad.Tipo.BIPLAZA) mRadioTipo.check(R.id.radio_biplaza);
                    else mRadioTipo.check(R.id.radio_uniplaza);
                } catch (Exception ignored) { mRadioTipo.check(R.id.radio_uniplaza); }
            }

            mRowId = extras.getInt(QuadEdit.QUAD_ID, -1);
            // Si hay un id válido, estamos editando: ajustar título y botón
            if (mRowId != null && mRowId >= 0) {
                TextView titleView = findViewById(R.id.title);
                if (titleView != null) titleView.setText("Editar quad");
                if (mConfirmButton != null) mConfirmButton.setText("GUARDAR");
            }
        }
    }

}
