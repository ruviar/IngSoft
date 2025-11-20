package es.unizar.eina.notepad.ui;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import es.unizar.eina.notepad.database.Quad;

/**
 * Adaptador para mostrar la lista de quads en un RecyclerView.
 * <p>
 * Extiende ListAdapter que utiliza DiffUtil para calcular eficientemente
 * las diferencias entre listas y animar los cambios automáticamente.
 * </p>
 * <p>
 * Funcionalidades:
 * - Click largo: Abre menú contextual para editar/eliminar
 * - Click simple: Navega a ThisQuadActivity con los detalles del quad
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see QuadViewHolder
 * @see Quad
 * @see Bookuad
 */
public class QuadListAdapter extends ListAdapter<Quad, QuadViewHolder> {
    /** Posición del ítem seleccionado actualmente */
    private int position;

    /**
     * Devuelve la posición del ítem seleccionado.
     * @return Posición en el adaptador.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Establece la posición del ítem seleccionado.
     * @param position Posición en el adaptador.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Constructor del adaptador.
     * @param diffCallback Callback para calcular diferencias entre listas.
     */
    public QuadListAdapter(@NonNull DiffUtil.ItemCallback<Quad> diffCallback) {
        super(diffCallback);
    }

    /**
     * Crea un nuevo ViewHolder.
     * @param parent ViewGroup padre.
     * @param viewType Tipo de vista.
     * @return ViewHolder creado.
     */
    @Override
    public QuadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return QuadViewHolder.create(parent);
    }

    /**
     * Devuelve el quad en la posición actual.
     * @return Quad seleccionado.
     */
    public Quad getCurrent() {
        return getItem(getPosition());
    }

    /**
     * Vincula los datos de un quad con su ViewHolder.
     * <p>
     * Configura los listeners para click simple y largo.
     * </p>
     *
     * @param holder ViewHolder a vincular.
     * @param position Posición en la lista.
     */
    @Override
    public void onBindViewHolder(QuadViewHolder holder, int position) {
        Quad current = getItem(position);
        holder.bind(current);

        holder.itemView.setOnLongClickListener(v -> {
            setPosition(holder.getAdapterPosition());
            return false;
        });

        // click simple: abrir pantalla ThisQuadActivity mostrando la matrícula
        holder.itemView.setOnClickListener(v -> {
            android.content.Context ctx = holder.itemView.getContext();
            android.content.Intent intent = new android.content.Intent(ctx, ThisQuadActivity.class);
            intent.putExtra(ThisQuadActivity.EXTRA_QUAD_ID, current.getId());
            ctx.startActivity(intent);
        });
    }

    /**
     * Callback para calcular diferencias entre quads.
     * <p>
     * DiffUtil utiliza esta clase para determinar qué ítems han cambiado
     * y optimizar las actualizaciones del RecyclerView.
     * </p>
     */
    static class QuadDiff extends DiffUtil.ItemCallback<Quad> {

        /**
         * Comprueba si dos quads representan el mismo ítem.
         * @param oldItem Quad antiguo.
         * @param newItem Quad nuevo.
         * @return true si tienen el mismo ID.
         */
        @Override
        public boolean areItemsTheSame(@NonNull Quad oldItem, @NonNull Quad newItem) {
            return oldItem.getId() == newItem.getId();
        }

        /**
         * Comprueba si dos quads tienen el mismo contenido.
         * @param oldItem Quad antiguo.
         * @param newItem Quad nuevo.
         * @return true si matrícula, tipo y precio son iguales.
         */
        @Override
        public boolean areContentsTheSame(@NonNull Quad oldItem, @NonNull Quad newItem) {
            String oMat = oldItem.getMatricula() == null ? "" : oldItem.getMatricula();
            String nMat = newItem.getMatricula() == null ? "" : newItem.getMatricula();
            String oTipo = oldItem.getTipo() == null ? "" : oldItem.getTipo().name();
            String nTipo = newItem.getTipo() == null ? "" : newItem.getTipo().name();
            int oPrecio = oldItem.getPrecio();
            int nPrecio = newItem.getPrecio();
            return oMat.equals(nMat) && oTipo.equals(nTipo) && oPrecio == nPrecio;
        }
    }
}
