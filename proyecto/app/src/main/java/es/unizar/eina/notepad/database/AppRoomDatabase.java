package es.unizar.eina.notepad.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base de datos Room de la aplicación de gestión de quads y reservas.
 * <p>
 * Esta clase abstracta define la configuración de la base de datos SQLite
 * utilizando la librería Room. Incluye tres entidades: {@link Quad},
 * {@link Reserva} y {@link ReservaQuad}.
 * </p>
 * <p>
 * Implementa el patrón Singleton para garantizar una única instancia de
 * la base de datos en toda la aplicación. Proporciona un ExecutorService
 * para ejecutar operaciones de escritura en hilos de fondo.
 * </p>
 * <p>
 * Al crear la base de datos por primera vez, se insertan datos de ejemplo
 * para facilitar las pruebas (5 quads, 5 reservas y sus relaciones).
 * </p>
 *
 * @author Juan Ondiviela Pamplona 897735 y Rubén Villar Artajona 896654 - Universidad de Zaragoza
 * @version 1.0
 * @see Quad
 * @see Reserva
 * @see ReservaQuad
 * @see QuadDao
 * @see ReservaDao
 * @see ReservaQuadDao
 */
@Database(entities = {Quad.class, Reserva.class, ReservaQuad.class}, version = 4, exportSchema = false)
public abstract class AppRoomDatabase extends RoomDatabase {

    /**
     * Proporciona acceso al DAO de Quad.
     * @return Instancia del QuadDao.
     */
    public abstract QuadDao quadDao();

    /**
     * Proporciona acceso al DAO de Reserva.
     * @return Instancia del ReservaDao.
     */
    public abstract ReservaDao reservaDao();

    /**
     * Proporciona acceso al DAO de ReservaQuad.
     * @return Instancia del ReservaQuadDao.
     */
    public abstract ReservaQuadDao reservaQuadDao();

    /** Instancia única de la base de datos (patrón Singleton) */
    private static volatile AppRoomDatabase INSTANCE;

    /** Número de hilos en el pool para operaciones de escritura */
    private static final int NUMBER_OF_THREADS = 4;

    /**
     * ExecutorService para ejecutar operaciones de base de datos en segundo plano.
     * <p>
     * Utiliza un pool de hilos fijo para gestionar múltiples operaciones concurrentes
     * de forma eficiente sin bloquear el hilo principal de la UI.
     * </p>
     */
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Obtiene la instancia única de la base de datos (patrón Singleton).
     * <p>
     * Si la base de datos no existe, la crea y configura con:
     * - Migración destructiva (fallbackToDestructiveMigration)
     * - Callback para poblar datos de ejemplo al crearla
     * </p>
     * <p>
     * Este método es thread-safe gracias a la doble verificación con bloqueo.
     * </p>
     *
     * @param context Contexto de la aplicación.
     * @return Instancia única de AppRoomDatabase.
     */
    static AppRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppRoomDatabase.class) {
                if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                AppRoomDatabase.class, "quad_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback que se ejecuta cuando se crea la base de datos por primera vez.
     * <p>
     * Inserta datos de ejemplo:
     * - 5 quads con diferentes tipos y precios
     * - 5 reservas con diferentes clientes y fechas
     * - 8 asociaciones reserva-quad que demuestran la relación N:N
     * </p>
     * <p>
     * Estos datos facilitan las pruebas de la aplicación.
     * Para mantener los datos entre reinicios, comenta el bloque
     * databaseWriteExecutor.execute().
     * </p>
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background with sample data (5 quads, 5 reservas)
                QuadDao quadDao = INSTANCE.quadDao();
                ReservaDao reservaDao = INSTANCE.reservaDao();
                ReservaQuadDao rqDao = INSTANCE.reservaQuadDao();

                // Clean existing
                quadDao.deleteAll();
                reservaDao.deleteAll();
                rqDao.deleteAll();

                // Insert 5 quads
                Quad q1 = new Quad(Quad.Tipo.UNIPLAZA, 80, "AAA-001", "Quad demo 1");
                Quad q2 = new Quad(Quad.Tipo.BIPLAZA, 100, "BBB-002", "Quad demo 2");
                Quad q3 = new Quad(Quad.Tipo.UNIPLAZA, 90, "CCC-003", "Quad demo 3");
                Quad q4 = new Quad(Quad.Tipo.BIPLAZA, 110, "DDD-004", "Quad demo 4");
                Quad q5 = new Quad(Quad.Tipo.UNIPLAZA, 95, "EEE-005", "Quad demo 5");

                long id1 = quadDao.insertQuad(q1);
                long id2 = quadDao.insertQuad(q2);
                long id3 = quadDao.insertQuad(q3);
                long id4 = quadDao.insertQuad(q4);
                long id5 = quadDao.insertQuad(q5);

                // Prepare 5 reservas with different dates
                long now = System.currentTimeMillis();
                long oneDay = 24L * 60L * 60L * 1000L;

                Reserva r1 = new Reserva(now + oneDay, now + 2 * oneDay, 80.0, 600111222, "Cliente A");
                Reserva r2 = new Reserva(now + 2 * oneDay, now + 4 * oneDay, 190.0, 600222333, "Cliente B");
                Reserva r3 = new Reserva(now + 3 * oneDay, now + 5 * oneDay, 285.0, 600333444, "Cliente C");
                Reserva r4 = new Reserva(now + 4 * oneDay, now + 6 * oneDay, 205.0, 600444555, "Cliente D");
                Reserva r5 = new Reserva(now + 5 * oneDay, now + 6 * oneDay, 95.0, 600555666, "Cliente E");

                long res1 = reservaDao.insertReserva(r1);
                long res2 = reservaDao.insertReserva(r2);
                long res3 = reservaDao.insertReserva(r3);
                long res4 = reservaDao.insertReserva(r4);
                long res5 = reservaDao.insertReserva(r5);

                // Link reservas to quads via ReservaQuad
                // r1 -> q1
                ReservaQuad rq1 = new ReservaQuad((int) res1, (int) id1, 2);
                // r2 -> q2, q3
                ReservaQuad rq2 = new ReservaQuad((int) res2, (int) id2, 1);
                ReservaQuad rq3 = new ReservaQuad((int) res2, (int) id3, 2);
                // r3 -> q1, q4, q5
                ReservaQuad rq4 = new ReservaQuad((int) res3, (int) id1, 1);
                ReservaQuad rq5 = new ReservaQuad((int) res3, (int) id4, 1);
                ReservaQuad rq6 = new ReservaQuad((int) res3, (int) id5, 0);
                // r4 -> q2
                ReservaQuad rq7 = new ReservaQuad((int) res4, (int) id2, 1);
                // r5 -> q5
                ReservaQuad rq8 = new ReservaQuad((int) res5, (int) id5, 1);

                rqDao.insert(rq1);
                rqDao.insert(rq2);
                rqDao.insert(rq3);
                rqDao.insert(rq4);
                rqDao.insert(rq5);
                rqDao.insert(rq6);
                rqDao.insert(rq7);
                rqDao.insert(rq8);
            });
        }
    };

}
