package es.unizar.eina.notepad.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad que representa la relación muchos-a-muchos entre Reserva y Quad.
 * <p>
 * Esta clase implementa el patrón de tabla de asociación para relacionar
 * reservas con quads. Una reserva puede incluir múltiples quads, y un quad
 * puede estar en múltiples reservas (en diferentes períodos).
 * </p>
 * <p>
 * Además de las claves foráneas, almacena información adicional específica
 * de la relación, como el número de cascos necesarios para el quad en
 * esta reserva particular.
 * </p>
 * <p>
 * Las claves foráneas están configuradas con eliminación en cascada (CASCADE),
 * por lo que al eliminar una reserva o un quad, se eliminarán automáticamente
 * las entradas relacionadas en esta tabla.
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see Reserva
 * @see Quad
 * @see ReservaQuadDao
 */
@Entity(tableName = "reserva_quad",
        foreignKeys = {
            @ForeignKey(entity = Reserva.class,
                    parentColumns = "id",
                    childColumns = "reserva_id",
                    onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Quad.class,
                    parentColumns = "id",
                    childColumns = "quad_id",
                    onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("reserva_id"), @Index("quad_id")})
public class ReservaQuad {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "reserva_id")
    private int reservaId;

    @ColumnInfo(name = "quad_id")
    private int quadId;

    @ColumnInfo(name = "num_cascos")
    private int numCascos;

    /**
     * Construye una nueva asociación entre una reserva y un quad.
     *
     * @param reservaId Identificador de la reserva.
     * @param quadId Identificador del quad.
     * @param numCascos Número de cascos necesarios para este quad en esta reserva.
     */
    public ReservaQuad(int reservaId, int quadId, int numCascos) {
        this.reservaId = reservaId;
        this.quadId = quadId;
        this.numCascos = numCascos;
    }

    /**
     * Devuelve el identificador único de esta asociación.
     * @return Identificador de la asociación.
     */
    public int getId() { return id; }

    /**
     * Establece el identificador de esta asociación.
     * @param id Identificador de la asociación.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Devuelve el identificador de la reserva asociada.
     * @return Identificador de la reserva.
     */
    public int getReservaId() { return reservaId; }

    /**
     * Establece el identificador de la reserva.
     * @param reservaId Identificador de la reserva.
     */
    public void setReservaId(int reservaId) { this.reservaId = reservaId; }

    /**
     * Devuelve el identificador del quad asociado.
     * @return Identificador del quad.
     */
    public int getQuadId() { return quadId; }

    /**
     * Establece el identificador del quad.
     * @param quadId Identificador del quad.
     */
    public void setQuadId(int quadId) { this.quadId = quadId; }

    /**
     * Devuelve el número de cascos necesarios.
     * @return Número de cascos.
     */
    public int getNumCascos() { return numCascos; }

    /**
     * Establece el número de cascos necesarios.
     * @param numCascos Número de cascos.
     */
    public void setNumCascos(int numCascos) { this.numCascos = numCascos; }
}
