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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class NameChooserActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int INTENT_RESULT_SETTING = 1;
    private final int BUFFER_NAMES_SIZE = 9;
    private final int SERVER_SIZE_BUFFER = 30;
    private final int DEFAULT_NUMBER_OF_BUTTONS = 3;
    private final String DEBUG_TAG = "NameChooserMainActivity";
    private final String URL_SERVER_GET_DATA    = "http://server.bacmine.com/names/getNames.php";
    private final String URL_SERVER_SEND_DATA   = "http://server.bacmine.com/names/sendData.php";
    private TextView textViewTitle;
    private ArrayList<String> listaNombres;
    Button[] buttons;
    private LinearLayout layoutButtons;
    private String pref_userName;
    private int pref_numberOfButtons;
    private String pref_sexo;
    private boolean pref_useFreq;
    private int pref_freq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().setTitle("Hola " + pref_userName);
        setContentView(R.layout.activity_name_chooser);

        readPreferences();

        textViewTitle   = (TextView)findViewById(R.id.TextViewTitle);
        layoutButtons   = (LinearLayout)findViewById(R.id.linearLayoutButtons);
        listaNombres    = new ArrayList<String>(SERVER_SIZE_BUFFER+BUFFER_NAMES_SIZE);

        createButtons();

        if ( pref_userName == null ){
            showInputDialog();
        }

        checkConectivity();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(pref_userName);

        textViewTitle.setText("Selecciona el nombre que más te gusta de entre los siguientes:");

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

    private void createButtons() {
        layoutButtons.removeAllViews();

        buttons = new Button[pref_numberOfButtons];
        for (int i=0; i< pref_numberOfButtons;i++){
            Button b = new Button(getApplicationContext());
            b.setOnClickListener(this);
            layoutButtons.addView(b);
            buttons[i] = b;
        }
    }

    private void readPreferences() {
        SharedPreferences sharedPref;

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        pref_numberOfButtons    = sharedPref.getInt(getString(R.string.pref_numberOfButtons), DEFAULT_NUMBER_OF_BUTTONS);
        pref_userName           = sharedPref.getString(getString(R.string.pref_userName), "Yo");
        //pref_sexo               = sharedPref.getString(getString(R.string.), "Yo");
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
                        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                        editor.putString(getString(R.string.pref_userName), pref_userName.substring(0, 1).toUpperCase() + pref_userName.substring(1));
                        editor.commit();
                        setTitle(pref_userName);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    private void checkConectivity(){
        ConnectivityManager connMgr;
        NetworkInfo networkInfo;

        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadDataTask().execute(URL_SERVER_GET_DATA);
            setNames();

        } else {
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
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }


    private void setNames(){
        if(listaNombres.size()>= pref_numberOfButtons){
            String[] names;

            //names = listaNombres.get(0).split(";");

            for (int i=0; i< pref_numberOfButtons;i++){
                String[] tokens = listaNombres.get(0).split(":");

                buttons[i].setTag(tokens[0]);
                buttons[i].setText(tokens[1]);

                listaNombres.remove(0);
            }

        } else {
            try {
                Thread.sleep(100);
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
                readPreferences();

                setTitle(pref_userName);
                createButtons();
                setNames();

                break;
        }

    }



    @Override
    public void onClick(View v) {
        Button buttonClicked;
        String url;
        StringBuffer idsButtonsNotClicked;


        buttonClicked = (Button)v;
        idsButtonsNotClicked = new StringBuffer();

        for (Button b : buttons){
            if (!b.getText().equals(buttonClicked.getText())){
                idsButtonsNotClicked.append(b.getTag() + ";");
            }
        }

        try {
            url = URL_SERVER_SEND_DATA +
                    "?" +
                    "clicked=" + buttonClicked.getTag() +
                    "&nonClicked=" + idsButtonsNotClicked.substring(0,idsButtonsNotClicked.length()-1) + //listaButtonsNotClicked.get(0).getTag() + ";" + listaButtonsNotClicked.get(1).getTag() +
                    "&pref_userName=" + URLEncoder.encode(pref_userName, "LATIN1");

            new SendDataToServer().execute(url);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        new DownloadDataTask().execute(URL_SERVER_GET_DATA);

        setNames();
    }


    private class DownloadDataTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            while (listaNombres.size()<BUFFER_NAMES_SIZE) {
                try {
                    for (String s : downloadUrl(urls[0]).split(";")) {
                        if (!listaNombres.contains(s)){
                            listaNombres.add(s);
                        }
                    }
                    Log.d(DEBUG_TAG, "Tamaño lista de nombres: " + listaNombres.size());

                } catch (IOException e) {
                    return "Unable to retrieve web page. URL may be invalid.";
                }
            }

            return "OK";
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

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
                Log.d(DEBUG_TAG, "Download data: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
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

            Toast.makeText(getApplicationContext(), "Error al enviar datos", Toast.LENGTH_LONG).show();
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
