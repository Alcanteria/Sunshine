package app.alcanteria.com.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    // This is the API ID key. You need this to be able to access open weather.
    public final String API_KEY = "id=4140963&APPID=7b659b85252e20659cd4ea9d4c9b38a0";

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

        String[] forecastArray = {
                                    "DEFAULT Today - Sunny - 88 / 50",
                                    "DEFAULT Tomorrow - Sunny - 50 / 40",
                                    "DEFAULT Monday - Cloudy - 88 / 50",
                                    "DEFAULT Tuesday - Cloudy - 30 / 20",
                                    "DEFAULT Wednesday - Clear - 60 / 50",
                                    "DEFAULT Thursday - Rain - 70 / 60",
                                    "DEFAULT Today - Sunny - 88 / 50",
                                    "DEFAULT Tomorrow - Sunny - 50 / 40",
                                    "DEFAULT Monday - Cloudy - 88 / 50",
                                    "DEFAULT Tuesday - Cloudy - 30 / 20",
                                    "DEFAULT Wednesday - Clear - 60 / 50",
                                    "DEFAULT Thursday - Rain - 70 / 60"
                                };

        ArrayList<String> dummyData = new ArrayList<String>(Arrays.asList(forecastArray));

        forecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, dummyData);

        ListView listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);

        /************************************** FOR REALZ WEATHER API CALL */





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
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute("4140963");
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                final String CITY_ID = "&id=" + params[0];

                // Unit of temperature measurements.
                final String TEMP_UNITS = "&units=imperial";

                // Put it all together.
                String dynamicUrl = FORECAST_BASE_URL +
                                    FORECAST_FIVE_DAY +
                                    TEMP_UNITS +
                                    CITY_ID +
                                    API_KEY;

                // Create the string for the OpenWeather query
                URL url = new URL(dynamicUrl);

                // Check yo'self
                //Log.v(LOG_TAG, "Built Uri " + dynamicUrl);

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
