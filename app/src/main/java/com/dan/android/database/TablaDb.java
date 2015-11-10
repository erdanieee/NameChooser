package com.dan.android.database;


public class TablaDb{
    public static final String TABLA = "db";

    //MySQLlite FIELDS
    public static final String COL_ZONE           = "zone"          + "_" + TABLA;

    public static final String sqlCreateTableDb = "CREATE TABLE " + TABLA + " (" +
            COL_ZONE            + " INTEGER DEFAULT -1)";
}
