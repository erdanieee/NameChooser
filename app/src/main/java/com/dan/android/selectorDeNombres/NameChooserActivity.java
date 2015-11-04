package com.dan.android.selectorDeNombres;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dan.android.database.DatabaseHelper;
import com.dan.android.database.Nombre;
import com.dan.android.selectordenombres.R;

import java.util.ArrayList;
import java.util.Random;


//TODO: Compartir en facebook los resultados cuando se encuentre un nombre común entre la pareja.


public class NameChooserActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String ID_PAYPAL = "3KHX7F9GQL6G8";
    private static final String ID_MARKET = "com.dan.android.selectordenombres";

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
    private AlertDialog         mConfigDialog=null;
    private float               pref_totalVotacionesNecesarias;
    private TextView            mTextViewCountConfig;
    private ToggleButton        mToggleButtonConfig;
    private SeekBar             mSeekBarConfig;
    private boolean             mContinueSearch;
    private boolean             mFastMode;
    private Switch              mExtendedModeSwitch;
    private boolean             mFirstRun;
    private Long                mTotalCount=null;
    private ArrayList<Nombre>   mUndelete;



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
        mUndelete       = new ArrayList<>();

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextRound();
            }
        });

        percentButton.setImageDrawable(new TextDrawable("0%", this));
        getPreferences();

        if(mContinueSearch){
            nextRound(null, true);

        } else {
            showConfigDialog();
            if (mFirstRun){
                showWellcomeDialog();

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putBoolean(getString(R.string.pref_firstStart), false);
                editor.apply();
                mFirstRun=false;
            }
        }
    }


    private void getPreferences() {
        SharedPreferences sharedPref;

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        pref_totalVotacionesHechas      = sharedPref.getLong(getString(R.string.pref_totalVotesDone), 0);
        pref_totalVotacionesNecesarias  = sharedPref.getFloat(getString(R.string.pref_totalVotesNeeded), 0);      //Default=0 porque ya se calculará cuando se reinicien las estadísticas
        mContinueSearch                 = sharedPref.getBoolean(getString(R.string.pref_continueSearch), false);
        mFastMode                       = sharedPref.getBoolean(getString(R.string.pref_fastMode), false);
        mFirstRun                       = sharedPref.getBoolean(getString(R.string.pref_firstStart), true);
    }


    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putLong(getString(R.string.pref_totalVotesDone), pref_totalVotacionesHechas);
        editor.putFloat(getString(R.string.pref_totalVotesNeeded), pref_totalVotacionesNecesarias);
        editor.putBoolean(getString(R.string.pref_fastMode), mFastMode);
        editor.apply();

        mDb.close();

        super.onStop();
    }

    private void showConfigDialog() {
        LayoutInflater layoutInflater;
        AlertDialog.Builder builder;
        View promptView;
        Random r;

        if(mConfigDialog==null) {
            layoutInflater  = LayoutInflater.from(this);
            promptView      = layoutInflater.inflate(R.layout.initial_option_dialog, null, false);
            builder         = new AlertDialog.Builder(this);
            r               = new Random();

            mTextViewCountConfig    = (TextView) promptView.findViewById(R.id.textViewCount);
            mSeekBarConfig          = (SeekBar) promptView.findViewById(R.id.seekBar);
            mToggleButtonConfig     = (ToggleButton) promptView.findViewById(R.id.toggleButton);
            mExtendedModeSwitch     = (Switch) promptView.findViewById(R.id.switch1);

            mToggleButtonConfig.setChecked(r.nextBoolean());
            mToggleButtonConfig.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    updateTextViewNumberOfNames(true);
                }
            });

            mSeekBarConfig.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    updateSeekBarConfigColor();
                    updateTextViewNumberOfNames(false);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    updateTextViewNumberOfNames(true);
                }
            });

            updateTextViewNumberOfNames(true);
            updateSeekBarConfigColor();

            builder.setView(promptView);
            builder
                    .setTitle(getResources().getString(R.string.configuration_title))
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            resetStatistics(getSexOptionDialog(), getPercentOptionDialog(), ! mExtendedModeSwitch.isChecked());
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetStatistics(getSexOptionDialog(), getPercentOptionDialog(), ! mExtendedModeSwitch.isChecked());
                        }
                    });

            mConfigDialog = builder.create();
        }
        mConfigDialog.show();
    }

    private void updateSeekBarConfigColor(){
        mSeekBarConfig.getProgressDrawable().setColorFilter(
                new LightingColorFilter(
                        0xFF000000,
                        Color.rgb(
                                (int) Math.round(mSeekBarConfig.getProgress() * 2.55),
                                (int) Math.round((mSeekBarConfig.getMax() - mSeekBarConfig.getProgress()) * 2.55),
                                0)));
    }


    private void updateTextViewNumberOfNames(boolean getFromDb) {
        if(getFromDb || mTotalCount==null){
            mTotalCount = mDb.getCountSexo(getSexOptionDialog());
        }
        mTextViewCountConfig.setText(String.valueOf(mTotalCount * getPercentOptionDialog() / 100));
    }


    private DatabaseHelper.SEXO getSexOptionDialog(){
        return mToggleButtonConfig.isChecked() ? DatabaseHelper.SEXO.MALE : DatabaseHelper.SEXO.FEMALE;
    }


    private int getPercentOptionDialog(){
        return mSeekBarConfig.getProgress() + 1;
    }


    private void resetStatistics(DatabaseHelper.SEXO s, int percentSelected, boolean fastMode) {
        mDb.resetTable(s, percentSelected);

        mFastMode                  = fastMode;
        pref_totalVotacionesHechas = 0;

        updateNumberOfNamesUsed();
        pref_totalVotacionesNecesarias = calculateNumberOfVotesNeeded(getNumberOfNamesUsed());

        nextRound(null, true);
    }




    private void setNames(){ setNames(true);}
    private void setNames(boolean fromDb){
        ArrayList<Nombre> nombres;
        int i;
        Button b;

        if(fromDb) {
            nombres = mDb.getUsedNamesByRandomAndCount(getNumberOfButtons());

        } else {
            nombres = mUndelete;
        }

        i = 0;
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

        } else if (id == R.id.action_donate){
            donatePayPal();
            return true;

        } else if (id == R.id.action_vote){
            voteMarket();
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

    /**
     * Muestra una nueva ronda de nombres
     *  **/
    public void nextRound(){ nextRound(null);}
    /**
     * Muestra una nueva ronda de nombres
     * @param v {@link View} que contiene en su <i>Tag</i> el {@link Nombre} que se ha elegido. Si es null, se asume que no se ha elegido ningún nombre en esa ronda porque se ha pulsado el botón skip
     *  **/
    public void nextRound(View v){ nextRound(v, false);}
    /**
     * Muestra una nueva ronda de nombres
     * @param v {@link View} que contiene en su <i>Tag</i> el {@link Nombre} que se ha elegido. Si es null, se asume que no se ha elegido ningún nombre en esa ronda porque se ha pulsado el botón skip
     * @param first Si es <b>true</b> reinicia las variables y muestra una nueva ronda. Si es <b>false</b> graba resultados de la ronda anterior y muestra una nueva ronda de nombres
     *  **/
    public void nextRound(View v, boolean first){           //first=true -> primera ronda
        Nombre n;
        float maxScore;

        //comprueba si se ha terminado
        if (getNumberOfNamesUsed() <= DEFAULT_REMAINING_NAMES_TO_END) {
            percentButton.setImageDrawable(new TextDrawable("100%", this));
            showEndDialog(mDb.getHighestScoreName().nombre);
            mUndelete.clear();

        } else {
            if(first) {
                mContinueSearch = true;
                updateNumberOfNamesUsed();
                updateNumberOfButtons();
                updateNumberOfNamesForCountRound();
                mUndelete.clear();

            } else {
                pref_totalVotacionesHechas += getNumberOfButtons();

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
                if (getNumberOfNamesForCountRound() <= 0) {
                    mDb.unUseLastNNamesByScore((int) Math.floor(getNumberOfNamesUsed() - (float) getNumberOfNamesUsed() / (mFastMode ? getNumberOfButtons() : 2)));
                    updateNumberOfNamesUsed();
                    updateNumberOfButtons();
                    updateNumberOfNamesForCountRound();
                }

                //save buttons for undelete
                mUndelete.clear();
                for (int i = 0; i < mLayoutButtons.getChildCount(); i++) {
                    mUndelete.add((Nombre) mLayoutButtons.getChildAt(i).getTag());
                }
            }

            percentButton.setImageDrawable(new TextDrawable(String.valueOf(
                    (int) Math.floor(100 * pref_totalVotacionesHechas / pref_totalVotacionesNecesarias)
            ) + "%", this));

            setNames();
        }
    }


    public void undelete(View v){
        if (!mUndelete.isEmpty()){
            pref_totalVotacionesHechas -= getNumberOfButtons();

            mDb.undo(mUndelete);

            updateNumberOfNamesUsed();
            updateNumberOfButtons();
            updateNumberOfNamesForCountRound();

            percentButton.setImageDrawable(new TextDrawable(String.valueOf(
                    (int) Math.floor(100 * pref_totalVotacionesHechas / pref_totalVotacionesNecesarias)
            ) + "%", this));

            setNames(false);

            mUndelete.clear();
        }
    }


    protected void showEndDialog(String winnerName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // setup a dialog window
        builder
                .setTitle(getResources().getString(R.string.end_dialog_title))
                .setMessage(getResources().getString(R.string.end_dialog_winnerName) + " " + winnerName + getResources().getString(R.string.end_dialog_startAgain))
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //showConfigDialog();
                                showDonateDialog();
                            }
                        });

        builder.create().show();
    }


    protected void showWellcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // setup a dialog window
        builder
                .setTitle(getResources().getString(R.string.welcome_dialog_title))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(getResources().getString(R.string.welcome_dialog_msg))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }


    protected void showDonateDialog() {
        LayoutInflater layoutInflater;
        AlertDialog.Builder builder;
        View promptView;
        ImageButton ibDonate, ibRate;


        layoutInflater  = LayoutInflater.from(this);
        promptView      = layoutInflater.inflate(R.layout.donate_vote_dialog, null, false);
        builder         = new AlertDialog.Builder(this);
        ibDonate        = (ImageButton) promptView.findViewById(R.id.imageButtonDonate);
        ibRate          = (ImageButton) promptView.findViewById(R.id.imageButtonRate);

        ibDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donatePayPal();
            }
        });

        ibRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voteMarket();
            }
        });

        builder.setView(promptView);
        builder
                .setTitle(getResources().getString(R.string.donate_dialog_title))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        showConfigDialog();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.noThanks), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showConfigDialog();
                    }
                });

        builder.create().show();
    }

    private void donatePayPal(){
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=" + ID_PAYPAL));
        startActivity(intent);
    }

    private void voteMarket(){
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + ID_MARKET));
        startActivity(intent);
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
        while (used > DEFAULT_REMAINING_NAMES_TO_END){
            votedInRound += buttons;
            totalClicks++;

            if(votedInRound > used){
                totalVotes     += votedInRound;
                votedInRound   -= used;
                used            = (int) Math.ceil((float)used/(mFastMode ? buttons : 2));
                buttons         = getOptimalNumberOfButtons(used);
            }
        }

        ret = clicks ? totalClicks : totalVotes;

        return ret;
    }
}
