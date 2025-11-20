package es.unizar.eina.notepad.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import es.unizar.eina.notepad.R;
import es.unizar.eina.notepad.database.Quad;

public class ListaQuadsActivity extends AppCompatActivity {

    private QuadViewModel mQuadViewModel;
    private QuadListAdapter mAdapter;
    private List<Quad> mAllQuads = new ArrayList<>();
    private com.google.android.material.floatingactionbutton.FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_listaquads);

            RecyclerView recyclerView = findViewById(R.id.recyclerview_lista);
            mAdapter = new QuadListAdapter(new QuadListAdapter.QuadDiff());
            recyclerView.setAdapter(mAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            android.util.Log.d("ListaQuadsActivity", "onCreate - RecyclerView initialized: " + (recyclerView!=null));

            mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);
            mQuadViewModel.getAllQuads().observe(this, quads -> {
                android.util.Log.d("ListaQuadsActivity", "observed quads size=" + (quads==null?0:quads.size()));
                if (quads != null) {
                    mAllQuads = new ArrayList<>(quads);
                    mAdapter.submitList(new ArrayList<>(mAllQuads));
                }
            });

            MaterialButton btnMatricula = findViewById(R.id.button_filter_matricula);
            MaterialButton btnTipo = findViewById(R.id.button_filter_tipo);
            MaterialButton btnPrecio = findViewById(R.id.button_filter_precio);

            btnMatricula.setOnClickListener(v -> sortByMatricula());
            btnTipo.setOnClickListener(v -> sortByTipo());
            btnPrecio.setOnClickListener(v -> sortByPrecio());

            // Floating action button: añadir nuevo quad
            mFab = findViewById(R.id.fab);
            mFab.setOnClickListener(v -> {
                startActivity(new android.content.Intent(ListaQuadsActivity.this, QuadEdit.class));
            });
        } catch (Throwable t) {
            android.util.Log.e("ListaQuadsActivity", "Exception in onCreate", t);
            Toast.makeText(this, "Error al abrir Lista de Quads: " + t.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
        }
    }

    private void sortByMatricula() {
        if (mAllQuads == null) return;
        List<Quad> copy = new ArrayList<>(mAllQuads);
        Collections.sort(copy, Comparator.comparing(q -> q.getMatricula() == null ? "" : q.getMatricula()));
        mAdapter.submitList(copy);
        Toast.makeText(this, "Ordenado por matrícula", Toast.LENGTH_SHORT).show();
    }

    private void sortByTipo() {
        if (mAllQuads == null) return;
        List<Quad> copy = new ArrayList<>(mAllQuads);
        Collections.sort(copy, new Comparator<Quad>() {
            @Override
            public int compare(Quad a, Quad b) {
                int va = a.getTipo() == null ? 1 : (a.getTipo() == Quad.Tipo.UNIPLAZA ? 0 : 1);
                int vb = b.getTipo() == null ? 1 : (b.getTipo() == Quad.Tipo.UNIPLAZA ? 0 : 1);
                if (va != vb) return Integer.compare(va, vb);
                // fallback alphabetical matricula
                String ma = a.getMatricula() == null ? "" : a.getMatricula();
                String mb = b.getMatricula() == null ? "" : b.getMatricula();
                return ma.compareTo(mb);
            }
        });
        mAdapter.submitList(copy);
        Toast.makeText(this, "Ordenado por tipo", Toast.LENGTH_SHORT).show();
    }

    private void sortByPrecio() {
        if (mAllQuads == null) return;
        List<Quad> copy = new ArrayList<>(mAllQuads);
        Collections.sort(copy, Comparator.comparingInt(Quad::getPrecio));
        mAdapter.submitList(copy);
        Toast.makeText(this, "Ordenado por precio", Toast.LENGTH_SHORT).show();
    }
}
