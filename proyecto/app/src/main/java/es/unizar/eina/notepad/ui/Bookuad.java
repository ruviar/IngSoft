package es.unizar.eina.notepad.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import es.unizar.eina.notepad.database.Quad;
import es.unizar.eina.notepad.R;

import static androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;

/**
 * Activity principal para la gestión de quads.
 * <p>
 * Muestra la lista de quads disponibles en un RecyclerView y permite
 * crear, editar y eliminar quads mediante un menú contextual.
 * Implementa el patrón MVVM observando cambios en el ViewModel.
 * </p>
 * <p>
 * Proporciona navegación hacia:
 * - {@link QuadEdit} para crear/editar quads
 * - ReservasActivity para gestionar reservas
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see QuadViewModel
 * @see QuadEdit
 * @see QuadListAdapter
 */
public class Bookuad extends AppCompatActivity {
    private QuadViewModel mQuadViewModel;

    static final int INSERT_ID = Menu.FIRST;
    static final int DELETE_ID = Menu.FIRST + 1;
    static final int EDIT_ID = Menu.FIRST + 2;
    static final int RESERVAS_ID = Menu.FIRST + 3;

    RecyclerView mRecyclerView;

    QuadListAdapter mAdapter;

    FloatingActionButton mFab;

    /**
     * Inicializa la activity.
     * <p>
     * Configura el RecyclerView con su adaptador, inicializa el ViewModel
     * y establece el observador para actualizar la lista automáticamente.
     * </p>
     *
     * @param savedInstanceState Estado guardado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listaquads);
        mRecyclerView = findViewById(R.id.recyclerview_lista);
        mAdapter = new QuadListAdapter(new QuadListAdapter.QuadDiff());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);

        mQuadViewModel.getAllQuads().observe(this, quads -> {
            mAdapter.submitList(quads);
        });

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(view -> createQuad());

        registerForContextMenu(mRecyclerView);

    }

    /**
     * Crea el menú de opciones.
     * @param menu Menú a inflar.
     * @return true si se creó correctamente.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.add_note);
        menu.add(Menu.NONE, RESERVAS_ID, Menu.NONE, R.string.add_reserva);
        return result;
    }

    /**
     * Maneja la selección de opciones del menú.
     * @param item Item del menú seleccionado.
     * @return true si se manejó el evento.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_ID:
                createQuad();
                return true;
            case RESERVAS_ID:
                startActivity(new Intent(this, ReservasActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Maneja la selección de opciones del menú contextual.
     * <p>
     * Permite editar o eliminar el quad seleccionado.
     * </p>
     *
     * @param item Item del menú contextual seleccionado.
     * @return true si se manejó el evento.
     */
    public boolean onContextItemSelected(MenuItem item) {
        Quad current = mAdapter.getCurrent();
        switch (item.getItemId()) {
            case DELETE_ID:
                Toast.makeText(
                        getApplicationContext(),
                        "Deleting " + (current.getMatricula() != null ? current.getMatricula() : "quad"),
                        Toast.LENGTH_LONG).show();
                mQuadViewModel.delete(current);
                return true;
            case EDIT_ID:
                editQuad(current);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Lanza la activity QuadEdit para crear un nuevo quad.
     */
    private void createQuad() {
        mStartCreateQuad.launch(new Intent(this, QuadEdit.class));
    }

    ActivityResultLauncher<Intent> mStartCreateQuad = newActivityResultLauncher(new ExecuteActivityResult() {
        @Override
        public void process(Bundle extras, Quad quad) {
            mQuadViewModel.insert(quad);
        }
    });

    ActivityResultLauncher<Intent> newActivityResultLauncher(ExecuteActivityResult executable) {
        return registerForActivityResult(
                new StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Bundle extras = result.getData().getExtras();
                            // Construir Quad usando las claves nuevas si están presentes
                            if (extras.containsKey(QuadEdit.QUAD_MATRICULA) || extras.containsKey(QuadEdit.QUAD_DESCRIPCION)) {
                                String matricula = extras.getString(QuadEdit.QUAD_MATRICULA, "");
                                String descripcion = extras.getString(QuadEdit.QUAD_DESCRIPCION, "");
                                String tipoName = extras.getString(QuadEdit.QUAD_TIPO, Quad.Tipo.UNIPLAZA.name());
                                int precio = extras.getInt(QuadEdit.QUAD_PRECIO, 0);
                                Quad.Tipo tipo;
                                try { tipo = Quad.Tipo.valueOf(tipoName); } catch (Exception e) { tipo = Quad.Tipo.UNIPLAZA; }
                                Quad quad = new Quad(tipo, precio, matricula, descripcion);
                                executable.process(extras, quad);
                            } else {
                                Quad quad = new Quad(extras.getString(QuadEdit.QUAD_TITLE),
                                        extras.getString(QuadEdit.QUAD_BODY));
                                executable.process(extras, quad);
                            }
                    }
                });
    }

    /**
     * Lanza la activity QuadEdit para editar un quad existente.
     * @param current Quad a editar.
     */
    private void editQuad(Quad current) {
        Intent intent = new Intent(this, QuadEdit.class);
        // Compatibilidad: antiguas claves
        intent.putExtra(QuadEdit.QUAD_TITLE, current.getMatricula());
        intent.putExtra(QuadEdit.QUAD_BODY, current.getDescripcion());
        // Nuevas claves
        intent.putExtra(QuadEdit.QUAD_MATRICULA, current.getMatricula());
        intent.putExtra(QuadEdit.QUAD_DESCRIPCION, current.getDescripcion());
        intent.putExtra(QuadEdit.QUAD_TIPO, current.getTipo() != null ? current.getTipo().name() : Quad.Tipo.UNIPLAZA.name());
        intent.putExtra(QuadEdit.QUAD_PRECIO, current.getPrecio());
        intent.putExtra(QuadEdit.QUAD_ID, current.getId());
        mStartUpdateQuad.launch(intent);
    }

    ActivityResultLauncher<Intent> mStartUpdateQuad = newActivityResultLauncher(new ExecuteActivityResult() {
        @Override
        public void process(Bundle extras, Quad quad) {
            int id = extras.getInt(QuadEdit.QUAD_ID);
            quad.setId(id);
            mQuadViewModel.update(quad);
        }
    });

}

interface ExecuteActivityResult {
    void process(Bundle extras, Quad quad);
}
