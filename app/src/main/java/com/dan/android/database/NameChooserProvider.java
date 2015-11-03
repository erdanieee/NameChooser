package com.dan.android.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;



public class NameChooserProvider extends ContentProvider {
    private static final String TAG             = "NameChooserProvider";
    private static final String PROVIDER_NAME   = "dan.android.nameChooser.provider";
    private static final String URI_NOMBRES     = "tablaNombres";

    public static final Uri CONTENT_URI_NOMBRES = Uri.parse("content://" + PROVIDER_NAME + "/" + URI_NOMBRES);

    //UriMatcher
    private static final int NOMBRES    = 1;
    private static final int NOMBRES_ID = 2;

    //inicializamos el UriMatcher
    public static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, URI_NOMBRES,                 NOMBRES);
        uriMatcher.addURI(PROVIDER_NAME, URI_NOMBRES + "/#",          NOMBRES_ID);
    }

    private DatabaseHelper dbHelper;


    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "Query " + uri.toString());
        Cursor c;
        SQLiteQueryBuilder sqlb = new SQLiteQueryBuilder();

        //si es una consulta a un ID concreto construimos el WHERE
        switch (uriMatcher.match(uri)){
            case NOMBRES_ID:
                sqlb.appendWhere(TablaNombres._ID + "=" + uri.getLastPathSegment());
            case NOMBRES:
                sqlb.setTables(TablaNombres.TABLA);
                break;

            default:
                throw new IllegalStateException ("Query no válida!!") ;
        }
        c= sqlb.query(dbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);   //TODO: comprobar si es necesario esto (!!!???)
        return c;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tabla=null;
        String where = selection;
        int rowDeleted;

        //si es una consulta a un ID concreto construimos el WHERE
        switch (uriMatcher.match(uri)){
            case NOMBRES_ID:
                where = TablaNombres._ID + "=" + uri.getLastPathSegment();
            case NOMBRES:
                tabla = TablaNombres.TABLA;
                break;

            default:
                throw new IllegalStateException ("Delete no válido!!") ;
        }
        rowDeleted = dbHelper.getWritableDatabase().delete(tabla, where, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "Delete " + rowDeleted + " items on " + uri.toString());
        return rowDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "Update " + uri.toString() + " " + values.toString());
        String tabla=null;
        String where = selection;
        int rowUpdated;

        //si es una consulta a un ID concreto construimos el WHERE
        switch (uriMatcher.match(uri)){
            case NOMBRES_ID:
                where = BaseColumns._ID + "=" + uri.getLastPathSegment();
            case NOMBRES:
                tabla = TablaNombres.TABLA;
                break;
            default:
                throw new IllegalStateException ("Update no válido!!") ;
        }
        rowUpdated = dbHelper.getWritableDatabase().update(tabla, values, where, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rowUpdated;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri contentUri  = null;
        Uri _uri        = null;
        long id         = -1;
        String tabla    = null;

        switch (uriMatcher.match(uri)){
            case NOMBRES:
                tabla = TablaNombres.TABLA;
                contentUri  = CONTENT_URI_NOMBRES;
                break;
            default:
                throw new IllegalStateException ("Insert no válido!!") ;
        }

        id = dbHelper.getWritableDatabase().insert(tabla, null, values);
        Log.d(TAG, "Inserted id: " + id + " on " + uri.toString() + " " + values.toString());

        if (id > 0){
            _uri = ContentUris.withAppendedId(contentUri, id);
            getContext().getContentResolver().notifyChange(_uri, null);
        }

        return _uri;
    }


    @Override
    public String getType(Uri uri) {
        String type = null;

        switch (uriMatcher.match(uri)){
            case NOMBRES_ID:
                type = "vnd.android.cursor.item/vnd.namechooser.contactos";
                break;
            case NOMBRES:
                type = "vnd.android.cursor.dir/vnd.namechooser.contactos";
                break;
            default:
                throw new IllegalStateException ("Tipo no válido!!") ;
        }

        return type;
    }
}
