package database;

import android.provider.BaseColumns;

/**
 * Created by dlopez on 22/10/13.
 */
public class TablaNombres implements BaseColumns{
    /** nombre de la tabla */
    public static final String TABLA_NOMBRES = "nombres";

    //MySQLlite FIELDS
    public static final String COL_NOMBRE           = "nombre"          + "_" + TABLA_NOMBRES;
    public static final String COL_SEXO             = "sexo"            + "_" + TABLA_NOMBRES;
    public static final String COL_FRECUENCIA       = "frecuencia"      + "_" + TABLA_NOMBRES;
    public static final String COL_USED             = "used"            + "_" + TABLA_NOMBRES;
    public static final String COL_SCORE            = "score"           + "_" + TABLA_NOMBRES;
    public static final String COL_COUNT            = "count"           + "_" + TABLA_NOMBRES;

    public static final String sqlCreateTableContactos = "CREATE TABLE " + TABLA_NOMBRES + " (" +
            _ID                 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NOMBRE          + " TEXT, " +
            COL_SEXO            + " TEXT, " +
            COL_FRECUENCIA      + " REAL, " +
            COL_USED            + " INTEGER DEFAULT 1, " +
            COL_SCORE           + " REAL DEFAULT 0, " +
            COL_COUNT           + " INTEGER DEFAULT 0)";
}
