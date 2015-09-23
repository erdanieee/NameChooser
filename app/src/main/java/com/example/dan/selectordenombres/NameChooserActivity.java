package com.example.dan.selectordenombres;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class NameChooserActivity extends AppCompatActivity {
    private final String DEBUG_TAG = "NameChooserMainActivity";
    private final String URL_SERVER_GET_DATA    = "http://server.bacmine.com/names/getNames.php";
    private final String URL_SERVER_SEND_DATA   = "http://server.bacmine.com/names/sendData.php";
    private TextView title;
    private ArrayList<String> listaNombres;
    Button[] buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_chooser);

        title           = (TextView)findViewById(R.id.TextViewTitle);
        listaNombres    = new ArrayList<String>(10);
        buttons         = new Button[]{
                (Button) findViewById(R.id.button1),
                (Button) findViewById(R.id.button2),
                (Button) findViewById(R.id.button3),
        };

        checkConectivity();





        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
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
        if(!listaNombres.isEmpty()){
            String[] names;

            names = listaNombres.get(0).split(";");

            for (int i=0; i<names.length;i++){
                String[] tokens = names[i].split(":");

                buttons[i].setTag(tokens[0]);
                buttons[i].setText(tokens[1]);
            }

            listaNombres.remove(0);

        } else {
            try {
                Thread.sleep(100);
                setNames();

            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }


    public void onButtonClick(View v){
        String url;

        Button buttonClicked = (Button)v;
        ArrayList<Button> listaButtonsNotClicked = new ArrayList<Button>();

        for (Button b : buttons){
            if (!b.getText().equals(buttonClicked.getText())){
                listaButtonsNotClicked.add(b);
            }
        }

        url = URL_SERVER_SEND_DATA +
                "?" +
                "h=" + buttonClicked.getTag() +
                "&n=" + listaButtonsNotClicked.get(0).getTag() +
                ";" + listaButtonsNotClicked.get(1).getTag();

        new SendDataToServer().execute(url);
        new DownloadDataTask().execute(URL_SERVER_GET_DATA);

        setNames();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    private class DownloadDataTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            while (listaNombres.size()<10) {
                try {
                    for (String s : downloadUrl(urls[0]).split("\n")) {
                        listaNombres.add(s);
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
            reader = new InputStreamReader(stream, "UTF-8");
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
                    //Log.d(DEBUG_TAG, "Sending data: OK!");
                    return "OK";
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

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
