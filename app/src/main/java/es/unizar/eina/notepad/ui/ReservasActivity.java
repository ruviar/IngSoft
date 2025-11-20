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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import es.unizar.eina.notepad.R;
import es.unizar.eina.notepad.database.Reserva;

import static androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;

public class ReservasActivity extends AppCompatActivity {

    private ReservaViewModel mReservaViewModel;
    static final int INSERT_ID = Menu.FIRST;

    RecyclerView mRecyclerView;
    ReservaListAdapter mAdapter;
    FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservas);

        mRecyclerView = findViewById(R.id.recyclerview);
        mAdapter = new ReservaListAdapter(new ReservaListAdapter.ReservaDiff(), new es.unizar.eina.notepad.database.QuadRepository(getApplication()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // enable context menu for edit/delete
        registerForContextMenu(mRecyclerView);

        mReservaViewModel = new ViewModelProvider(this).get(ReservaViewModel.class);
        mReservaViewModel.getAllReservas().observe(this, reservas -> mAdapter.submitList(reservas));

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(v -> createReserva());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.add_reserva);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == INSERT_ID) {
            createReserva();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createReserva() {
        mStartCreateReserva.launch(new Intent(this, ReservaEdit.class));
    }

    public boolean onContextItemSelected(MenuItem item) {
        Reserva current = mAdapter.getCurrent();
        switch (item.getItemId()) {
            case Bookuad.DELETE_ID:
                mReservaViewModel.delete(current);
                return true;
            case Bookuad.EDIT_ID:
                editReserva(current);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void editReserva(Reserva current) {
        Intent intent = new Intent(this, ReservaEdit.class);
        intent.putExtra(ReservaEdit.RESERVA_CUSTOMER, current.getCustomer());
        // Preferir pasar fechas como long (milis) y también incluir las cadenas por compatibilidad
        intent.putExtra(ReservaEdit.RESERVA_FECHA_RECOGIDA, current.getStartDateLong());
        intent.putExtra(ReservaEdit.RESERVA_FECHA_DEVOLUCION, current.getEndDateLong());
        intent.putExtra(ReservaEdit.RESERVA_START, current.getStartDate());
        intent.putExtra(ReservaEdit.RESERVA_END, current.getEndDate());
        intent.putExtra(ReservaEdit.RESERVA_ID, current.getId());
        mStartUpdateReserva.launch(intent);
    }

    ActivityResultLauncher<Intent> mStartCreateReserva = registerForActivityResult(
            new StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        if (extras.containsKey(ReservaEdit.RESERVA_NOM_CLIENTE) || extras.containsKey(ReservaEdit.RESERVA_PRECIO_TOTAL)) {
                            long fechaRecogida = extras.getLong(ReservaEdit.RESERVA_FECHA_RECOGIDA, 0L);
                            long fechaDevolucion = extras.getLong(ReservaEdit.RESERVA_FECHA_DEVOLUCION, 0L);
                            double precioTotal = extras.getDouble(ReservaEdit.RESERVA_PRECIO_TOTAL, 0.0);
                            int telefono = extras.getInt(ReservaEdit.RESERVA_TELEFONO, 0);
                            String nomCliente = extras.getString(ReservaEdit.RESERVA_NOM_CLIENTE, extras.getString(ReservaEdit.RESERVA_CUSTOMER));
                            Reserva r = new Reserva(fechaRecogida, fechaDevolucion, precioTotal, telefono, nomCliente);
                            mReservaViewModel.insert(r);
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

    ActivityResultLauncher<Intent> mStartUpdateReserva = registerForActivityResult(
            new StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        if (extras.containsKey(ReservaEdit.RESERVA_NOM_CLIENTE) || extras.containsKey(ReservaEdit.RESERVA_PRECIO_TOTAL)) {
                                long fechaRecogida = extras.getLong(ReservaEdit.RESERVA_FECHA_RECOGIDA, 0L);
                                long fechaDevolucion = extras.getLong(ReservaEdit.RESERVA_FECHA_DEVOLUCION, 0L);
                                double precioTotal = extras.getDouble(ReservaEdit.RESERVA_PRECIO_TOTAL, 0.0);
                                int telefono = extras.getInt(ReservaEdit.RESERVA_TELEFONO, 0);
                                String nomCliente = extras.getString(ReservaEdit.RESERVA_NOM_CLIENTE, extras.getString(ReservaEdit.RESERVA_CUSTOMER));
                                Reserva r = new Reserva(fechaRecogida, fechaDevolucion, precioTotal, telefono, nomCliente);
                                int id = extras.getInt(ReservaEdit.RESERVA_ID);
                                r.setId(id);
                                mReservaViewModel.update(r);
                        } else {
                                Reserva r = new Reserva(extras.getString(ReservaEdit.RESERVA_CUSTOMER),
                                    extras.getString(ReservaEdit.RESERVA_START),
                                    extras.getString(ReservaEdit.RESERVA_END));
                                int id = extras.getInt(ReservaEdit.RESERVA_ID);
                                r.setId(id);
                                mReservaViewModel.update(r);
                        }
                    }
                }
            }
    );

}
