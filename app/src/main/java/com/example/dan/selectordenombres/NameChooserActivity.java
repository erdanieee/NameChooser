package com.example.dan.selectordenombres;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import database.DatabaseHelper;
import database.Nombre;

public class NameChooserActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int INTENT_RESULT_SETTING  = 846384126;
    private static final int INTENT_NEW_USER        = 684654168;
    private static final int DEFAULT_NUMBER_CLICK_ROUND = 30;       //TODO: obtener esto como una preferencia al inicio
    private static final int DEFAULT_MIN_NUMBER_OF_BUTTONS = 2;
    private static final int DEFAULT_MAX_NUMBER_OF_BUTTONS = 8;
    private static final int DEFAULT_REMAINING_NAMES_TO_END = 2;
    private final String DEBUG_TAG = "NameChooserMainActivity";
    private TextView textViewTitle;
    private LinearLayout layoutButtons;
    private String  pref_userName;
    private float pref_totalVotacionesNecesarias;
    private long pref_totalVotacionesHechas;
    private int auxVotaciones=0;
    private boolean blockVotes = false;

    private DatabaseHelper db = null;
    private Long    _numberOfNamesUsed = null;              //USE getter and setter!
    private Integer _numberOfButtons = null;                //USE getter and setter!
    private Integer _numberOfNamesForCountRound = null;     //USE getter and setter!
    private FloatingActionButton percentButton=null;


    private long updateNumberOfNamesUsed(){ return getNumberOfNamesUsed(true); }
    private long getNumberOfNamesUsed(){ return getNumberOfNamesUsed(false); }
    private long getNumberOfNamesUsed(boolean updateNumberOfNames){
        if (updateNumberOfNames || _numberOfNamesUsed==null){
            _numberOfNamesUsed = db.getUsedCount();
        }
        return _numberOfNamesUsed;
    }

    private int updateNumberOfButtons(){ return getNumberOfButtons(true); }
    private int getNumberOfButtons(){ return getNumberOfButtons(false); }
    private int getNumberOfButtons(boolean updateNumberOfButtons){
        if(updateNumberOfButtons || _numberOfButtons==null){
            Log.d(DEBUG_TAG, "Update number of buttons");

            //Calculate optimal number of buttons
            _numberOfButtons = getOptimalNumberOfButtons(getNumberOfNamesUsed());

            while (layoutButtons.getChildCount() != _numberOfButtons){
                if(layoutButtons.getChildCount()> _numberOfButtons){
                    layoutButtons.removeViewAt(layoutButtons.getChildCount()-1);

                } else {
                    Button b = new Button(getApplicationContext());
                    b.setOnClickListener(this);
                    layoutButtons.addView(b);
                }
            }
        }
        return _numberOfButtons;
    }

    private int getOptimalNumberOfButtons(long n){
        int b = DEFAULT_MAX_NUMBER_OF_BUTTONS;
        while (b > DEFAULT_MIN_NUMBER_OF_BUTTONS && n/b < DEFAULT_NUMBER_CLICK_ROUND){
            b--;
        }

        return b;
    }


    private void setNumberOfNamesForCountRound(int i){ _numberOfNamesForCountRound=i; }
    private int updateNumberOfNamesForCountRound(){ return getNumberOfNamesForCountRound(true); }
    private int getNumberOfNamesForCountRound(){ return getNumberOfNamesForCountRound(false); }
    private int getNumberOfNamesForCountRound(boolean updateNumberOfRemainingSamples){
        if(updateNumberOfRemainingSamples || _numberOfNamesForCountRound==null){
            _numberOfNamesForCountRound = db.getNumberOfNamesWithLessCount();
        }
        return _numberOfNamesForCountRound;
    }


    //TODO: Compartir en facebook los resultados cuando se encuentre un nombre común entre la pareja.
    //TODO: añadir opción para borrar estadísticas (borrar solo hombres, mujeres o ambos)
    //TODO: página inicial de configuración

    //TODO: quantiles:  0%      10%     20%     30%     40%     50%     60%     70%     80%     90%     100%
    //TODO:             0.0040  0.0050  0.0060  0.0080  0.0110  0.0160  0.0260  0.0480  0.1018  0.3398  29.2160
    //TODO:             2437    2258    2048    1769    1489    1235    982     735     488     244     1



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_chooser);

        textViewTitle   = (TextView)findViewById(R.id.TextViewTitle);
        layoutButtons   = (LinearLayout)findViewById(R.id.linearLayoutButtons);
        db              = new DatabaseHelper(this);
        percentButton   = (FloatingActionButton)findViewById(R.id.porcentaje);

        //percentButton.image
        //percentButton.align

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //setSupportActionBar(toolbar);

//        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_firstStart), true)) {
//            startActivityForResult(new Intent(this, NewUserActivity.class), INTENT_NEW_USER);   //TODO: considerar la posibilidad de hacerlo con un diálogo en lugar de un intent


        resetStatistics(DatabaseHelper.SEXO.FEMALE);

//        } else {
            getPreferences();
