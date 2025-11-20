package es.unizar.eina.notepad.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import es.unizar.eina.notepad.R;

public class ReservaMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservamenu);

        MaterialButton add = findViewById(R.id.button_add_reserva);
        MaterialButton view = findViewById(R.id.button_view_reservas);

        // Use ActivityResult to receive reserva details and insert via ViewModel
        androidx.lifecycle.ViewModelProvider provider = new androidx.lifecycle.ViewModelProvider(this);
        ReservaViewModel reservaViewModel = provider.get(ReservaViewModel.class);

        androidx.activity.result.ActivityResultLauncher<Intent> startCreate = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData()!=null && result.getData().getExtras()!=null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras.containsKey(ReservaEdit.RESERVA_NOM_CLIENTE) || extras.containsKey(ReservaEdit.RESERVA_PRECIO_TOTAL)) {
                            long fechaRecogida = 0L;
                            long fechaDevolucion = 0L;
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
                                es.unizar.eina.notepad.database.Reserva r = new es.unizar.eina.notepad.database.Reserva(fechaRecogida, fechaDevolucion, precioTotal, telefono, nomCliente);
                                reservaViewModel.insert(r);
                        } else {
                                es.unizar.eina.notepad.database.Reserva r = new es.unizar.eina.notepad.database.Reserva(extras.getString(ReservaEdit.RESERVA_CUSTOMER),
                                    extras.getString(ReservaEdit.RESERVA_START),
                                    extras.getString(ReservaEdit.RESERVA_END));
                                reservaViewModel.insert(r);
                        }
                    }
                }
        );

        add.setEnabled(true);
        view.setEnabled(true);
        add.setOnClickListener(v -> {
            android.util.Log.d("ReservaMenuActivity", "button_add_reserva clicked");
            startCreate.launch(new Intent(ReservaMenuActivity.this, ReservaEdit.class));
        });

        view.setOnClickListener(v -> {
            android.util.Log.d("ReservaMenuActivity", "button_view_reservas clicked");
            startActivity(new Intent(ReservaMenuActivity.this, ListaReservasActivity.class));
        });
    }
}
