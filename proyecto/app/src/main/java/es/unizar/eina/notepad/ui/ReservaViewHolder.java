package es.unizar.eina.notepad.ui;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import es.unizar.eina.notepad.R;

class ReservaViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

    private final TextView mCustomerView;
    private final TextView mDatesView;
    private final TextView mPrecioView;

    protected ReservaViewHolder(View itemView) {
        super(itemView);
        mCustomerView = itemView.findViewById(R.id.text_matricula);
        mDatesView = itemView.findViewById(R.id.text_tipo);
        mPrecioView = itemView.findViewById(R.id.text_precio);
        itemView.setOnCreateContextMenuListener(this);
    }

    public void bind(es.unizar.eina.notepad.database.Reserva reserva) {
        if (reserva == null) return;
        mCustomerView.setText(reserva.getCustomer() == null ? "" : reserva.getCustomer());
        String start = "";
        String end = "";
        try {
            start = reserva.getStartDate() == null ? "" : reserva.getStartDate();
            end = reserva.getEndDate() == null ? "" : reserva.getEndDate();
        } catch (Exception e) {
            android.util.Log.e("ReservaViewHolder", "Error formatting dates", e);
        }
        String dates = start + " - " + end;
        mDatesView.setText(dates);
        try {
            mPrecioView.setText(String.format("%.2f €", reserva.getPrecioTotal()));
        } catch (Exception e) {
            mPrecioView.setText("0.00 €");
        }
    }

    /** Permite actualizar el precio mostrado en la fila (desde el adaptador en background). */
    public void setPrecio(double precio) {
        mPrecioView.setText(String.format("%.2f €", precio));
    }
    static ReservaViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new ReservaViewHolder(view);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, Bookuad.DELETE_ID, Menu.NONE, R.string.menu_delete);
        menu.add(Menu.NONE, Bookuad.EDIT_ID, Menu.NONE, R.string.menu_edit);
    }
}
