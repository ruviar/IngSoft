package es.unizar.eina.notepad.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import static androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import es.unizar.eina.notepad.R;
import es.unizar.eina.notepad.database.Quad;

public class QuadMenuActivity extends AppCompatActivity {

    private QuadViewModel mQuadViewModel;
    private ActivityResultLauncher<Intent> mStartCreateQuad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quadmenu);

        mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);

        mStartCreateQuad = registerForActivityResult(new StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData()!=null && result.getData().getExtras()!=null) {
                Bundle extras = result.getData().getExtras();
                // construir Quad desde extras (nuevas claves)
                if (extras.containsKey(QuadEdit.QUAD_MATRICULA) || extras.containsKey(QuadEdit.QUAD_DESCRIPCION)) {
                    String matricula = extras.getString(QuadEdit.QUAD_MATRICULA, "");
                    String descripcion = extras.getString(QuadEdit.QUAD_DESCRIPCION, "");
                    String tipoName = extras.getString(QuadEdit.QUAD_TIPO, Quad.Tipo.UNIPLAZA.name());
                    int precio = extras.getInt(QuadEdit.QUAD_PRECIO, 0);
                    Quad.Tipo tipo;
                    try { tipo = Quad.Tipo.valueOf(tipoName); } catch (Exception e) { tipo = Quad.Tipo.UNIPLAZA; }
                    Quad quad = new Quad(tipo, precio, matricula, descripcion);
                    mQuadViewModel.insert(quad);
                } else {
                    Quad quad = new Quad(extras.getString(QuadEdit.QUAD_TITLE), extras.getString(QuadEdit.QUAD_BODY));
                    mQuadViewModel.insert(quad);
                }
            }
        });

        MaterialButton add = findViewById(R.id.button_add_quad);
        MaterialButton view = findViewById(R.id.button_view_quads);

        view.setEnabled(true);
        android.util.Log.d("QuadMenuActivity", "view button initialized, enabled=" + view.isEnabled());

        add.setOnClickListener(v -> {
            mStartCreateQuad.launch(new Intent(QuadMenuActivity.this, QuadEdit.class));
        });

        view.setOnClickListener(v -> {
            android.util.Log.d("QuadMenuActivity", "button_view_quads clicked");
            try {
                startActivity(new Intent(QuadMenuActivity.this, ListaQuadsActivity.class));
            } catch (Exception e) {
                android.util.Log.e("QuadMenuActivity", "failed to start ListaQuadsActivity", e);
            }
        });
    }
}
