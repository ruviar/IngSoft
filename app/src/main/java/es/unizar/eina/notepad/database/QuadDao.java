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
 * Data Access Object (DAO) para la entidad Quad.
 * <p>
 * Define las operaciones de acceso a datos para quads en la base de datos.
 * Room genera automáticamente la implementación de esta interfaz.
 * </p>
 * <p>
 * Proporciona métodos CRUD básicos (Create, Read, Update, Delete) y
 * consultas personalizadas para gestionar la persistencia de quads.
 * </p>
 *
 * @author Estudiante IS - Universidad de Zaragoza
 * @version 1.0
 * @see Quad
 * @see QuadRepository
 */
@Dao
public interface QuadDao {
    /**
     * Inserta un nuevo quad en la base de datos.
     * <p>
     * Si ya existe un quad con la misma clave primaria, se ignora
     * la inserción (estrategia IGNORE).
     * </p>
     *
     * @param quad Quad a insertar.
     * @return Identificador del quad insertado, o -1 si falló la inserción.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertQuad(Quad quad);

    /**
     * Actualiza un quad existente en la base de datos.
     * <p>
     * El quad debe tener un id válido que corresponda a un registro existente.
     * </p>
     *
     * @param quad Quad con los datos actualizados.
     * @return Número de filas afectadas (1 si se actualizó, 0 si no se encontró).
     */
    @Update
    int updateQuad(Quad quad);

    /**
     * Elimina un quad de la base de datos.
     *
     * @param quad Quad a eliminar (debe tener un id válido).
     * @return Número de filas afectadas (1 si se eliminó, 0 si no se encontró).
     */
    @Delete
    int deleteQuad(Quad quad);

    /**
     * Elimina todos los quads de la base de datos.
     * <p>
     * Cuidado: Esta operación es irreversible.
     * </p>
     */
    @Query("DELETE FROM quad")
    void deleteAll();

    /**
     * Obtiene todos los quads ordenados alfabéticamente por matrícula.
     * <p>
     * Devuelve un LiveData que permite observar cambios en la lista de quads.
     * La lista se actualiza automáticamente cuando hay cambios en la base de datos.
     * </p>
     *
     * @return LiveData con la lista de todos los quads.
     */
    @Query("SELECT * FROM quad ORDER BY matricula ASC")
    LiveData<List<Quad>> getAllQuads();

    /**
     * Obtiene un quad específico por su identificador.
     * <p>
     * Este método es síncrono y debe ejecutarse en un hilo de fondo.
     * </p>
     *
     * @param id Identificador del quad.
     * @return El quad encontrado, o null si no existe.
     */
    @Query("SELECT * FROM quad WHERE id = :id")
    Quad getQuadById(int id);
}

