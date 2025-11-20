package es.unizar.eina.notepad.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object (DAO) para la entidad ReservaQuad.
 * <p>
 * Gestiona las operaciones de acceso a datos para la tabla de asociación
 * muchos-a-muchos entre Reserva y Quad.
 * </p>
 * <p>
 * Proporciona métodos para vincular y desvincular quads de reservas,
 * así como para consultar las asociaciones existentes.
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see ReservaQuad
 * @see Reserva
 * @see Quad
 */
@Dao
public interface ReservaQuadDao {
    /**
     * Inserta una nueva asociación entre una reserva y un quad.
     *
     * @param reservaQuad Asociación a insertar.
     * @return Identificador de la asociación insertada.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(ReservaQuad reservaQuad);

    /**
     * Actualiza una asociación existente.
     *
     * @param reservaQuad Asociación con los datos actualizados.
     * @return Número de filas afectadas.
     */
    @Update
    int update(ReservaQuad reservaQuad);

    /**
     * Elimina una asociación entre reserva y quad.
     *
     * @param reservaQuad Asociación a eliminar.
     * @return Número de filas afectadas.
     */
    @Delete
    int delete(ReservaQuad reservaQuad);

    /**
     * Elimina todas las asociaciones de la tabla.
     */
    @Query("DELETE FROM reserva_quad")
    void deleteAll();

    /**
     * Obtiene todas las asociaciones reserva-quad.
     *
     * @return LiveData con la lista de todas las asociaciones.
     */
    @Query("SELECT * FROM reserva_quad ORDER BY id ASC")
    LiveData<List<ReservaQuad>> getAllReservaQuads();

    /**
     * Obtiene todos los quads asociados a una reserva específica.
     * <p>
     * Método síncrono que debe ejecutarse en un hilo de fondo.
     * </p>
     *
     * @param reservaId Identificador de la reserva.
     * @return Lista de asociaciones (ReservaQuad) para esa reserva.
     */
    @Query("SELECT * FROM reserva_quad WHERE reserva_id = :reservaId")
    List<ReservaQuad> getQuadsByReservaId(int reservaId);

    /**
     * Obtiene todas las reservas asociadas a un quad específico.
     * <p>
     * Método síncrono que debe ejecutarse en un hilo de fondo.
     * </p>
     *
     * @param quadId Identificador del quad.
     * @return Lista de asociaciones (ReservaQuad) para ese quad.
     */
    @Query("SELECT * FROM reserva_quad WHERE quad_id = :quadId")
    List<ReservaQuad> getReservasByQuadId(int quadId);

    /**
     * Obtiene la asociación específica entre una reserva y un quad.
     *
     * @param reservaId Identificador de la reserva.
     * @param quadId Identificador del quad.
     * @return La asociación encontrada, o null si no existe.
     */
    @Query("SELECT * FROM reserva_quad WHERE reserva_id = :reservaId AND quad_id = :quadId LIMIT 1")
    ReservaQuad getByReservaAndQuad(int reservaId, int quadId);

    /**
     * Obtiene una asociación por su identificador.
     *
     * @param id Identificador de la asociación.
     * @return La asociación encontrada, o null si no existe.
     */
    @Query("SELECT * FROM reserva_quad WHERE id = :id LIMIT 1")
    ReservaQuad getById(int id);

    /**
     * Elimina todas las asociaciones de una reserva específica.
     * <p>
     * Útil al eliminar o modificar completamente una reserva.
     * </p>
     *
     * @param reservaId Identificador de la reserva.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM reserva_quad WHERE reserva_id = :reservaId")
    int deleteByReservaId(int reservaId);

    /**
     * Elimina todas las asociaciones de un quad específico.
     *
     * @param quadId Identificador del quad.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM reserva_quad WHERE quad_id = :quadId")
    int deleteByQuadId(int quadId);
}
