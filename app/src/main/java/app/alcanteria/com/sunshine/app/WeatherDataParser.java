package app.alcanteria.com.sunshine.app;
/**
 * Created by Nick on 1/9/2016.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import  org.json.JSONArray;
import  org.json.JSONException;
import  org.json.JSONObject;

import java.text.SimpleDateFormat;

public class WeatherDataParser {

    public final String LOG_TAG = "WeatherDataParser";

    private Context context;

    public WeatherDataParser(Context current){
        this.context = current;
    }

    private double GetMaxTemperatureForDay(String weatherJSONString, int dayIndex) throws JSONException {

        JSONObject weather = new JSONObject(weatherJSONString);
        JSONArray days = weather.getJSONArray("list");
        JSONObject dayInfo = days.getJSONObject(dayIndex);
        JSONObject temperatureInfo = dayInfo.getJSONObject("temp");

        return temperatureInfo.getDouble("max");
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public String[] getWeatherDataFromJson(String forecastJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "temp_max";
        final String OWM_MIN = "temp_min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();
        //Log.v(LOG_TAG, "weatherArray Size = " + weatherArray.length());
        String[] resultStrs = new String[weatherArray.length()];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            //JSONObject mainObject = dayForecast.getJSONObject(OWM_DESCRIPTION);
            //JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_DESCRIPTION);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = FormatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        for (String s : resultStrs) {
            //Log.v(LOG_TAG, "Forecast entry: " + s);
        }
        return resultStrs;


    }

    /** Format the weather results for high/low display. */
    private String FormatHighLows(double high, double low){
            /* Weather results are returned as Celsius by default. This formats the weather values
            * based on the user's preferences. */
        // Get the user preferences ready to pass along to the weather data parser.
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String unitType = sharedPrefs.getString(
                context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric));

        if(unitType.equals(context.getString(R.string.pref_units_imperial))){
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        }
        else if(!unitType.equals(context.getString(R.string.pref_units_metric))){
            Log.d(LOG_TAG, "Unit type not found: " + unitType);
        }

        // Round to whole numbers.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        // Format into a single string.
        String highLowString = roundedHigh + "/" + roundedLow;

        return highLowString;
    }
}
