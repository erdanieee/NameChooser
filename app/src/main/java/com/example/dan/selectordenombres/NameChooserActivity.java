package com.example.dan.selectordenombres;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

public class NameChooserActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int INTENT_RESULT_SETTING = 99;
    private final int BUFFER_NAMES_SIZE = 50;
    private final int DEFAULT_NUMBER_OF_BUTTONS = 3;
    private final int DEFAULT_MAX_FREQ_THRESHOLD = 10;
    private final int DEFAULT_MIN_FREQ_THRESHOLD = 50;
    private final String DEBUG_TAG = "NameChooserMainActivity";
    private final String URL_SERVER_GET_DATA    = "http://server.bacmine.com/names/getNames.php";
    private final String URL_SERVER_SEND_DATA   = "http://server.bacmine.com/names/sendData.php";
    private TextView textViewTitle;
    private ArrayList<String> bufferNombres;
    Button[] buttons;
    private LinearLayout layoutButtons;

    private String  pref_userName;
    private int     pref_numberOfButtons;
    private int     pref_freqMax;
    private float   pref_freqMin;
    private int     pref_serverBuffer;
    private String  pref_sexo;
    private boolean pref_useFreq;
    private boolean pref_useCompoundNames;


    //TODO: Compartir en facebook los resultados cuando se encuentre un nombre común entre la pareja.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_chooser);

        textViewTitle   = (TextView)findViewById(R.id.TextViewTitle);
        layoutButtons   = (LinearLayout)findViewById(R.id.linearLayoutButtons);
        bufferNombres   = new ArrayList<String>(pref_serverBuffer +BUFFER_NAMES_SIZE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        checkConectivity();
        updatePreferences();
        textViewTitle.setText("Selecciona el nombre que más te gusta de entre los siguientes:");


        /*Intent i = new Intent(this, NewUserActivity.class);
        startActivity(i);*/


        /*

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }


    private boolean checkConectivity(){
        ConnectivityManager connMgr;
        NetworkInfo networkInfo;

        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d(DEBUG_TAG, "Conectivity OK!");
            return true;

        } else {
            Log.d(DEBUG_TAG, "Fallo de conexión");
            new AlertDialog.Builder(getApplicationContext())
                    .setTitle("Conectividad")
                    .setMessage("No se ha podido establecer la conexión con el servidor.")
                    .setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            checkConectivity();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(DEBUG_TAG, "Dialogo de conexión cancelado");
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        textViewTitle.setText("No se ha podido establecer la conexión con el servidor");
        return false;
    }


    private void updatePreferences() {
        SharedPreferences sharedPref;
        int new_numberOfButtons, new_freqMax, new_freqMin, new_bufferName;
        String new_userName, new_sexo;
        boolean new_useFreq, new_multiNames;

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        new_numberOfButtons = sharedPref.getInt(getString(R.string.pref_numberOfButtons), DEFAULT_NUMBER_OF_BUTTONS);
        new_freqMax         = sharedPref.getInt(getString(R.string.pref_freqMax), DEFAULT_MAX_FREQ_THRESHOLD);
        new_freqMin         = sharedPref.getInt(getString(R.string.pref_freqMin), DEFAULT_MIN_FREQ_THRESHOLD);
        new_bufferName      = sharedPref.getInt(getString(R.string.pref_bufferNombres), 20);
        new_userName        = sharedPref.getString(getString(R.string.pref_userName), null);
        new_sexo            = sharedPref.getString(getString(R.string.pref_sexo), "H");
        new_useFreq         = sharedPref.getBoolean(getString(R.string.pref_useFreq), true);
        new_multiNames      = sharedPref.getBoolean(getString(R.string.pref_useCompoundNames), false);


        if(new_bufferName!=pref_serverBuffer){
            pref_serverBuffer = new_bufferName;
            Log.d(DEBUG_TAG, "New pref buffer: " + pref_serverBuffer);
        }

        if(new_freqMax!= pref_freqMax){
            pref_freqMax = new_freqMax;
            bufferNombres.clear();
            Log.d(DEBUG_TAG, "New pref freq max: " + pref_freqMax);
        }

        if(new_freqMin!= pref_freqMin){
            pref_freqMin = (float)new_freqMin/100;
            bufferNombres.clear();
            Log.d(DEBUG_TAG, "New pref freq min: " + pref_freqMin);
        }

        if(new_sexo!=pref_sexo){
            pref_sexo = new_sexo;
            bufferNombres.clear();
            Log.d(DEBUG_TAG, "New pref sexo: " + pref_sexo);
        }

        if(new_useFreq!= pref_useFreq){
            pref_useFreq = new_useFreq;
            bufferNombres.clear();
            Log.d(DEBUG_TAG, "New pref use freq: " + pref_useFreq);
        }

        if(new_multiNames!=pref_useCompoundNames){
            pref_useCompoundNames = new_multiNames;
            bufferNombres.clear();
            Log.d(DEBUG_TAG, "New pref compound names: " + pref_useCompoundNames);
        }

        if(new_userName==null || new_userName.equals("")){
            showInputDialog();

        } else if(new_userName!=pref_userName){
            pref_userName = new_userName;
            setTitle(pref_userName);
            Log.d(DEBUG_TAG, "New pref user name: " + pref_userName);
        }

        if(new_numberOfButtons!=pref_numberOfButtons){
            pref_numberOfButtons = new_numberOfButtons;
            createButtons();
            Log.d(DEBUG_TAG, "New pref number of buttons: " + pref_numberOfButtons);
        }

        downloadData();
        setNames();
    }


    private void createButtons() {
        Log.d(DEBUG_TAG, "Create buttons");
        layoutButtons.removeAllViews();

        buttons = new Button[pref_numberOfButtons];
        for (int i=0; i< pref_numberOfButtons;i++){
            Button b = new Button(getApplicationContext());
            b.setOnClickListener(this);
            layoutButtons.addView(b);
            buttons[i] = b;
        }
    }


    protected void showInputDialog() {
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
                        updatePreferences();
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
    }


    private void setNames(){
        for (Button b : buttons){
            b.setEnabled(false);
        }

        bufferNombres.trimToSize();
        if(bufferNombres.size()>= pref_numberOfButtons){
            String[] names;

            //names = bufferNombres.get(0).split(";");

            synchronized (this) {
                for (int i = 0; i < pref_numberOfButtons; i++) {
                    String[] tokens;

                    tokens = bufferNombres.get(0).split(":");
                    bufferNombres.remove(0);

                    Log.d(DEBUG_TAG, "Set names: " + bufferNombres.get(0) + " (" + Arrays.toString(tokens) + ")");

                    buttons[i].setTag(tokens[0]);
                    buttons[i].setText(tokens[1]);
                }
            }
            for (Button b : buttons){
                b.setEnabled(true);
            }

        } else {
            try {
                Thread.sleep(50);
                setNames();

            } catch (Exception e){
                e.printStackTrace();
            }
        }
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
            case INTENT_RESULT_SETTING:
                updatePreferences();

                break;
        }
    }














    @Override
    public void onClick(View v) {
        setNames();
        sendData((Button) v);
        downloadData();
    }


    private void downloadData(){
        String url;

        url = URL_SERVER_GET_DATA +
                "?buffer=" + pref_serverBuffer +
                "&sexo=" + pref_sexo +
                (pref_useCompoundNames==true? "&multiName=1":"") +
                (pref_useFreq ?"&freqMax="+ pref_freqMax + "&freqMin="+ pref_freqMin :"");

        Log.d(DEBUG_TAG, "Download url: " + url);
        new DownloadDataTask().execute(url);
    }


    private class DownloadDataTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            synchronized (this) {
                while (bufferNombres.size()<BUFFER_NAMES_SIZE) {
                    try {
                        for (String s : downloadUrl(urls[0]).split(";")) {
                            bufferNombres.add(s);
                        }
                        Log.d(DEBUG_TAG, "Tamaño lista de nombres: " + bufferNombres.size());

                    } catch (IOException e) {
                        return "Unable to retrieve web page. URL may be invalid.";
                    }
                }
            }

            return "OK";
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 5000;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len).trim();
                Log.d(DEBUG_TAG, contentAsString);
                Log.d(DEBUG_TAG, "Download data: " + response);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream,"LATIN1");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
    }










    private void sendData(Button buttonClicked){
        String url;
        StringBuffer idsButtonsNotClicked;

        idsButtonsNotClicked = new StringBuffer();
        for (Button b : buttons){
            if (!b.getText().equals(buttonClicked.getText())){
                idsButtonsNotClicked.append(b.getTag() + ";");
            }
        }

        try {
            url = URL_SERVER_SEND_DATA +
                    "?clicked=" + buttonClicked.getTag() +
                    "&nonClicked=" + idsButtonsNotClicked.substring(0,idsButtonsNotClicked.length()-1) +
                    "&userName=" + URLEncoder.encode(pref_userName, "LATIN1");

            Log.d(DEBUG_TAG, "Send url: " + url);
            new SendDataToServer().execute(url);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private class SendDataToServer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                if(sendData(urls[0])){
                    return "OK";
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            //Toast.makeText(getApplicationContext(), "Error al enviar datos", Toast.LENGTH_LONG).show();
            return "BAD";
        }

        private boolean sendData(String myurl) throws IOException {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(20000 /* milliseconds */);
            conn.setConnectTimeout(30000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "Sending data: " + response);

            if (response==HttpURLConnection.HTTP_OK){
                return true;

            } else {
                return false;
            }
        }
    }
}
