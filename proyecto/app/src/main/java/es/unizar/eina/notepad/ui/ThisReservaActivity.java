package es.unizar.eina.notepad.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import static androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import es.unizar.eina.notepad.R;
import es.unizar.eina.notepad.database.Reserva;
import es.unizar.eina.notepad.database.ReservaQuad;
import es.unizar.eina.notepad.database.ReservaQuadRepository;

public class ThisReservaActivity extends AppCompatActivity {

    public static final String EXTRA_RESERVA_ID = "thisreserva_reserva_id";

    private ReservaViewModel mReservaViewModel;
    private Reserva mReserva;
    private int mReservaId = -1;

    private ActivityResultLauncher<Intent> mStartEditReserva = registerForActivityResult(
            new StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData()!=null && result.getData().getExtras()!=null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras.containsKey(ReservaEdit.RESERVA_NOM_CLIENTE) || extras.containsKey(ReservaEdit.RESERVA_PRECIO_TOTAL)) {
                        long fechaRecogida = extras.getLong(ReservaEdit.RESERVA_FECHA_RECOGIDA, 0L);
                        long fechaDevolucion = extras.getLong(ReservaEdit.RESERVA_FECHA_DEVOLUCION, 0L);
                        double precioTotal = extras.getDouble(ReservaEdit.RESERVA_PRECIO_TOTAL, 0.0);
                        int telefono = extras.getInt(ReservaEdit.RESERVA_TELEFONO, 0);
                        String nomCliente = extras.getString(ReservaEdit.RESERVA_NOM_CLIENTE, extras.getString(ReservaEdit.RESERVA_CUSTOMER));
                        Reserva r = new Reserva(fechaRecogida, fechaDevolucion, precioTotal, telefono, nomCliente);
                        int id = extras.getInt(ReservaEdit.RESERVA_ID, mReservaId);
                        r.setId(id);
                        mReservaViewModel.update(r);
                        mReserva = r;
                        // Si vienen quads seleccionados en el resultado, sincronizarlos con la tabla reserva_quad
                        if (extras.containsKey("SELECTED_QUAD_IDS")) {
                            new Thread(() -> {
                                try {
                                    String selIds = extras.getString("SELECTED_QUAD_IDS", "");
                                    String selCascos = extras.getString("SELECTED_CASCOS", "");
                                    ReservaQuadRepository rqRepo = new ReservaQuadRepository(getApplication());
                                    // Eliminar entradas previas de esta reserva
                                    rqRepo.deleteByReservaId(id);
                                    if (selIds != null && !selIds.isEmpty()) {
                                        String[] ids = selIds.split(",");
                                        String[] cas = selCascos != null ? selCascos.split(",") : new String[ids.length];
                                        for (int i = 0; i < ids.length; i++) {
                                            try {
                                                int qid = Integer.parseInt(ids[i].trim());
                                                int nc = 0;
                                                if (i < cas.length) try { nc = Integer.parseInt(cas[i].trim()); } catch (Exception ignored) {}
                                                ReservaQuad rq = new ReservaQuad(id, qid, nc);
                                                rqRepo.insert(rq);
                                            } catch (Exception ignored) {}
                                        }
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }).start();
                        }
                        updateTitleAndSubtitle();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thisreserva);

        mReservaViewModel = new ViewModelProvider(this).get(ReservaViewModel.class);

        Intent incoming = getIntent();
        if (incoming != null && incoming.hasExtra(EXTRA_RESERVA_ID)) {
            mReservaId = incoming.getIntExtra(EXTRA_RESERVA_ID, -1);
        }

        // If extras include fields, use them to prefill without DB lookup
        Bundle extras = incoming!=null?incoming.getExtras():null;
        if (extras != null) {
            String nom = extras.getString(ReservaEdit.RESERVA_NOM_CLIENTE, extras.getString(ReservaEdit.RESERVA_CUSTOMER, null));
            double precio = extras.getDouble(ReservaEdit.RESERVA_PRECIO_TOTAL, 0.0);
            if (nom!=null) {
                long fechaRecogida = extras.getLong(ReservaEdit.RESERVA_FECHA_RECOGIDA, 0L);
                long fechaDevolucion = extras.getLong(ReservaEdit.RESERVA_FECHA_DEVOLUCION, 0L);
                int telefono = extras.getInt(ReservaEdit.RESERVA_TELEFONO, 0);
                mReserva = new Reserva(fechaRecogida, fechaDevolucion, precio, telefono, nom);
                mReserva.setId(extras.getInt(ReservaEdit.RESERVA_ID, mReservaId));
            }
        }

        updateTitleAndSubtitle();

        MaterialButton btnEnviar = findViewById(R.id.button_enviar);
        MaterialButton btnDatos = findViewById(R.id.button_datos_reserva);
        MaterialButton btnEliminar = findViewById(R.id.button_eliminar_reserva);

        btnEnviar.setOnClickListener(v -> {
            if (mReserva==null) { Toast.makeText(this, "No hay datos para enviar", Toast.LENGTH_SHORT).show(); return; }
            String text = "Reserva de " + mReserva.getNomCliente() + " - Precio: " + String.format("%.2f €", mReserva.getPrecioTotal()) + "\nFecha: " + mReserva.getStartDate() + " - " + mReserva.getEndDate();
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(send, "Enviar reserva"));
        });

        btnDatos.setOnClickListener(v -> {
            if (mReserva != null) {
                // Cargar quads asociados en background, reintentando si es necesario, luego lanzar intent en UI thread
                new Thread(() -> {
                    try {
                        ReservaQuadRepository rqRepo = new ReservaQuadRepository(getApplication());
                        java.util.List<ReservaQuad> linked = rqRepo.getQuadsByReservaId(mReserva.getId());
                        // Si está vacío, reintentar algunas veces (esperando a que la inserción termine)
                        int attempts = 0;
                        final int MAX_ATTEMPTS = 10;
                        final long RETRY_MS = 250;
                        while ((linked == null || linked.isEmpty()) && attempts < MAX_ATTEMPTS) {
                            attempts++;
                            try { Thread.sleep(RETRY_MS); } catch (InterruptedException ignored) {}
                            linked = rqRepo.getQuadsByReservaId(mReserva.getId());
                        }

                        final java.util.List<ReservaQuad> finalLinked = linked;
                        // Volver al UI thread para crear intent y lanzar
                        runOnUiThread(() -> {
                            Intent intent = new Intent(ThisReservaActivity.this, ReservaEdit.class);
                            intent.putExtra(ReservaEdit.RESERVA_NOM_CLIENTE, mReserva.getNomCliente());
                            intent.putExtra(ReservaEdit.RESERVA_FECHA_RECOGIDA, mReserva.getStartDateLong());
                            intent.putExtra(ReservaEdit.RESERVA_FECHA_DEVOLUCION, mReserva.getEndDateLong());
                            // also include formatted strings for compatibility
                            intent.putExtra(ReservaEdit.RESERVA_START, mReserva.getStartDate());
                            intent.putExtra(ReservaEdit.RESERVA_END, mReserva.getEndDate());
                            intent.putExtra(ReservaEdit.RESERVA_PRECIO_TOTAL, mReserva.getPrecioTotal());
                            intent.putExtra(ReservaEdit.RESERVA_TELEFONO, mReserva.getTelefono());
                            intent.putExtra(ReservaEdit.RESERVA_ID, mReserva.getId());

                            // Añadir quads asociados para que ReservaEdit los pueda pre-seleccionar
                            if (finalLinked != null && !finalLinked.isEmpty()) {
                                StringBuilder ids = new StringBuilder();
                                StringBuilder cascos = new StringBuilder();
                                for (int j = 0; j < finalLinked.size(); j++) {
                                    ReservaQuad rq = finalLinked.get(j);
                                    if (j > 0) { ids.append(","); cascos.append(","); }
                                    ids.append(rq.getQuadId());
                                    cascos.append(rq.getNumCascos());
                                }
                                intent.putExtra("SELECTED_QUAD_IDS", ids.toString());
                                intent.putExtra("SELECTED_CASCOS", cascos.toString());
                            }

                            mStartEditReserva.launch(intent);
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        // En caso de error, lanzar intent con al menos el ID
                        runOnUiThread(() -> {
                            Intent intent = new Intent(ThisReservaActivity.this, ReservaEdit.class);
                            intent.putExtra(ReservaEdit.RESERVA_ID, mReserva.getId());
                            mStartEditReserva.launch(intent);
                        });
                    }
                }).start();
            } else {
                Intent intent = new Intent(ThisReservaActivity.this, ReservaEdit.class);
                intent.putExtra(ReservaEdit.RESERVA_ID, mReservaId);
                mStartEditReserva.launch(intent);
            }
        });

        btnEliminar.setOnClickListener(v -> {
            String nombre = (mReserva != null && mReserva.getNomCliente() != null) ? mReserva.getNomCliente() : ("#" + mReservaId);
            String msg = "Seguro que quieres eliminar la reserva de " + nombre + "?";
            AlertDialog.Builder builder = new AlertDialog.Builder(ThisReservaActivity.this);
            builder.setTitle("Confirmar eliminación");
            builder.setMessage(msg);
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            builder.setPositiveButton("Sí", (dialog, which) -> {
                // perform delete; mirror ThisQuadActivity behavior: fetch from repo if needed
                if (mReserva != null) {
                    mReservaViewModel.delete(mReserva);
                } else {
                    new Thread(() -> {
                        es.unizar.eina.notepad.database.ReservaRepository repo = new es.unizar.eina.notepad.database.ReservaRepository(getApplication());
                        es.unizar.eina.notepad.database.Reserva r = repo.getReservaById(mReservaId);
                        if (r != null) {
                            mReservaViewModel.delete(r);
                        }
                    }).start();
                }
                dialog.dismiss();
                startActivity(new Intent(ThisReservaActivity.this, ListaReservasActivity.class));
                finish();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(ThisReservaActivity.this, R.color.confirm_no));
            }
            if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(ThisReservaActivity.this, R.color.danger));
            }
        });
    }

    private void updateTitleAndSubtitle() {
        TextView title = findViewById(R.id.title_reserva);
        TextView subtitle = findViewById(R.id.subtitle_reserva);
        if (mReserva != null) {
            title.setText(mReserva.getNomCliente() == null ? "" : mReserva.getNomCliente());
            subtitle.setText(String.format("%.2f €", mReserva.getPrecioTotal()));
        } else {
            if (mReservaId!=-1) title.setText("Reserva #" + mReservaId);
            subtitle.setText("");
        }
    }

}
