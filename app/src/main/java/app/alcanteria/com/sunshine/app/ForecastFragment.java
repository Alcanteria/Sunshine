package app.alcanteria.com.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    // This is the API ID key. You need this to be able to access open weather.
    public final String API_KEY = "&id=4140963&APPID=7b659b85252e20659cd4ea9d4c9b38a0";

    // Array adapter to hold the forecast data.
    public ArrayAdapter<String> forecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container);

        forecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, new ArrayList<String>());

        ListView listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String forecast = forecastAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather(){
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        String location = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        weatherTask.execute(location);
    }

    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }

    /* Performs the network activity to retrieve the weather forecast data. */
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        protected String[] doInBackground(String... params){

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON result as a string.
            String forecastJsonStr = null;

            // Parameters for URL builder
            String format = "json";
            String units = "metric";
            int numDays = 7;

            try{
                /* CREATE A URL DYNAMICALLY BASED ON USER SUPPLIED PARAMETERS. */

                // The beginning url contents
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/";

                // This is for current weather
                final String FORECAST_CURRENT = "weather?";

                // This is for a 5 day forecast
                final String FORECAST_FIVE_DAY = "forecast?";

                // This is the City ID
                //final String CITY_ID = "&id=" + params[0];

                // This is the name of the city.
                final String CITY_NAME = "q=" + params[0];
                //final String CITY_NAME = "q=Washington, D.C.";

                // Country code - DEFAULTS TO U.S.A.
                final String COUNTRY_CODE = ",us";

                // Unit of temperature measurements.
                final String TEMP_UNITS = "&units=imperial";

                // Put it all together.
                String dynamicUrl = FORECAST_BASE_URL +
                                    FORECAST_FIVE_DAY +
                                    CITY_NAME +
                                    COUNTRY_CODE +
                                    TEMP_UNITS +
                                    API_KEY;

                // Create the string for the OpenWeather query
                //Uri uri = new URI(dynamicUrl.replace(" ", "%20"));
                URL url = new URL(dynamicUrl.replace(" ", "%20"));

                // Check yo'self
                Log.v(LOG_TAG, "Built Uri " + dynamicUrl);

                // Create the request to openweather and open the connection.
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a string.
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if(inputStream == null){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null){
                    // Insert the JSON result into a string buffer, line by line.
                    buffer.append(line + "\n");
                }

                // Check if the buffer is empty, which would be no data was transferred.
                if(buffer.length() == 0)
                    return null;

                forecastJsonStr = buffer.toString();

            }
            catch(IOException e){
                Log.e(LOG_TAG, "Error ", e);
                return null;
            }
            finally{
                if(urlConnection != null){
                    urlConnection.disconnect();

                    if(reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error Closing Stream", e);
                        }
                    }
                }
            }

            try{
                WeatherDataParser parser = new WeatherDataParser();
                return parser.getWeatherDataFromJson(forecastJsonStr);
            }
            catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(String[] results){
            if(results != null){
                forecastAdapter.clear();
                for(String dayForecastString : results)
                    forecastAdapter.add(dayForecastString);
            }
        }


    }
}