//        }



        updateNumberOfNamesUsed();
        updateNumberOfButtons();
        updateNumberOfNamesForCountRound();
        setNames();

        percentButton.setImageDrawable(new TextDrawable("0%"));
        textViewTitle.setText("Selecciona de los siguientes nombres el que más te gusta:");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextRound(null);
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });
    }

    private void resetStatistics(DatabaseHelper.SEXO s) {
        db.resetTable(s);
    }


    private void getPreferences() {
        SharedPreferences sharedPref;

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        pref_userName                   = sharedPref.getString(getString(R.string.pref_userName), "");
        pref_totalVotacionesNecesarias  = sharedPref.getInt(getString(R.string.pref_totalVotesNeeded), calculateNumberOfVotesNeeded(getNumberOfNamesUsed()));    //TODO: grabar totalNames la primera vez que se ejecuta y en el reset
        pref_totalVotacionesHechas      = sharedPref.getLong(getString(R.string.pref_totalVotesDone), 0);                           //TODO: grabar antes de cerrar y cuando se reinicie

        setTitle(pref_userName);
    }









/*    protected void showInputDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        pref_userName = editText.getText().toString();

                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                        editor.putString(getString(R.string.pref_userName), pref_userName);
                        editor.commit();
                        getPreferences();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                                homeIntent.addCategory(Intent.CATEGORY_HOME);
                                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(homeIntent);
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }*/




    private void setButtonsEnabled(boolean b){
        for (int i=0; i<layoutButtons.getChildCount(); i++){
            ( (Button)layoutButtons.getChildAt(i) ).setEnabled(b);
        }
    }


    private void setNames(){
        ArrayList<Nombre> nombres;
        int i=0;
        Button b;

        nombres = db.getUsedNamesByRandomAndCount(getNumberOfButtons());

        i       = 0;
        for (Nombre n : nombres){
            b = (Button) layoutButtons.getChildAt(i++);

            b.setText(n.nombre);
            b.setTag(n);
        }
        Log.d(DEBUG_TAG, "Set names: " + nombres);
    }



/*
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
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, INTENT_RESULT_SETTING);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_NEW_USER:
                getPreferences();
                break;

            case INTENT_RESULT_SETTING:     //TODO: eliminar preferencias para cambiar sexo/frecuencia en mitad de la búsqueda. Si quieren cambiar, que empiecen de nuevo!
                getPreferences();
                update
                break;
        }
    }
*/




    @Override
    public void onClick(View v) {
        if (!blockVotes) {
            nextRound(v);
        }
    }

    public void nextRound(){ nextRound(null);}
    public void nextRound(View v){
        Nombre n;
        float maxScore=0;

        //setButtonsEnabled(false);
        blockVotes = true;

        auxVotaciones++;
        pref_totalVotacionesHechas +=getNumberOfButtons();
        percentButton.setImageDrawable(new TextDrawable(String.valueOf(
                (int)Math.ceil(100*pref_totalVotacionesHechas / pref_totalVotacionesNecesarias)
        ) + "%"));

        db.raiseCount(layoutButtons);

        maxScore = 0;
        for (int i = 0; i < layoutButtons.getChildCount(); i++) {
            n = (Nombre) layoutButtons.getChildAt(i).getTag();

            if (n.score > maxScore) {
                maxScore = n.score;
            }
        }

        if(v!=null) {
            db.updateScore((Nombre) v.getTag(), maxScore + (1 / (float) getNumberOfButtons()));
        }

        setNumberOfNamesForCountRound(getNumberOfNamesForCountRound() - getNumberOfButtons());

        //check end
        if (getNumberOfNamesUsed() <= DEFAULT_REMAINING_NAMES_TO_END) {
            percentButton.setImageDrawable(new TextDrawable("100%"));
            showEndDialog(db.getHighestScoreName().nombre);

        //check round
        } else if (getNumberOfNamesForCountRound() <= 0) {
            db.unUseLastNNamesByScore((int) Math.ceil(getNumberOfNamesUsed() / 2f));
            updateNumberOfNamesUsed();
            updateNumberOfButtons();
            updateNumberOfNamesForCountRound();
        }

        setNames();
        //setButtonsEnabled(true);
        blockVotes = false;
    }



    protected void showEndDialog(String winnerName) {
        //LayoutInflater layoutInflater = LayoutInflater.from(this);
        //View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //alertDialogBuilder.setView(promptView);

        //( (EditText) promptView.findViewById(R.id.edittext) ).setText(winnerName);

        // setup a dialog window
        builder
                .setTitle("Fin!")
                .setMessage("Ganador: " + winnerName)
                .setIcon(android.R.drawable.picture_frame)  //FIXME: cambiar icono por uno en condiciones
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetStatistics(DatabaseHelper.SEXO.FEMALE);
                        updateNumberOfNamesUsed();
                        updateNumberOfButtons();
                        updateNumberOfNamesForCountRound();
                        nextRound(null);
                        auxVotaciones=0;
                    }
                });

        builder.create().show();
    }


    private int calculateNumberOfVotesNeeded(long n){
        int totalVotes, buttons;
        long used, votedInRound;

        used            = n;
        totalVotes      = 0;
        votedInRound    = 0;
        buttons         = getOptimalNumberOfButtons(used);
        while (used > DEFAULT_REMAINING_NAMES_TO_END){
            votedInRound += buttons;

            if(votedInRound >= used){
                totalVotes     += votedInRound;
                votedInRound   -= used;
                used            = (int)Math.ceil(used/2f);
                buttons         = getOptimalNumberOfButtons(used);
            }
        }

        return totalVotes;
    }
}
