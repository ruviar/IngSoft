package es.unizar.eina.notepad.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad que representa un Quad (vehículo) en el sistema de alquiler.
 * <p>
 * Esta clase modela un quad disponible para alquiler con sus características
 * principales: tipo (uniplaza o biplaza), precio por día, matrícula única
 * y descripción opcional.
 * </p>
 * <p>
 * La clase utiliza anotaciones de Room para persistencia en base de datos SQLite.
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see QuadDao
 * @see Reserva
 */
@Entity(tableName = "quad")
public class Quad {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "tipo")
    private Tipo tipo;

    @ColumnInfo(name = "precio")
    private int precio;

    @NonNull
    @ColumnInfo(name = "matricula")
    private String matricula;

    @ColumnInfo(name = "descripcion")
    private String descripcion;

    /**
     * Construye un nuevo Quad con todos sus atributos.
     *
     * @param tipo Tipo de quad (UNIPLAZA o BIPLAZA).
     * @param precio Precio de alquiler por día en euros.
     * @param matricula Matrícula única del quad. No puede ser nula.
     * @param descripcion Descripción opcional del quad.
     */
    public Quad(Tipo tipo, int precio, @NonNull String matricula, String descripcion) {
        this.tipo = tipo;
        this.precio = precio;
        this.matricula = matricula;
        this.descripcion = descripcion;
    }

    /**
     * Constructor de compatibilidad con versiones previas de la aplicación.
     * <p>
     * Permite crear un Quad utilizando la interfaz antigua basada en title/body.
     * Se utiliza para migración de datos o compatibilidad con código legacy.
     * </p>
     *
     * @param title Título del quad (se mapea a matrícula).
     * @param body Cuerpo descriptivo (se mapea a descripción).
     */
    public Quad(@NonNull String title, String body) {
        this.tipo = Tipo.UNIPLAZA;
        this.precio = 0;
        this.matricula = title != null ? title : "";
        this.descripcion = body;
    }

    /**
     * Devuelve el identificador único del quad en la base de datos.
     * @return Identificador del quad.
     */
    public int getId(){ return this.id; }

    /**
     * Establece el identificador único del quad.
     * @param id Identificador del quad.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Devuelve el tipo de quad.
     * @return Tipo del quad (UNIPLAZA o BIPLAZA).
     */
    public Tipo getTipo() { return tipo; }

    /**
     * Establece el tipo de quad.
     * @param tipo Tipo del quad (UNIPLAZA o BIPLAZA).
     */
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    /**
     * Devuelve el precio de alquiler por día.
     * @return Precio en euros.
     */
    public int getPrecio() { return precio; }

    /**
     * Establece el precio de alquiler por día.
     * @param precio Precio en euros.
     */
    public void setPrecio(int precio) { this.precio = precio; }

    /**
     * Devuelve la matrícula del quad.
     * @return Matrícula del quad.
     */
    public String getMatricula() { return matricula; }

    /**
     * Establece la matrícula del quad.
     * @param matricula Matrícula del quad.
     */
    public void setMatricula(String matricula) { this.matricula = matricula; }

    /**
     * Devuelve la descripción del quad.
     * @return Descripción del quad.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Establece la descripción del quad.
     * @param descripcion Descripción del quad.
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Enumerado que define los tipos de quad disponibles.
     * <p>
     * UNIPLAZA: Quad para una persona.<br>
     * BIPLAZA: Quad para dos personas.
     * </p>
     */
    public enum Tipo {
        /** Quad de una plaza */
        UNIPLAZA,
        /** Quad de dos plazas */
        BIPLAZA
    }

}
