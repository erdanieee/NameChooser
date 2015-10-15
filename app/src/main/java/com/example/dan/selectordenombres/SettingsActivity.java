package com.example.dan.selectordenombres;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    private static final String DEBUG_TAG = "Setting activity";
    public static int count;
    public static Preference prefUseFilter =null;


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceCountListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String url, sexo;
            int freqMax, freqMin;
            Boolean useFreq, useMultiName, useFilters;
            boolean prefCompNames;

            if (!(preference instanceof CheckBoxPreference)) {
                preference.setSummary(value.toString());
            }

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).edit();
            if(value instanceof String){
                editor.putString(preference.getKey(), (String)value);

            } else if (value instanceof Boolean){
                editor.putBoolean(preference.getKey(), (boolean)value);

            } else if (value instanceof Integer){
                editor.putInt(preference.getKey(), (int)value);
            }
            editor.commit();

            sexo            = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getContext().getResources().getString(R.string.pref_sexo), null);
            useMultiName    = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getBoolean(preference.getContext().getResources().getString(R.string.pref_useCompoundNames), false);
            freqMax         = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getInt(preference.getContext().getResources().getString(R.string.pref_freqMax), 10);
            freqMin         = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getInt(preference.getContext().getResources().getString(R.string.pref_freqMin), 1);
            useFreq         = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getBoolean(preference.getContext().getResources().getString(R.string.pref_useFreq), false);
            useFilters      = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getBoolean(preference.getContext().getResources().getString(R.string.pref_filtrarNombres), true);

            url = NameChooserActivity.URL_SERVER_GET_DATA +
                    "?sexo=" + sexo +
                    (useFilters?
                        (useMultiName? "&multiName=1":"") + (useFreq? "&freqMax="+ freqMax + "&freqMin="+ (float)freqMin/100 : "")
                        : "&multiName=1") +
                    "&count=1";

            Log.d(DEBUG_TAG, "Count URL: " + url);
            AsyncTask<String, Void, String> d = new DownloadDataTask();
            d.execute(url);

            try {
                prefUseFilter.setSummary("Total: " + d.get() + " nombres.");

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            return true;
        }
    };


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryStringToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), null));
    }
    private static void bindPreferenceSummaryIntToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getInt(preference.getKey(), -1));
    }
    private static void bindPreferenceSummaryIntCount(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceCountListener);
        sBindPreferenceCountListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getInt(preference.getKey(), -1));
    }
    private static void bindPreferenceSummaryStringCount(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceCountListener);
        sBindPreferenceCountListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), null));
    }
    private static void bindPreferenceSummaryBooleanCount(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceCountListener);
        sBindPreferenceCountListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getBoolean(preference.getKey(), false));
    }

    protected void updateCount(String text){
        synchronized (prefUseFilter) {
            prefUseFilter.setSummary(text);
        }
    }


    protected boolean isValidFragment (String fragmentName){
        return true;
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryStringToValue(findPreference(getResources().getString(R.string.pref_userName)));
            bindPreferenceSummaryIntToValue(findPreference(getResources().getString(R.string.pref_numberOfButtons)));
        }


    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_servidor);

            prefUseFilter = findPreference(getResources().getString(R.string.pref_filtrarNombres));

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryIntToValue(findPreference(getResources().getString(R.string.pref_bufferNombres)));
            bindPreferenceSummaryStringCount(findPreference(getResources().getString(R.string.pref_sexo)));
            bindPreferenceSummaryIntCount(findPreference(getResources().getString(R.string.pref_freqMax)));
            bindPreferenceSummaryIntCount(findPreference(getResources().getString(R.string.pref_freqMin)));
            bindPreferenceSummaryBooleanCount(findPreference(getResources().getString(R.string.pref_useCompoundNames)));
            bindPreferenceSummaryBooleanCount(findPreference(getResources().getString(R.string.pref_useFreq)));
            bindPreferenceSummaryBooleanCount(findPreference(getResources().getString(R.string.pref_filtrarNombres)));
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
   /* @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryStringToValue(findPreference("sync_frequency"));
        }
    }*/




    protected static class DownloadDataTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            String count="";

            try {
                count=downloadUrl(urls[0]);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return count;
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 50;

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
                Log.d(DEBUG_TAG, "Select count response: " + response);
                Log.d(DEBUG_TAG, contentAsString);
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
}
