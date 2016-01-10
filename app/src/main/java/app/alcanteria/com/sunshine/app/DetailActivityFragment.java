package app.alcanteria.com.sunshine.app;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
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
            String forecastString = intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView)rootView.findViewById(R.id.detail_text)).setText(forecastString);
        }
        return rootView;
    }
}
