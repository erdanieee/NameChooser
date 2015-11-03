package com.dan.android.database;

import android.provider.BaseColumns;


public class TablaNombres implements BaseColumns{
    /** nombre de la tabla */
    public static final String TABLA = "nombres";

    //MySQLlite FIELDS
    public static final String COL_NOMBRE           = "nombre"          + "_" + TABLA;
    public static final String COL_SEXO             = "sexo"            + "_" + TABLA;
    public static final String COL_FRECUENCIA       = "frecuencia"      + "_" + TABLA;
    public static final String COL_USED             = "used"            + "_" + TABLA;
    public static final String COL_SCORE            = "score"           + "_" + TABLA;
    public static final String COL_COUNT            = "count"           + "_" + TABLA;

    public static final String sqlCreateTableContactos = "CREATE TABLE " + TABLA + " (" +
            _ID                 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NOMBRE          + " TEXT, " +
            COL_SEXO            + " TEXT, " +
            COL_FRECUENCIA      + " REAL, " +
            COL_USED            + " INTEGER DEFAULT 1, " +
            COL_SCORE           + " REAL DEFAULT 0, " +
            COL_COUNT           + " INTEGER DEFAULT 0)";
}
