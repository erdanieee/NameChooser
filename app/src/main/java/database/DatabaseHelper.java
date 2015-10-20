package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by dan on 25/08/13.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME       = "names.db";
    private static final int DATABASE_VERSION       = 1;
    private Context mContext;


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


    public long count(String table, String selection, String[] selectionArgs){
        long c;
        c = DatabaseUtils.queryNumEntries(getReadableDatabase(), table, selection, selectionArgs);
        return c;
    }
}
