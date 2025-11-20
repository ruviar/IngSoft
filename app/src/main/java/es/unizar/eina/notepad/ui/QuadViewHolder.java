package es.unizar.eina.notepad.ui;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import es.unizar.eina.notepad.R;

/**
 * ViewHolder para mostrar un quad en el RecyclerView.
 * <p>
 * Mantiene referencias a las vistas de un ítem de la lista y
 * proporciona métodos para vincular datos del quad con las vistas.
 * </p>
 * <p>
 * Implementa View.OnCreateContextMenuListener para mostrar un menú
 * contextual con opciones de edición y eliminación.
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see Quad
 * @see QuadListAdapter
 */
class QuadViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
    private final TextView mIdView;
    private final TextView mMatriculaView;
    private final TextView mTipoView;
    private final TextView mPrecioView;

    /**
     * Constructor del ViewHolder.
     * <p>
     * Inicializa las referencias a las vistas y configura el listener
     * para el menú contextual.
     * </p>
     *
     * @param itemView Vista raíz del ítem.
     */
    protected QuadViewHolder(View itemView) {
        super(itemView);
        mIdView = itemView.findViewById(R.id.text_id);
        mMatriculaView = itemView.findViewById(R.id.text_matricula);
        mTipoView = itemView.findViewById(R.id.text_tipo);
        mPrecioView = itemView.findViewById(R.id.text_precio);

        itemView.setOnCreateContextMenuListener(this);
    }

    /**
     * Vincula los datos de un quad con las vistas.
     * <p>
     * Actualiza los TextViews con la información del quad:
     * ID, matrícula, tipo y precio formateado.
     * </p>
     *
     * @param quad Quad cuyos datos se mostrarán.
     */
    public void bind(es.unizar.eina.notepad.database.Quad quad) {
        if (quad == null) return;
        try {
            mIdView.setText(String.valueOf(quad.getId()));
            mMatriculaView.setText(quad.getMatricula() == null ? "" : quad.getMatricula());
            mTipoView.setText(quad.getTipo() == null ? "" : quad.getTipo().name());
            mPrecioView.setText(String.format("%d €", quad.getPrecio()));
        } catch (Exception e) {
            android.util.Log.e("QuadViewHolder", "Error binding quad", e);
        }
    }

    /**
     * Método factory para crear un ViewHolder.
     * <p>
     * Infla el layout del ítem y crea una nueva instancia de QuadViewHolder.
     * </p>
     *
     * @param parent ViewGroup padre.
     * @return Nueva instancia de QuadViewHolder.
     */
    static QuadViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new QuadViewHolder(view);
    }


    /**
     * Crea el menú contextual para el quad.
     * <p>
     * Añade opciones para eliminar y editar el quad.
     * </p>
     *
     * @param menu Menú contextual a crear.
     * @param v Vista que activó el menú.
     * @param menuInfo Información adicional del menú.
     */
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, Bookuad.DELETE_ID, Menu.NONE, R.string.menu_delete);
        menu.add(Menu.NONE, Bookuad.EDIT_ID, Menu.NONE, R.string.menu_edit);
    }


}
