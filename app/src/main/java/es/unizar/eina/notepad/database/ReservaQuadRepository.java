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
 * Repositorio para gestionar el acceso a las asociaciones ReservaQuad.
 * <p>
 * Proporciona métodos para gestionar la relación muchos-a-muchos entre
 * reservas y quads. Permite vincular/desvincular quads de reservas y
 * consultar las asociaciones existentes.
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see ReservaQuad
 * @see ReservaQuadDao
 */
public class ReservaQuadRepository {

    private final ReservaQuadDao mReservaQuadDao;
    private final LiveData<List<ReservaQuad>> mAllReservaQuads;

    /** Timeout en milisegundos para operaciones de base de datos */
    private final long TIMEOUT = 15000;

    /**
     * Constructor del repositorio.
     *
     * @param application Contexto de la aplicación para acceder a la base de datos.
     */
    public ReservaQuadRepository(Application application) {
        AppRoomDatabase db = AppRoomDatabase.getDatabase(application);
        mReservaQuadDao = db.reservaQuadDao();
        mAllReservaQuads = mReservaQuadDao.getAllReservaQuads();
    }

    /**
     * Devuelve un LiveData con todas las asociaciones reserva-quad.
     *
     * @return LiveData con la lista observable de todas las asociaciones.
     */
    public LiveData<List<ReservaQuad>> getAllReservaQuads() {
        return mAllReservaQuads;
    }

    /**
     * Inserta una nueva asociación entre una reserva y un quad.
     *
     * @param reservaQuad Asociación a insertar.
     * @return Identificador de la asociación insertada, o -1 en caso de error.
     */
    public long insert(ReservaQuad reservaQuad) {
        Future<Long> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaQuadDao.insert(reservaQuad));
        try {
            long id = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
            Log.d("ReservaQuadRepository", "inserted reservaQuad id=" + id);
            return id;
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaQuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Actualiza una asociación existente.
     *
     * @param reservaQuad Asociación con los datos actualizados.
     * @return Número de filas afectadas, o -1 en caso de error.
     */
    public int update(ReservaQuad reservaQuad) {
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaQuadDao.update(reservaQuad));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaQuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Elimina una asociación.
     *
     * @param reservaQuad Asociación a eliminar.
     * @return Número de filas afectadas, o -1 en caso de error.
     */
    public int delete(ReservaQuad reservaQuad) {
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaQuadDao.delete(reservaQuad));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaQuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Obtiene todos los quads asociados a una reserva.
     *
     * @param reservaId Identificador de la reserva.
     * @return Lista de asociaciones para esa reserva, o null en caso de error.
     */
    public List<ReservaQuad> getQuadsByReservaId(int reservaId) {
        Future<List<ReservaQuad>> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaQuadDao.getQuadsByReservaId(reservaId));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaQuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return null;
        }
    }

    /**
     * Obtiene todas las reservas asociadas a un quad.
     *
     * @param quadId Identificador del quad.
     * @return Lista de asociaciones para ese quad, o null en caso de error.
     */
    public List<ReservaQuad> getReservasByQuadId(int quadId) {
        Future<List<ReservaQuad>> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaQuadDao.getReservasByQuadId(quadId));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaQuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return null;
        }
    }

    /**
     * Obtiene la asociación específica entre una reserva y un quad.
     *
     * @param reservaId Identificador de la reserva.
     * @param quadId Identificador del quad.
     * @return La asociación encontrada, o null si no existe o hay error.
     */
    public ReservaQuad getByReservaAndQuad(int reservaId, int quadId) {
        Future<ReservaQuad> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaQuadDao.getByReservaAndQuad(reservaId, quadId));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaQuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return null;
        }
    }

    /**
     * Obtiene una asociación por su identificador.
     *
     * @param id Identificador de la asociación.
     * @return La asociación encontrada, o null si no existe o hay error.
     */
    public ReservaQuad getById(int id) {
        Future<ReservaQuad> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaQuadDao.getById(id));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaQuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return null;
        }
    }

    /**
     * Elimina todas las asociaciones de una reserva.
     *
     * @param reservaId Identificador de la reserva.
     * @return Número de filas eliminadas, o -1 en caso de error.
     */
    public int deleteByReservaId(int reservaId) {
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaQuadDao.deleteByReservaId(reservaId));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaQuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Elimina todas las asociaciones de un quad.
     *
     * @param quadId Identificador del quad.
     * @return Número de filas eliminadas, o -1 en caso de error.
     */
    public int deleteByQuadId(int quadId) {
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> mReservaQuadDao.deleteByQuadId(quadId));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaQuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }
}
