package es.unizar.eina.notepad.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import es.unizar.eina.notepad.R;
import es.unizar.eina.notepad.database.Reserva;
import es.unizar.eina.notepad.database.ReservaRepository;
import es.unizar.eina.notepad.database.ReservaQuad;
import es.unizar.eina.notepad.database.ReservaQuadRepository;
import static androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;

public class ListaReservasActivity extends AppCompatActivity {

    private ReservaViewModel mReservaViewModel;
    private ReservaListAdapter mAdapter;
    private List<Reserva> mAllReservas = new ArrayList<>();
    FloatingActionButton mFab;
    ActivityResultLauncher<Intent> mStartCreateReserva = registerForActivityResult(
            new StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        if (extras.containsKey(ReservaEdit.RESERVA_NOM_CLIENTE) || extras.containsKey(ReservaEdit.RESERVA_PRECIO_TOTAL)) {
                            long fechaRecogida = 0L;
                            long fechaDevolucion = 0L;
                            // prefer long extras when present
                            if (extras.containsKey(ReservaEdit.RESERVA_FECHA_RECOGIDA)) {
                                try { fechaRecogida = extras.getLong(ReservaEdit.RESERVA_FECHA_RECOGIDA, 0L); } catch (Exception ignored) {}
                            } else {
                                String s = extras.getString(ReservaEdit.RESERVA_FECHA_RECOGIDA, "");
                                try { fechaRecogida = Long.parseLong(s); } catch (Exception ignored) { fechaRecogida = 0L; }
                            }
                            if (extras.containsKey(ReservaEdit.RESERVA_FECHA_DEVOLUCION)) {
                                try { fechaDevolucion = extras.getLong(ReservaEdit.RESERVA_FECHA_DEVOLUCION, 0L); } catch (Exception ignored) {}
                            } else {
                                String s = extras.getString(ReservaEdit.RESERVA_FECHA_DEVOLUCION, "");
                                try { fechaDevolucion = Long.parseLong(s); } catch (Exception ignored) { fechaDevolucion = 0L; }
                            }
                            double precioTotal = extras.getDouble(ReservaEdit.RESERVA_PRECIO_TOTAL, 0.0);
                            int telefono = extras.getInt(ReservaEdit.RESERVA_TELEFONO, 0);
                            String nomCliente = extras.getString(ReservaEdit.RESERVA_NOM_CLIENTE, extras.getString(ReservaEdit.RESERVA_CUSTOMER));
                            Reserva r = new Reserva(fechaRecogida, fechaDevolucion, precioTotal, telefono, nomCliente);
                            // Insertar reserva y crear las entradas ReservaQuad si se devolvieron quads seleccionados
                            // Usamos los métodos bloqueantes del repositorio para asegurarnos
                            // de que las inserciones hayan finalizado antes de continuar.
                            try {
                                ReservaRepository repo = new ReservaRepository(getApplication());
                                long insertedId = repo.insert(r);
                                if (insertedId > 0) {
                                    // Buscar claves SELECTED_QUAD_IDS / SELECTED_CASCOS
                                    String selIds = extras.getString("SELECTED_QUAD_IDS", "");
                                    String selCascos = extras.getString("SELECTED_CASCOS", "");
                                    if (!selIds.isEmpty()) {
                                        String[] ids = selIds.split(",");
                                        String[] cas = selCascos != null ? selCascos.split(",") : new String[ids.length];
                                        ReservaQuadRepository rqRepo = new ReservaQuadRepository(getApplication());
                                        for (int i = 0; i < ids.length; i++) {
                                            try {
                                                int qid = Integer.parseInt(ids[i]);
                                                int nc = 0;
                                                if (i < cas.length) try { nc = Integer.parseInt(cas[i]); } catch (Exception ignored) {}
                                                ReservaQuad rq = new ReservaQuad((int) insertedId, qid, nc);
                                                rqRepo.insert(rq);
                                            } catch (Exception ignored) {}
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                android.util.Log.e("ListaReservasActivity", "Error inserting reserva/reservaQuad", e);
                            }
                        } else {
                            Reserva r = new Reserva(extras.getString(ReservaEdit.RESERVA_CUSTOMER),
                                    extras.getString(ReservaEdit.RESERVA_START),
                                    extras.getString(ReservaEdit.RESERVA_END));
                            mReservaViewModel.insert(r);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_listareservas);

            RecyclerView recyclerView = findViewById(R.id.recyclerview_lista);
            // Pasamos una instancia de QuadRepository para que el adaptador pueda resolver el precio por quadId
            mAdapter = new ReservaListAdapter(new ReservaListAdapter.ReservaDiff(), new es.unizar.eina.notepad.database.QuadRepository(getApplication()));
            recyclerView.setAdapter(mAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            android.util.Log.d("ListaReservasActivity", "onCreate - RecyclerView initialized: " + (recyclerView!=null));

            mReservaViewModel = new ViewModelProvider(this).get(ReservaViewModel.class);
            mReservaViewModel.getAllReservas().observe(this, reservas -> {
                if (reservas != null) {
                    mAllReservas = new ArrayList<>(reservas);
                    mAdapter.submitList(new ArrayList<>(mAllReservas));
                }
            });

            MaterialButton btnCliente = findViewById(R.id.button_filter_cliente);
            MaterialButton btnRecogida = findViewById(R.id.button_filter_fecha_recogida);
            MaterialButton btnDevolucion = findViewById(R.id.button_filter_fecha_devolucion);

            btnCliente.setOnClickListener(v -> sortByCliente());
            btnRecogida.setOnClickListener(v -> sortByRecogida());
            btnDevolucion.setOnClickListener(v -> sortByDevolucion());
            // Floating action button: abrir pantalla de añadir reserva
            mFab = findViewById(R.id.fab);
            mFab.setOnClickListener(v -> createReserva());
        } catch (Throwable t) {
            android.util.Log.e("ListaReservasActivity", "Exception in onCreate", t);
            Toast.makeText(this, "Error al abrir Lista de Reservas: " + t.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
        }
    }

    private void createReserva() {
        mStartCreateReserva.launch(new Intent(this, ReservaEdit.class));
    }

    private void sortByCliente() {
        if (mAllReservas == null) return;
        List<Reserva> copy = new ArrayList<>(mAllReservas);
        Collections.sort(copy, Comparator.comparing(r -> r.getCustomer() == null ? "" : r.getCustomer()));
        mAdapter.submitList(copy);
        Toast.makeText(this, "Ordenado por cliente", Toast.LENGTH_SHORT).show();
    }

    private void sortByRecogida() {
        if (mAllReservas == null) return;
        List<Reserva> copy = new ArrayList<>(mAllReservas);
        Collections.sort(copy, Comparator.comparing(r -> r.getStartDate() == null ? "" : r.getStartDate()));
        mAdapter.submitList(copy);
        Toast.makeText(this, "Ordenado por fecha recogida", Toast.LENGTH_SHORT).show();
    }

    private void sortByDevolucion() {
        if (mAllReservas == null) return;
        List<Reserva> copy = new ArrayList<>(mAllReservas);
        Collections.sort(copy, Comparator.comparing(r -> r.getEndDate() == null ? "" : r.getEndDate()));
        mAdapter.submitList(copy);
        Toast.makeText(this, "Ordenado por fecha devolución", Toast.LENGTH_SHORT).show();
    }
}
