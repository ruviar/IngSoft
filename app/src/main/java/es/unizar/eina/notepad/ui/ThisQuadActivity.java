package es.unizar.eina.notepad.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import static androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import es.unizar.eina.notepad.R;
import es.unizar.eina.notepad.database.Quad;
import es.unizar.eina.notepad.database.QuadRepository;
import es.unizar.eina.notepad.database.ReservaQuad;
import es.unizar.eina.notepad.database.ReservaQuadRepository;
import java.util.List;

public class ThisQuadActivity extends AppCompatActivity {

    public static final String EXTRA_QUAD_ID = "thisquad_quad_id";

    private QuadViewModel mQuadViewModel;
    private Quad mQuad;
    private int mQuadId = -1;

    private ActivityResultLauncher<Intent> mStartEditQuad = registerForActivityResult(
            new StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData()!=null && result.getData().getExtras()!=null) {
                    // QuadEdit will return the edited fields; reuse Bookuad/newActivityResultLauncher logic elsewhere
                    Bundle extras = result.getData().getExtras();
                    if (extras.containsKey(QuadEdit.QUAD_MATRICULA) || extras.containsKey(QuadEdit.QUAD_DESCRIPCION)) {
                        String matricula = extras.getString(QuadEdit.QUAD_MATRICULA, "");
                        String descripcion = extras.getString(QuadEdit.QUAD_DESCRIPCION, "");
                        String tipoName = extras.getString(QuadEdit.QUAD_TIPO, Quad.Tipo.UNIPLAZA.name());
                        int precio = extras.getInt(QuadEdit.QUAD_PRECIO, 0);
                        Quad.Tipo tipo;
                        try { tipo = Quad.Tipo.valueOf(tipoName); } catch (Exception e) { tipo = Quad.Tipo.UNIPLAZA; }
                        Quad updated = new Quad(tipo, precio, matricula, descripcion);
                        int id = extras.getInt(QuadEdit.QUAD_ID, mQuadId);
                        updated.setId(id);
                        mQuadViewModel.update(updated);
                        mQuad = updated;
                        updateTitle();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thisquad);

        mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);

        Intent i = getIntent();
        if (i != null && i.hasExtra(EXTRA_QUAD_ID)) {
            mQuadId = i.getIntExtra(EXTRA_QUAD_ID, -1);
        }
        if (mQuadId == -1) {
            finish();
            return;
        }

        // cargar quad en background y actualizar UI
        new Thread(() -> {
            QuadRepository repo = new QuadRepository(getApplication());
            Quad q = repo.getQuadById(mQuadId);
            mQuad = q;
            runOnUiThread(this::updateTitle);
        }).start();

        MaterialButton btnDatos = findViewById(R.id.button_datos);
        MaterialButton btnEliminar = findViewById(R.id.button_eliminar);

        btnDatos.setOnClickListener(v -> {
            Intent intent = new Intent(ThisQuadActivity.this, QuadEdit.class);
            if (mQuad != null) {
                intent.putExtra(QuadEdit.QUAD_MATRICULA, mQuad.getMatricula());
                intent.putExtra(QuadEdit.QUAD_DESCRIPCION, mQuad.getDescripcion());
                intent.putExtra(QuadEdit.QUAD_TIPO, mQuad.getTipo()!=null?mQuad.getTipo().name():Quad.Tipo.UNIPLAZA.name());
                intent.putExtra(QuadEdit.QUAD_PRECIO, mQuad.getPrecio());
                intent.putExtra(QuadEdit.QUAD_ID, mQuad.getId());
            } else {
                intent.putExtra(QuadEdit.QUAD_ID, mQuadId);
            }
            mStartEditQuad.launch(intent);
        });

        btnEliminar.setOnClickListener(v -> {
            String mat = (mQuad != null && mQuad.getMatricula() != null) ? mQuad.getMatricula() : ("#" + mQuadId);
            
            // Verificar si el quad tiene reservas asociadas antes de permitir eliminación
            new Thread(() -> {
                ReservaQuadRepository rqRepo = new ReservaQuadRepository(getApplication());
                List<ReservaQuad> reservasAsociadas = rqRepo.getReservasByQuadId(mQuadId);
                
                runOnUiThread(() -> {
                    if (reservasAsociadas != null && !reservasAsociadas.isEmpty()) {
                        // Bloquear eliminación: el quad tiene reservas activas
                        AlertDialog.Builder builder = new AlertDialog.Builder(ThisQuadActivity.this);
                        builder.setTitle("No se puede eliminar");
                        builder.setMessage("El quad " + mat + " no puede ser eliminado porque está incluido en " + 
                                         reservasAsociadas.size() + " reserva(s) activa(s). " +
                                         "Por favor, elimine o modifique las reservas asociadas primero.");
                        builder.setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss());
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        // No hay reservas asociadas, proceder con confirmación de eliminación
                        String msg = "Seguro que quieres eliminar el quad " + mat + "?";
                        AlertDialog.Builder builder = new AlertDialog.Builder(ThisQuadActivity.this);
                        builder.setTitle("Confirmar eliminación");
                        builder.setMessage(msg);
                        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
                        builder.setPositiveButton("Sí", (dialog, which) -> {
                            // perform delete
                            if (mQuad != null) {
                                mQuadViewModel.delete(mQuad);
                            } else {
                                new Thread(() -> {
                                    QuadRepository repo = new QuadRepository(getApplication());
                                    Quad q = repo.getQuadById(mQuadId);
                                    if (q != null) {
                                        mQuadViewModel.delete(q);
                                    }
                                }).start();
                            }
                            dialog.dismiss();
                            // go to ListaQuads
                            startActivity(new Intent(ThisQuadActivity.this, ListaQuadsActivity.class));
                            finish();
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        // color buttons: No -> green, Sí -> red
                        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(ThisQuadActivity.this, R.color.confirm_no));
                        }
                        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(ThisQuadActivity.this, R.color.danger));
                        }
                    }
                });
            }).start();
        });
    }

    private void updateTitle() {
        TextView title = findViewById(R.id.title_quad);
        if (mQuad != null) title.setText(mQuad.getMatricula()==null?"":mQuad.getMatricula());
    }
}
