package com.example.android.sunshine.app.ui.detail;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.model.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
    private static final int FORECAST_LOADER = 0;
    // In this case the id needs to be fully qualified with a table name, since
    // the content provider joins the location & weather tables in the background
    // (both have an _id column)
    // On the one hand, that's annoying.  On the other, you can search the weather table
    // using the location set by the user, which is only in the Location table.
    // So the convenience is worth it.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };
    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private String dateString;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreatView called");

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        if(intent!=null && intent.hasExtra(Intent.EXTRA_TEXT)){
            this.dateString = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onActivityCreated called");
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.detailfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            startActivity(createShareForecastIntent());
            return true;
        }

        return false;
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, dateString + FORECAST_SHARE_HASHTAG);

        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String location = Utility.getLocationPreferences(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, this.dateString);

        return new CursorLoader(
                getActivity(),
                weatherUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String dateStringData = Utility.formatDate(data.getString(COL_WEATHER_DATE));
            String weatherDescription = data.getString(COL_WEATHER_DESC);
            boolean isMetric = Utility.isMetric(getActivity());
            String highTemp = Utility.formatTemperature(
                    data.getDouble(COL_WEATHER_MAX_TEMP),
                    isMetric);
            String lowTemp = Utility.formatTemperature(
                    data.getDouble(COL_WEATHER_MIN_TEMP),
                    isMetric);
            updateDetailsUi(dateStringData, weatherDescription, highTemp, lowTemp);
        }
    }

    private void updateDetailsUi(String dateStringData, String weatherDescription, String highTemp, String lowTemp) {
        TextView textView = (TextView) getActivity().findViewById(R.id.list_item_date_textView);
        textView.setText(dateStringData);

        textView = (TextView) getActivity().findViewById(R.id.list_item_forecast_textView);
        textView.setText(weatherDescription);

        textView = (TextView) getActivity().findViewById(R.id.list_item_high_textView);
        textView.setText(highTemp);

        textView = (TextView) getActivity().findViewById(R.id.list_item_low_textView);
        textView.setText(lowTemp);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
