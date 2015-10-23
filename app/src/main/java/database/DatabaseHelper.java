package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
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
    private String FEMALE_SYMBOL = "M";
    private String MALE_SYMBOL = "H";
    private static final int DATABASE_VERSION       = 1;
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

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TablaNombres.TABLA_NOMBRES);


        Log.i(this.getClass().getSimpleName(), "Creando nueva base de datos");
        sqLiteDatabase.execSQL(TablaNombres.sqlCreateTableContactos);

        try{
            br = new BufferedReader(new InputStreamReader(mContext.getAssets().open("spanish.txt")));
            sqLiteDatabase.beginTransaction();

            while((line=br.readLine())!=null){
                tokens = line.split("\t");
                values.clear();

                values.put(TablaNombres.COL_NOMBRE, tokens[0]);
                values.put(TablaNombres.COL_SEXO, tokens[1]);
                values.put(TablaNombres.COL_FRECUENCIA, tokens[2]);

                sqLiteDatabase.insert(TablaNombres.TABLA_NOMBRES, null, values);
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

    }


    public long getUsedCount(){
        return count(TablaNombres.TABLA_NOMBRES, TablaNombres.COL_USED+"=?", new String[]{"1"});
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

        c = getReadableDatabase().query(TablaNombres.TABLA_NOMBRES, proyection, selection, selectionArgs, null, null, order, String.valueOf(limit));

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

        c = getReadableDatabase().query(TablaNombres.TABLA_NOMBRES, proyection, selection, selectionArgs, null, null, order, limit);

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

        c = getReadableDatabase().query(TablaNombres.TABLA_NOMBRES, proyection, selection, selectionArg, groupBy, null, order, limit);       //FIXME: si no funcionar usar rawQuery

        while(c.moveToNext()){
            count = c.getInt(0);
            break;
        }
        c.close();

        return count;

    }


    public void raiseCount(LinearLayout layoutButtons) {
        SQLiteDatabase db;
        SQLiteStatement stmt;
        Nombre n;

        db  = getWritableDatabase();

        stmt = db.compileStatement("UPDATE " +
                    TablaNombres.TABLA_NOMBRES +
                    " SET "   + TablaNombres.COL_COUNT + "=" + TablaNombres.COL_COUNT + "+1" +
                    " WHERE " + TablaNombres._ID + "=?");

        try {
            db.beginTransaction();
            for (int i = 0; i < layoutButtons.getChildCount(); i++) {
                n = (Nombre) ((Button) layoutButtons.getChildAt(i)).getTag();

                stmt.bindLong(1, n.id);
                stmt.execute();
            }
            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    public void unUseLastNNamesByScore(int numberOfRows) {
        SQLiteDatabase db;
        SQLiteStatement stmt;
        Nombre n;

        db  = getWritableDatabase();

        stmt = db.compileStatement(
                "UPDATE " + TablaNombres.TABLA_NOMBRES +
                        " SET " + TablaNombres.COL_USED + "=0" +
                        " WHERE " + TablaNombres._ID + " IN" +
                        "(" +
                        " SELECT " + TablaNombres._ID +
                        " FROM " + TablaNombres.TABLA_NOMBRES +
                        " WHERE " + TablaNombres.COL_USED + "=1" +
                        " ORDER BY " + TablaNombres.COL_SCORE + " ASC" +
                        " LIMIT " + String.valueOf(numberOfRows) +
                        ")"
        );

        stmt.execute();
    }


    public void resetTable (SEXO s){
        SQLiteDatabase db;
        SQLiteStatement stmt;

        db  = getWritableDatabase();

        stmt = db.compileStatement("UPDATE " +
                TablaNombres.TABLA_NOMBRES +
                " SET " + TablaNombres.COL_USED + "=?" +
                " AND " + TablaNombres.COL_SCORE + "=?" +
                " AND " + TablaNombres.COL_COUNT + "=?" +
                " WHERE " + TablaNombres.COL_SEXO + "=?"
        );

        try {
            db.beginTransaction();

            //set sex
            stmt.bindLong(1, 1);
            stmt.bindLong(2, 0);
            stmt.bindLong(3, 0);
            stmt.bindString(4, s==SEXO.FEMALE ? FEMALE_SYMBOL : MALE_SYMBOL);
            stmt.execute();

            //unset the other sex
            stmt.bindLong(1, 0);
            stmt.bindLong(2, 0);
            stmt.bindLong(3, 0);
            stmt.bindString(4, s==SEXO.FEMALE ? MALE_SYMBOL : FEMALE_SYMBOL);
            stmt.execute();

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

}
