package app.alcanteria.com.sunshine.app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container);

        String[] forecastArray = {
                                    "Today - Sunny - 88 / 50",
                                    "Tomorrow - Sunny - 50 / 40",
                                    "Monday - Cloudy - 88 / 50",
                                    "Tuesday - Cloudy - 30 / 20",
                                    "Wednesday - Clear - 60 / 50",
                                    "Thursday - Rain - 70 / 60",
                                    "Today - Sunny - 88 / 50",
                                    "Tomorrow - Sunny - 50 / 40",
                                    "Monday - Cloudy - 88 / 50",
                                    "Tuesday - Cloudy - 30 / 20",
                                    "Wednesday - Clear - 60 / 50",
                                    "Thursday - Rain - 70 / 60"
                                };

        ArrayList<String> dummyData = new ArrayList<String>(Arrays.asList(forecastArray));

        ArrayAdapter<String> forecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, dummyData);

        ListView listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);

        return rootView;
    }
}
