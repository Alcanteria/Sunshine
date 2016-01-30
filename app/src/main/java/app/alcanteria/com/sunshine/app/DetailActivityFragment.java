package app.alcanteria.com.sunshine.app;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String forecastString;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Grab the passed intent.
        Intent intent = getActivity().getIntent();

        // Grab the root view.
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        /** Make sure the intent actually has extra text packed with it
         * and set the text view in the detail activity to the packaged text. */
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            forecastString = intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView)rootView.findViewById(R.id.detail_text)).setText(forecastString);
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){

        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve share item.
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider shareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider
        if(shareActionProvider != null){
            shareActionProvider.setShareIntent(createShareForecstIntent());
        }
        else{
            Log.d(LOG_TAG, "Share Action Provider Is Null.");
        }
    }

    private Intent createShareForecstIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecastString + FORECAST_SHARE_HASHTAG);

        return shareIntent;
    }
}
