package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by dan on 25/08/13.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME       = "names.db";
    private static final String DATABASE_FILE   = "test.txt";
    //private static final String DATABASE_FILE   = "spanish.txt";
    private String FEMALE_SYMBOL = "M";
    private String MALE_SYMBOL = "H";
    private static final int DATABASE_VERSION  = 4;
    private Context mContext;




    public enum SEXO { MALE, FEMALE};


    public DatabaseHelper(Context contexto){
        super(contexto, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = contexto;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        BufferedReader br;
        String line=null;
        String[] tokens;
        ContentValues values = new ContentValues();

        try{
            br = new BufferedReader(new InputStreamReader(mContext.getAssets().open(DATABASE_FILE)));
            sqLiteDatabase.beginTransaction();

            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TablaNombres.TABLA);

            Log.i(this.getClass().getSimpleName(), "Creando nueva base de datos");
            sqLiteDatabase.execSQL(TablaNombres.sqlCreateTableContactos);

            while((line=br.readLine())!=null){
                tokens = line.split("\t");
                values.clear();

                values.put(TablaNombres.COL_NOMBRE, tokens[0]);
                values.put(TablaNombres.COL_SEXO, tokens[1]);
                values.put(TablaNombres.COL_FRECUENCIA, tokens[2]);

                sqLiteDatabase.insert(TablaNombres.TABLA, null, values);
            }
            sqLiteDatabase.setTransactionSuccessful();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            sqLiteDatabase.endTransaction();
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);       //FIXME: temporal!!!        onCreate(db);       //FIXME: temporal!!!
    }


    public long getUsedCount(){
        return count(TablaNombres.TABLA, TablaNombres.COL_USED+"=?", new String[]{"1"});
    }


    private long count(String table, String selection, String[] selectionArgs){
        long c;
        c = DatabaseUtils.queryNumEntries(getReadableDatabase(), table, selection, selectionArgs);
        return c;
    }


    public ArrayList<Nombre> getUsedNamesByRandomAndCount(int limit){
        ArrayList<Nombre> aux;
        Cursor c;
        String[] proyection;
        String selection;
        String[] selectionArgs;
        String order;

        proyection      = new String[]{TablaNombres._ID, TablaNombres.COL_NOMBRE, TablaNombres.COL_SCORE, TablaNombres.COL_COUNT};
        selection       = TablaNombres.COL_USED + "=?";
        selectionArgs   = new String[]{"1"};
        order           = TablaNombres.COL_COUNT + " ASC, RANDOM()";
        aux             = new ArrayList<Nombre>(limit);

        c = getReadableDatabase().query(TablaNombres.TABLA, proyection, selection, selectionArgs, null, null, order, String.valueOf(limit));

        while(c.moveToNext()){
            aux.add(new Nombre( c.getInt(c.getColumnIndex(TablaNombres._ID)),
                                c.getString(c.getColumnIndex(TablaNombres.COL_NOMBRE)),
                                c.getFloat(c.getColumnIndex(TablaNombres.COL_SCORE)),
                                c.getInt(c.getColumnIndex(TablaNombres.COL_COUNT))
            ));
        }
        c.close();

        return aux;
    }



    public Nombre getHighestScoreName(){
        Nombre n=null;

        Cursor c;
        String[] proyection;
        String selection;
        String[] selectionArgs;
        String order;
        String limit;

        proyection      = new String[]{TablaNombres._ID, TablaNombres.COL_NOMBRE, TablaNombres.COL_SCORE, TablaNombres.COL_COUNT};
        selection       = TablaNombres.COL_USED + "=?";
        selectionArgs   = new String[]{"1"};
        order           = TablaNombres.COL_SCORE + " DESC";
        limit           = "1";

        c = getReadableDatabase().query(TablaNombres.TABLA, proyection, selection, selectionArgs, null, null, order, limit);

        while(c.moveToNext()){
            n = new Nombre( c.getInt(c.getColumnIndex(TablaNombres._ID)),
                    c.getString(c.getColumnIndex(TablaNombres.COL_NOMBRE)),
                    c.getFloat(c.getColumnIndex(TablaNombres.COL_SCORE)),
                    c.getInt(c.getColumnIndex(TablaNombres.COL_COUNT))
            );
            break;
        }
        c.close();

        return n;
    }



    public int getNumberOfNamesWithLessCount(){
        int count=0;

        Cursor c;
        String[] proyection;
        String selection;
        String[] selectionArg;
        String groupBy;
        String order;
        String limit;

        proyection      = new String[]{"COUNT(" + TablaNombres._ID + ")"};
        selection       = TablaNombres.COL_USED + "=?";
        selectionArg    = new String[] {"1"};
        groupBy         = TablaNombres.COL_COUNT;
        order           = TablaNombres.COL_COUNT + " ASC";
        limit           = "1";

        c = getReadableDatabase().query(TablaNombres.TABLA, proyection, selection, selectionArg, groupBy, null, order, limit);       //FIXME: si no funcionar usar rawQuery

        while(c.moveToNext()){
            count = c.getInt(0);
            break;
        }
        c.close();

        return count;

    }


    public void raiseCount(LinearLayout layoutButtons) {
        SQLiteDatabase db;
        Nombre n;

        db = getWritableDatabase();

        try {
            db.beginTransaction();
            for (int i = 0; i < layoutButtons.getChildCount(); i++) {
                n = (Nombre) ((Button) layoutButtons.getChildAt(i)).getTag();

                db.execSQL("UPDATE " + TablaNombres.TABLA +
                                " SET " + TablaNombres.COL_COUNT + "=" + TablaNombres.COL_COUNT + "+1" +
                                " WHERE " + TablaNombres._ID + "=" + String.valueOf(n.id)
                );
            }
            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }



    public void unUseLastNNamesByScore(int numberOfRows) {
        SQLiteDatabase db;
        Nombre n;

        db  = getWritableDatabase();

        db.execSQL(
                "UPDATE " + TablaNombres.TABLA +
                        " SET " + TablaNombres.COL_USED + "=0" +
                        " WHERE " + TablaNombres._ID + " IN" +
                        "(" +
                        " SELECT " + TablaNombres._ID +
                        " FROM " + TablaNombres.TABLA +
                        " WHERE " + TablaNombres.COL_USED + "=1" +
                        " ORDER BY " + TablaNombres.COL_SCORE + " ASC" +
                        " LIMIT " + String.valueOf(numberOfRows) +
                        ")"
        );
    }
    }

    public void resetTable (SEXO s){
        SQLiteDatabase db;
        ContentValues values;
        String selection;
        String[] selectionArgs;

        values = new ContentValues();
        selection = TablaNombres.COL_SEXO + "=?";
        db = getWritableDatabase();

        try {
            db.beginTransaction();

            values.clear();
            values.put(TablaNombres.COL_USED, 1);
            values.put(TablaNombres.COL_SCORE, 0);
            values.put(TablaNombres.COL_COUNT, 0);
            selectionArgs = new String[]{s==SEXO.FEMALE ? FEMALE_SYMBOL : MALE_SYMBOL};
            db.update(TablaNombres.TABLA, values, selection, selectionArgs);

            values.clear();
            values.put(TablaNombres.COL_USED, 0);
            values.put(TablaNombres.COL_SCORE, 0);
            values.put(TablaNombres.COL_COUNT, 0);
            selectionArgs = new String[]{s==SEXO.FEMALE ? MALE_SYMBOL : FEMALE_SYMBOL};
            db.update(TablaNombres.TABLA, values, selection, selectionArgs);

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }


    public void updateScore(Nombre n, float s) {
        SQLiteDatabase db;

        db = getWritableDatabase();

        db.execSQL("UPDATE " + TablaNombres.TABLA +
                        " SET " + TablaNombres.COL_SCORE + "=" + TablaNombres.COL_SCORE + "+" + String.valueOf(s) +
                        " WHERE " + TablaNombres._ID + "=" + String.valueOf(n.id)
        );
    }


    public Nombre getNombre(int id){
        Nombre n=null;

        Cursor c;
        String[] proyection;
        String selection;
        String[] selectionArgs;

        proyection      = new String[]{TablaNombres._ID, TablaNombres.COL_NOMBRE, TablaNombres.COL_SCORE, TablaNombres.COL_COUNT};
        selection       = TablaNombres._ID + "=?";
        selectionArgs   = new String[]{String.valueOf(id)};

        c = getReadableDatabase().query(TablaNombres.TABLA, proyection, selection, selectionArgs, null, null, null);

        while(c.moveToNext()){
            n = new Nombre( c.getInt(c.getColumnIndex(TablaNombres._ID)),
                    c.getString(c.getColumnIndex(TablaNombres.COL_NOMBRE)),
                    c.getFloat(c.getColumnIndex(TablaNombres.COL_SCORE)),
                    c.getInt(c.getColumnIndex(TablaNombres.COL_COUNT))
            );
            break;
        }
        c.close();

        return n;
    }
}
