package com.example.dan.selectordenombres;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

import database.DatabaseHelper;
import database.Nombre;



//TODO: Compartir en facebook los resultados cuando se encuentre un nombre común entre la pareja.
//TODO: añadir opción para borrar estadísticas (borrar solo hombres, mujeres o ambos)
//TODO: página inicial de configuración

//TODO: quantiles:  0%      10%     20%     30%     40%     50%     60%     70%     80%     90%     100%
//TODO:             0.0040  0.0050  0.0060  0.0080  0.0110  0.0160  0.0260  0.0480  0.1018  0.3398  29.2160
//TODO:             2437    2258    2048    1769    1489    1235    982     735     488     244     1



public class NameChooserActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int    DEFAULT_NUMBER_CLICK_ROUND      = 30;       //TODO: obtener esto como una preferencia al inicio
    private static final int    DEFAULT_MIN_NUMBER_OF_BUTTONS   = 2;
    private static final int    DEFAULT_MAX_NUMBER_OF_BUTTONS   = 8;
    private static final int    DEFAULT_REMAINING_NAMES_TO_END  = 2;
    private final String        DEBUG_TAG = "NameChooserMainActivity";

    private long                pref_totalVotacionesHechas;

    private DatabaseHelper      mDb = null;
    private LinearLayout        mLayoutButtons;
    private FloatingActionButton percentButton=null;
    private long                mLastClickTime;
    private Long                mNumberOfNamesUsed          = null;             //USE getter and setter!
    private Integer             mNumberOfButtons            = null;             //USE getter and setter!
    private Integer             mNumberOfNamesForCountRound = null;             //USE getter and setter!
    private DatabaseHelper.SEXO mSexo;
    private int                 mPercentSelected;
    private AlertDialog         mConfigDialog=null;
    private float               mTotalVotacionesNecesarias;
    private TextView            mTextViewCount;




    private long updateNumberOfNamesUsed(){ return getNumberOfNamesUsed(true); }
    private long getNumberOfNamesUsed(){ return getNumberOfNamesUsed(false); }
    private long getNumberOfNamesUsed(boolean updateNumberOfNames){
        if (updateNumberOfNames || mNumberOfNamesUsed ==null){
            mNumberOfNamesUsed = mDb.getUsedCount();
        }
        return mNumberOfNamesUsed;
    }

    private int updateNumberOfButtons(){ return getNumberOfButtons(true); }
    private int getNumberOfButtons(){ return getNumberOfButtons(false); }
    private int getNumberOfButtons(boolean updateNumberOfButtons){
        if(updateNumberOfButtons || mNumberOfButtons ==null){
            Log.d(DEBUG_TAG, "Update number of buttons");

            //Calculate optimal number of buttons
            mNumberOfButtons = getOptimalNumberOfButtons(getNumberOfNamesUsed());

            while (mLayoutButtons.getChildCount() != mNumberOfButtons){
                if(mLayoutButtons.getChildCount()> mNumberOfButtons){
                    mLayoutButtons.removeViewAt(mLayoutButtons.getChildCount()-1);

                } else {
                    Button b = new Button(getApplicationContext());
                    b.setOnClickListener(this);
                    mLayoutButtons.addView(b);
                }
            }
        }
        return mNumberOfButtons;
    }


    private void setNumberOfNamesForCountRound(int i){ mNumberOfNamesForCountRound =i; }
    private int updateNumberOfNamesForCountRound(){ return getNumberOfNamesForCountRound(true); }
    private int getNumberOfNamesForCountRound(){ return getNumberOfNamesForCountRound(false); }
    private int getNumberOfNamesForCountRound(boolean updateNumberOfRemainingSamples){
        if(updateNumberOfRemainingSamples || mNumberOfNamesForCountRound ==null){
            mNumberOfNamesForCountRound = mDb.getNumberOfNamesWithLessCount();
        }
        return mNumberOfNamesForCountRound;
    }


    private int getOptimalNumberOfButtons(long n){
        int b = DEFAULT_MAX_NUMBER_OF_BUTTONS;
        while (b > DEFAULT_MIN_NUMBER_OF_BUTTONS && n/b < DEFAULT_NUMBER_CLICK_ROUND){
            b--;
        }

        return b;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_chooser);

        mLayoutButtons  = (LinearLayout)findViewById(R.id.linearLayoutButtons);
        percentButton   = (FloatingActionButton)findViewById(R.id.porcentaje);
        mDb             = new DatabaseHelper(this);
        mPercentSelected= getResources().getInteger(R.integer.default_percent_selected);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextRound();
            }
        });

        percentButton.setImageDrawable(new TextDrawable("0%"));
        getPreferences();

        showConfigDialog();     //FIXME: comprobar si ya hay una ejecución en marcha y reiniciarla
    }

    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putLong(getString(R.string.pref_totalVotesDone), pref_totalVotacionesHechas);
        editor.apply();

        super.onStop();
    }

    private void showConfigDialog() {
        LayoutInflater layoutInflater;
        AlertDialog.Builder builder;
        View promptView;
        //Spinner spinner;
        SeekBar seekBar;
        ToggleButton tb;

        if(mConfigDialog==null) {
            layoutInflater  = LayoutInflater.from(this);
            promptView      = layoutInflater.inflate(R.layout.initial_option_dialog, null, false);
            builder         = new AlertDialog.Builder(this);
            //spinner       = (Spinner) promptView.findViewById(R.id.spinner);
            seekBar         = (SeekBar) promptView.findViewById(R.id.seekBar);
            tb              = (ToggleButton) promptView.findViewById(R.id.toggleButton);
            mTextViewCount  = (TextView) promptView.findViewById(R.id.textViewCount);

            builder.setView(promptView);

            tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mSexo = isChecked ? DatabaseHelper.SEXO.MALE : DatabaseHelper.SEXO.FEMALE;
                    mTextViewCount.setText(String.valueOf(mDb.getCountSexo(mSexo)*mPercentSelected/100));
                }
            });

            /*spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSexo = position == 0 ? DatabaseHelper.SEXO.MALE : DatabaseHelper.SEXO.FEMALE;
                }
            });*/

            seekBar.getProgressDrawable().setColorFilter(new LightingColorFilter(0xFF000000, Color.rgb((int) Math.round(seekBar.getProgress() * 2.55), (int) Math.round((seekBar.getMax() - seekBar.getProgress()) * 2.55), 0)));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    seekBar.getProgressDrawable().setColorFilter(new LightingColorFilter(0xFF000000, Color.rgb((int) Math.round(progress * 2.55), (int) Math.round((seekBar.getMax() - progress) * 2.55), 0)));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mPercentSelected = seekBar.getProgress() + 1;       //TODO: change percent selected by decil
                    mTextViewCount.setText(String.valueOf(mDb.getCountSexo(mSexo)*mPercentSelected/100));
                }
            });

            // setup a dialog window
            builder
                    .setTitle("Configuración")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            resetStatistics(mSexo, mPercentSelected);

                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetStatistics(mSexo, mPercentSelected);
                        }
                    });

            mConfigDialog = builder.create();
        }
        mConfigDialog.show();
    }


    private void resetStatistics(DatabaseHelper.SEXO s, int percentSelected) {
        mDb.resetTable(s, percentSelected);

        mTotalVotacionesNecesarias = calculateNumberOfVotesNeeded(getNumberOfNamesUsed());
        pref_totalVotacionesHechas = 0;

        percentButton.setImageDrawable(new TextDrawable("0%"));

        nextRound(null, true);
    }


    private void getPreferences() {
        SharedPreferences sharedPref;

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        pref_totalVotacionesHechas = sharedPref.getLong(getString(R.string.pref_totalVotesDone), 0);        //TODO: grabar antes de cerrar y cuando se reinicie
    }


    private void setNames(){
        ArrayList<Nombre> nombres;
        int i;
        Button b;

        nombres = mDb.getUsedNamesByRandomAndCount(getNumberOfButtons());

        i       = 0;
        for (Nombre n : nombres){
            b = (Button) mLayoutButtons.getChildAt(i++);

            b.setText(n.nombre);
            b.setTag(n);
        }
        Log.d(DEBUG_TAG, "Set names: " + nombres);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_name_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showConfigDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        // Preventing multiple clicks
        if (SystemClock.elapsedRealtime() - mLastClickTime > 500) {
            mLastClickTime = SystemClock.elapsedRealtime();
            nextRound(v);
        }
    }

    public void nextRound(){ nextRound(null);}
    public void nextRound(View v){ nextRound(v, false);}
    public void nextRound(View v, boolean first){
        Nombre n;
        float maxScore;

        if (first){
            updateNumberOfNamesUsed();
            updateNumberOfButtons();
            updateNumberOfNamesForCountRound();

        } else {
            pref_totalVotacionesHechas += getNumberOfButtons();
            percentButton.setImageDrawable(new TextDrawable(String.valueOf(
                    (int) Math.floor(100 * pref_totalVotacionesHechas / mTotalVotacionesNecesarias)
            ) + "%"));

            mDb.raiseCount(mLayoutButtons);

            maxScore = 0;
            for (int i = 0; i < mLayoutButtons.getChildCount(); i++) {
                n = (Nombre) mLayoutButtons.getChildAt(i).getTag();

                if (n.score > maxScore) {
                    maxScore = n.score;
                }
            }

            if (v != null) {
                mDb.updateScore((Nombre) v.getTag(), maxScore + (1 / (float) getNumberOfButtons()));
            }

            setNumberOfNamesForCountRound(getNumberOfNamesForCountRound() - getNumberOfButtons());

            //check end
            if (getNumberOfNamesUsed() <= DEFAULT_REMAINING_NAMES_TO_END) {
                percentButton.setImageDrawable(new TextDrawable("100%"));
                showEndDialog(mDb.getHighestScoreName().nombre);

                //check round
            } else if (getNumberOfNamesForCountRound() <= 0) {
                mDb.unUseLastNNamesByScore((int) Math.floor(getNumberOfNamesUsed() / 2f));          //TODO: en modo "fast" dividir entre el número de botones
                updateNumberOfNamesUsed();
                updateNumberOfButtons();
                updateNumberOfNamesForCountRound();
            }
        }

        setNames();
    }



    protected void showEndDialog(String winnerName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // setup a dialog window
        builder
                .setTitle("¡Terminado!")
                .setMessage("El nombre más votado es " + winnerName + ". Pulsa " + getString(android.R.string.ok) + " para volver a empezar")
                .setIcon(android.R.drawable.picture_frame)  //FIXME: cambiar icono por uno en condiciones
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showConfigDialog();
                    }
                });

        builder.create().show();
    }


    private int calculateNumberOfVotesNeeded(long n){ return calculateNumberOfVotesNeeded(n,false); }
    private int calculateNumberOfVotesNeeded(long n, boolean clicks){
        int ret, totalVotes, totalClicks, buttons;
        long used, votedInRound;

        used            = n;
        totalClicks     = 0;
        totalVotes      = 0;
        votedInRound    = 0;
        buttons         = getOptimalNumberOfButtons(used);
        while (used >= DEFAULT_REMAINING_NAMES_TO_END){
            votedInRound += buttons;
            totalClicks++;

            if(votedInRound >= used){
                totalVotes     += votedInRound;
                votedInRound   -= used;
                used            = (int) Math.floor(used/2f);    //TODO: en modo "fast" dividir entre el número de botones
                buttons         = getOptimalNumberOfButtons(used);
            }
        }

        ret = clicks ? totalClicks : totalVotes;

        return ret+1;
    }
}
