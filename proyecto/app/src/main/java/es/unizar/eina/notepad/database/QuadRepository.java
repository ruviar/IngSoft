package es.unizar.eina.notepad.database;


import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Repositorio que gestiona el acceso a la fuente de datos de Quads.
 * <p>
 * Esta clase actúa como intermediaria entre el ViewModel y el DAO,
 * proporcionando una API limpia para acceder a los datos de quads.
 * Gestiona la ejecución de operaciones de base de datos en hilos
 * de fondo y maneja los errores de sincronización.
 * </p>
 * <p>
 * Implementa el patrón Repository del patrón arquitectónico MVVM,
 * ocultando los detalles de implementación de la persistencia de datos.
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see Quad
 * @see QuadDao
 * @see QuadViewModel
 * @see AppRoomDatabase
 */
public class QuadRepository {

    private final QuadDao mQuadDao;
    private final LiveData<List<Quad>> mAllQuads;

    /** Timeout en milisegundos para operaciones de base de datos */
    private final long TIMEOUT = 15000;

    /**
     * Constructor del repositorio.
     * <p>
     * Obtiene la instancia de la base de datos y el DAO correspondiente.
     * Inicializa el LiveData con todos los quads.
     * </p>
     *
     * @param application Contexto de la aplicación para acceder a la base de datos.
     */
    public QuadRepository(Application application) {
        AppRoomDatabase db = AppRoomDatabase.getDatabase(application);
        mQuadDao = db.quadDao();
        mAllQuads = mQuadDao.getAllQuads();
    }

    /**
     * Devuelve un objeto LiveData con todos los quads.
     * <p>
     * Room ejecuta la consulta en un hilo separado automáticamente.
     * El LiveData notifica a los observadores cuando los datos cambian.
     * </p>
     *
     * @return LiveData con la lista observable de todos los quads.
     */
    public LiveData<List<Quad>> getAllQuads() { return mAllQuads; }

    /**
     * Inserta un nuevo quad en la base de datos.
     * <p>
     * La operación se ejecuta de forma asíncrona en un hilo de fondo
     * para no bloquear el hilo principal de la UI.
     * </p>
     *
     * @param quad Quad a insertar. Debe tener una matrícula no nula y no vacía.
     * @return Identificador del quad insertado (&gt; 0) si tuvo éxito, -1 en caso de error.
     */
    public long insert(Quad quad) {
        /* Para que la App funcione correctamente y no lance una excepción, la modificación de la
         * base de datos se debe lanzar en un hilo de ejecución separado
         * (databaseWriteExecutor.submit). Para poder sincronizar la recuperación del resultado
         * devuelto por la base de datos, se puede utilizar un Future.
         */
        Future<Long> future = AppRoomDatabase.databaseWriteExecutor.submit(
            () -> mQuadDao.insertQuad(quad));
        try {
            long id = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
            Log.d("QuadRepository", "inserted quad id=" + id + " matricula=" + (quad.getMatricula()!=null?quad.getMatricula():""));
            return id;
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("QuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Actualiza un quad existente en la base de datos.
     * <p>
     * El quad debe tener un id válido que corresponda a un registro existente.
     * La operación se ejecuta de forma asíncrona.
     * </p>
     *
     * @param quad Quad con los datos actualizados. Debe tener un id válido (&gt; 0).
     * @return 1 si se actualizó correctamente, 0 si no se encontró el quad, -1 en caso de error.
     */
    public int update(Quad quad) {
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(
            () -> mQuadDao.updateQuad(quad));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("QuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }


    /**
     * Elimina un quad de la base de datos.
     * <p>
     * Atención: Si el quad tiene reservas asociadas, también se eliminarán
     * las entradas en ReservaQuad debido a la configuración CASCADE de
     * las foreign keys.
     * </p>
     *
     * @param quad Quad a eliminar. Debe tener un id válido (&gt; 0).
     * @return 1 si se eliminó correctamente, 0 si no se encontró, -1 en caso de error.
     */
    public int delete(Quad quad) {
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(
            () -> mQuadDao.deleteQuad(quad));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("QuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Obtiene un quad por su identificador.
     * <p>
     * Este método es síncrono y se ejecuta en un hilo de fondo.
     * Espera hasta obtener el resultado o alcanzar el timeout.
     * </p>
     *
     * @param id Identificador del quad.
     * @return El quad encontrado, o null si no existe o hay error.
     */
    public Quad getQuadById(int id) {
        Future<Quad> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mQuadDao.getQuadById(id));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("QuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return null;
        }
    }
}
