package es.unizar.eina.notepad.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad que representa una reserva de alquiler de quads.
 * <p>
 * Esta clase modela una reserva realizada por un cliente, incluyendo
 * fechas de recogida y devolución, precio total, teléfono de contacto
 * y nombre del cliente.
 * </p>
 * <p>
 * Una reserva puede estar asociada a uno o más quads mediante la
 * relación muchos-a-muchos gestionada por {@link ReservaQuad}.
 * Las fechas se almacenan como timestamps (long) en milisegundos.
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see ReservaDao
 * @see Quad
 * @see ReservaQuad
 */
@Entity(tableName = "reserva")
public class Reserva {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "fecha_recogida")
    private long fechaRecogida;

    @ColumnInfo(name = "fecha_devolucion")
    private long fechaDevolucion;

    @ColumnInfo(name = "precio_total")
    private double precioTotal;

    @ColumnInfo(name = "telefono")
    private int telefono;

    @NonNull
    @ColumnInfo(name = "nom_cliente")
    private String nomCliente;

    /**
     * Construye una nueva reserva con todos sus atributos.
     *
     * @param fechaRecogida Fecha y hora de recogida en milisegundos (timestamp).
     * @param fechaDevolucion Fecha y hora de devolución en milisegundos (timestamp).
     * @param precioTotal Precio total de la reserva en euros.
     * @param telefono Teléfono de contacto del cliente.
     * @param nomCliente Nombre del cliente. No puede ser nulo.
     */
    public Reserva(long fechaRecogida, long fechaDevolucion, double precioTotal,
                   int telefono, @NonNull String nomCliente) {
        this.fechaRecogida = fechaRecogida;
        this.fechaDevolucion = fechaDevolucion;
        this.precioTotal = precioTotal;
        this.telefono = telefono;
        this.nomCliente = nomCliente;
    }

    /**
     * Constructor de compatibilidad con versiones previas de la aplicación.
     * <p>
     * Permite crear una Reserva utilizando la interfaz antigua basada en
     * customer/startDate/endDate con fechas en formato String.
     * Se utiliza para migración de datos o compatibilidad con código legacy.
     * </p>
     *
     * @param customer Nombre del cliente (se mapea a nomCliente).
     * @param startDate Fecha de inicio en formato String.
     * @param endDate Fecha de fin en formato String.
     */
    public Reserva(@NonNull String customer, String startDate, String endDate) {
        this.fechaRecogida = parseDateString(startDate);
        this.fechaDevolucion = parseDateString(endDate);
        this.precioTotal = 0.0;
        this.telefono = 0;
        this.nomCliente = customer;
    }

    /**
     * Devuelve el identificador único de la reserva en la base de datos.
     * @return Identificador de la reserva.
     */
    public int getId(){ return id; }

    /**
     * Establece el identificador único de la reserva.
     * @param id Identificador de la reserva.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Devuelve la fecha de recogida en milisegundos.
     * @return Timestamp de la fecha de recogida.
     */
    public long getFechaRecogida() { return fechaRecogida; }

    /**
     * Devuelve la fecha de devolución en milisegundos.
     * @return Timestamp de la fecha de devolución.
     */
    public long getFechaDevolucion() { return fechaDevolucion; }

    /**
     * Devuelve el precio total de la reserva.
     * @return Precio total en euros.
     */
    public double getPrecioTotal() { return precioTotal; }

    /**
     * Devuelve el teléfono de contacto del cliente.
     * @return Número de teléfono.
     */
    public int getTelefono() { return telefono; }

    /**
     * Devuelve el nombre del cliente.
     * @return Nombre del cliente.
     */
    public String getNomCliente() { return nomCliente; }

    /**
     * Método de compatibilidad: devuelve el nombre del cliente.
     * @return Nombre del cliente.
     * @deprecated Usar {@link #getNomCliente()} en su lugar.
     */
    public String getCustomer() { return nomCliente; }

    /**
     * Método de compatibilidad: devuelve la fecha de inicio formateada.
     * @return Fecha de inicio en formato legible, o null si no está definida.
     * @deprecated Usar {@link #getFechaRecogida()} en su lugar.
     */
    public String getStartDate() {
        return fechaRecogida <= 0 ? null : formatDate(fechaRecogida);
    }

    /**
     * Método de compatibilidad: devuelve la fecha de fin formateada.
     * @return Fecha de fin en formato legible, o null si no está definida.
     * @deprecated Usar {@link #getFechaDevolucion()} en su lugar.
     */
    public String getEndDate() {
        return fechaDevolucion <= 0 ? null : formatDate(fechaDevolucion);
    }

    /**
     * Método de compatibilidad: devuelve la fecha de inicio en milisegundos.
     * @return Timestamp de la fecha de inicio.
     * @deprecated Usar {@link #getFechaRecogida()} en su lugar.
     */
    public long getStartDateLong() { return fechaRecogida; }

    /**
     * Método de compatibilidad: devuelve la fecha de fin en milisegundos.
     * @return Timestamp de la fecha de fin.
     * @deprecated Usar {@link #getFechaDevolucion()} en su lugar.
     */
    public long getEndDateLong() { return fechaDevolucion; }

    /**
     * Establece la fecha de recogida.
     * @param fechaRecogida Timestamp de la fecha de recogida en milisegundos.
     */
    public void setFechaRecogida(long fechaRecogida) { this.fechaRecogida = fechaRecogida; }

    /**
     * Establece la fecha de devolución.
     * @param fechaDevolucion Timestamp de la fecha de devolución en milisegundos.
     */
    public void setFechaDevolucion(long fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    /**
     * Establece el precio total de la reserva.
     * @param precioTotal Precio total en euros.
     */
    public void setPrecioTotal(double precioTotal) { this.precioTotal = precioTotal; }

    /**
     * Establece el teléfono de contacto.
     * @param telefono Número de teléfono del cliente.
     */
    public void setTelefono(int telefono) { this.telefono = telefono; }

    /**
     * Establece el nombre del cliente.
     * @param nomCliente Nombre del cliente.
     */
    public void setNomCliente(String nomCliente) { this.nomCliente = nomCliente; }

    /**
     * Método de compatibilidad: establece el nombre del cliente.
     * @param customer Nombre del cliente.
     * @deprecated Usar {@link #setNomCliente(String)} en su lugar.
     */
    public void setCustomer(String customer) { this.nomCliente = customer; }

    /**
     * Método de compatibilidad: establece la fecha de inicio desde String.
     * @param start Fecha de inicio en formato String.
     * @deprecated Usar {@link #setFechaRecogida(long)} en su lugar.
     */
    public void setStartDate(String start) { this.fechaRecogida = parseDateString(start); }

    /**
     * Método de compatibilidad: establece la fecha de fin desde String.
     * @param end Fecha de fin en formato String.
     * @deprecated Usar {@link #setFechaDevolucion(long)} en su lugar.
     */
    public void setEndDate(String end) { this.fechaDevolucion = parseDateString(end); }

    /**
     * Convierte una cadena de fecha a timestamp en milisegundos.
     * <p>
     * Soporta múltiples formatos: milisegundos numéricos, yyyy-MM-dd,
     * dd/MM/yyyy, y formato corto local.
     * </p>
     *
     * @param s Cadena con la fecha a parsear.
     * @return Timestamp en milisegundos, o 0 si no se puede parsear.
     */
    private static long parseDateString(String s) {
        if (s == null) return 0L;
        s = s.trim();
        if (s.isEmpty()) return 0L;
        // try millis
        try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        java.text.DateFormat[] formats = new java.text.DateFormat[] {
                new java.text.SimpleDateFormat("yyyy-MM-dd"),
                new java.text.SimpleDateFormat("dd/MM/yyyy"),
                java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT)
        };
        for (java.text.DateFormat df : formats) {
            try {
                java.util.Date d = df.parse(s);
                if (d != null) return d.getTime();
            } catch (Exception ignored) {}
        }
        return 0L;
    }

    /**
     * Formatea un timestamp en una cadena de fecha legible.
     *
     * @param millis Timestamp en milisegundos.
     * @return Fecha formateada en formato local medio, o cadena vacía si hay error.
     */
    private static String formatDate(long millis) {
        try {
            java.util.Date d = new java.util.Date(millis);
            return java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(d);
        } catch (Exception e) { return ""; }
    }

}
