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
 * Data Access Object (DAO) para la entidad Reserva.
 * <p>
 * Define las operaciones de acceso a datos para reservas en la base de datos.
 * Room genera automáticamente la implementación de esta interfaz.
 * </p>
 * <p>
 * Proporciona métodos CRUD básicos y consultas personalizadas para
 * gestionar la persistencia de reservas.
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see Reserva
 * @see ReservaQuadDao
 */
@Dao
public interface ReservaDao {
    /**
     * Inserta una nueva reserva en la base de datos.
     * <p>
     * Si ya existe una reserva con la misma clave primaria, se ignora
     * la inserción (estrategia IGNORE).
     * </p>
     *
     * @param reserva Reserva a insertar.
     * @return Identificador de la reserva insertada, o -1 si falló.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertReserva(Reserva reserva);

    /**
     * Actualiza una reserva existente en la base de datos.
     *
     * @param reserva Reserva con los datos actualizados.
     * @return Número de filas afectadas (1 si se actualizó, 0 si no se encontró).
     */
    @Update
    int updateReserva(Reserva reserva);

    /**
     * Elimina una reserva de la base de datos.
     * <p>
     * Nota: Las entradas relacionadas en ReservaQuad se eliminarán
     * automáticamente debido a la foreign key CASCADE.
     * </p>
     *
     * @param reserva Reserva a eliminar (debe tener un id válido).
     * @return Número de filas afectadas.
     */
    @Delete
    int deleteReserva(Reserva reserva);

    /**
     * Elimina todas las reservas de la base de datos.
     */
    @Query("DELETE FROM reserva")
    void deleteAll();

    /**
     * Obtiene todas las reservas ordenadas por fecha de recogida ascendente.
     * <p>
     * Devuelve un LiveData que permite observar cambios en la lista.
     * </p>
     *
     * @return LiveData con la lista de todas las reservas.
     */
    @Query("SELECT * FROM reserva ORDER BY fecha_recogida ASC")
    LiveData<List<Reserva>> getAllReservas();

    /**
     * Obtiene una reserva específica por su identificador.
     * <p>
     * Método síncrono que debe ejecutarse en un hilo de fondo.
     * </p>
     *
     * @param id Identificador de la reserva.
     * @return La reserva encontrada, o null si no existe.
     */
    @Query("SELECT * FROM reserva WHERE id = :id LIMIT 1")
    Reserva getReservaById(int id);
}
