package es.unizar.eina.notepad.ui;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import es.unizar.eina.notepad.database.Quad;
import es.unizar.eina.notepad.database.QuadRepository;

/**
 * ViewModel para gestionar los datos de quads en la capa de presentación.
 * <p>
 * Implementa el patrón MVVM (Model-View-ViewModel) proporcionando una
 * abstracción entre la UI y el repositorio de datos. Sobrevive a cambios
 * de configuración (como rotaciones de pantalla).
 * </p>
 * <p>
 * Mantiene una referencia al repositorio y expone LiveData que la UI
 * puede observar para actualizaciones automáticas.
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see QuadRepository
 * @see Quad
 * @see Bookuad
 */
public class QuadViewModel extends AndroidViewModel {

    private QuadRepository mRepository;

    private final LiveData<List<Quad>> mAllQuads;

    /**
     * Constructor del ViewModel.
     * <p>
     * Inicializa el repositorio y carga el LiveData con todos los quads.
     * </p>
     *
     * @param application Contexto de la aplicación.
     */
    public QuadViewModel(Application application) {
        super(application);
        mRepository = new QuadRepository(application);
        mAllQuads = mRepository.getAllQuads();
    }

    /**
     * Devuelve el LiveData con todos los quads.
     * @return LiveData observable con la lista de quads.
     */
    LiveData<List<Quad>> getAllQuads() { return mAllQuads; }

    /**
     * Inserta un nuevo quad.
     * @param quad Quad a insertar.
     */
    public void insert(Quad quad) { mRepository.insert(quad); }

    /**
     * Actualiza un quad existente.
     * @param quad Quad con los datos actualizados.
     */
    public void update(Quad quad) { mRepository.update(quad); }

    /**
     * Elimina un quad.
     * @param quad Quad a eliminar.
     */
    public void delete(Quad quad) { mRepository.delete(quad); }
}
