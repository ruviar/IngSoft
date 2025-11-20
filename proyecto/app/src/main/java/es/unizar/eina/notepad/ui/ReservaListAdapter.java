package es.unizar.eina.notepad.ui;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import es.unizar.eina.notepad.database.Quad;
import es.unizar.eina.notepad.database.QuadRepository;
import es.unizar.eina.notepad.database.Reserva;

public class ReservaListAdapter extends ListAdapter<Reserva, ReservaViewHolder> {
    private int position;
    private final QuadRepository mQuadRepository;

    public ReservaListAdapter(@NonNull DiffUtil.ItemCallback<Reserva> diffCallback, QuadRepository quadRepository) {
        super(diffCallback);
        this.mQuadRepository = quadRepository;
    }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    @Override
    public ReservaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ReservaViewHolder.create(parent);
    }

    public Reserva getCurrent() { return getItem(getPosition()); }

    @Override
    public void onBindViewHolder(ReservaViewHolder holder, int position) {
        Reserva current = getItem(position);
        holder.bind(current);
        holder.itemView.setOnLongClickListener(v -> { setPosition(holder.getAdapterPosition()); return false; });
        holder.itemView.setOnClickListener(v -> {
            // Lanzar pantalla de detalle de reserva con datos prellenados
            android.content.Context ctx = holder.itemView.getContext();
            android.content.Intent intent = new android.content.Intent(ctx, ThisReservaActivity.class);
            intent.putExtra(ThisReservaActivity.EXTRA_RESERVA_ID, current.getId());
            intent.putExtra(ReservaEdit.RESERVA_NOM_CLIENTE, current.getNomCliente());
            intent.putExtra(ReservaEdit.RESERVA_PRECIO_TOTAL, current.getPrecioTotal());
            intent.putExtra(ReservaEdit.RESERVA_FECHA_RECOGIDA, current.getFechaRecogida());
            intent.putExtra(ReservaEdit.RESERVA_FECHA_DEVOLUCION, current.getFechaDevolucion());
            // La relación con quads ahora es N:N; enviar sólo los campos de Reserva
            intent.putExtra(ReservaEdit.RESERVA_TELEFONO, current.getTelefono());
            intent.putExtra(ReservaEdit.RESERVA_ID, current.getId());
            ctx.startActivity(intent);
        });

        // Precio mostrado = precio total almacenado en la reserva
        holder.setPrecio(current.getPrecioTotal());
    }

    static class ReservaDiff extends DiffUtil.ItemCallback<Reserva> {
        @Override
        public boolean areItemsTheSame(@NonNull Reserva oldItem, @NonNull Reserva newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Reserva oldItem, @NonNull Reserva newItem) {
            String oCust = oldItem.getCustomer() == null ? "" : oldItem.getCustomer();
            String nCust = newItem.getCustomer() == null ? "" : newItem.getCustomer();
            String oStart = oldItem.getStartDate() == null ? "" : oldItem.getStartDate();
            String nStart = newItem.getStartDate() == null ? "" : newItem.getStartDate();
            String oEnd = oldItem.getEndDate() == null ? "" : oldItem.getEndDate();
            String nEnd = newItem.getEndDate() == null ? "" : newItem.getEndDate();
            double oPrecio = oldItem.getPrecioTotal();
            double nPrecio = newItem.getPrecioTotal();
            return oCust.equals(nCust) && oStart.equals(nStart) && oEnd.equals(nEnd) && Double.compare(oPrecio, nPrecio) == 0;
        }
    }
}
