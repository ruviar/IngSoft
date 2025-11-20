package es.unizar.eina.notepad.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import es.unizar.eina.notepad.R;

public class InicioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        MaterialButton buttonQuad = findViewById(R.id.button_quad);
        MaterialButton buttonReserva = findViewById(R.id.button_reserva);

        buttonQuad.setOnClickListener(v -> {
            android.util.Log.d("InicioActivity", "button_quad clicked");
            Intent intent = new Intent(InicioActivity.this, QuadMenuActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        buttonReserva.setOnClickListener(v -> {
            android.util.Log.d("InicioActivity", "button_reserva clicked");
            Intent intent = new Intent(InicioActivity.this, ReservaMenuActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }
}
