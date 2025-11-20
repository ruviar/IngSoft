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
 * Repositorio que gestiona el acceso a la fuente de datos de Reservas.
 * <p>
 * Actúa como intermediario entre el ViewModel y el DAO, proporcionando
 * una API limpia para operaciones CRUD sobre reservas. Gestiona la
 * ejecución de operaciones en hilos de fondo y el manejo de errores.
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see Reserva
 * @see ReservaDao
 */
public class ReservaRepository {

    private final ReservaDao mReservaDao;
    private final LiveData<List<Reserva>> mAllReservas;

    /** Timeout en milisegundos para operaciones de base de datos */
    private final long TIMEOUT = 15000;

    /**
     * Constructor del repositorio.
     *
     * @param application Contexto de la aplicación para acceder a la base de datos.
     */
    public ReservaRepository(Application application) {
        AppRoomDatabase db = AppRoomDatabase.getDatabase(application);
        mReservaDao = db.reservaDao();
        mAllReservas = mReservaDao.getAllReservas();
    }
    /**
     * Devuelve un LiveData con todas las reservas.
     *
     * @return LiveData con la lista observable de todas las reservas.
     */
    public LiveData<List<Reserva>> getAllReservas() { return mAllReservas; }

    /**
     * Inserta una nueva reserva en la base de datos.
     *
     * @param reserva Reserva a insertar.
     * @return Identificador de la reserva insertada (&gt; 0) si tuvo éxito, -1 en caso de error.
     */
    public long insert(Reserva reserva) {
        Future<Long> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaDao.insertReserva(reserva));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Actualiza una reserva existente.
     *
     * @param reserva Reserva con los datos actualizados.
     * @return 1 si se actualizó, 0 si no se encontró, -1 en caso de error.
     */
    public int update(Reserva reserva) {
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaDao.updateReserva(reserva));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Elimina una reserva de la base de datos.
     * <p>
     * Las asociaciones en ReservaQuad se eliminarán automáticamente (CASCADE).
     * </p>
     *
     * @param reserva Reserva a eliminar.
     * @return 1 si se eliminó, 0 si no se encontró, -1 en caso de error.
     */
    public int delete(Reserva reserva) {
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaDao.deleteReserva(reserva));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Obtiene una reserva por su identificador.
     *
     * @param id Identificador de la reserva.
     * @return La reserva encontrada, o null si no existe o hay error.
     */
    public Reserva getReservaById(int id) {
        Future<Reserva> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaDao.getReservaById(id));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return null;
        }
    }
}
