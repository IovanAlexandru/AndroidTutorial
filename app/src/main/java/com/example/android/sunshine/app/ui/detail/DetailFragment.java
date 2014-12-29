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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.model.ImageUtility;
import com.example.android.sunshine.app.model.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private static final String LOCATION_KEY = "location";
    private static final int DETAILS_LOADER = 0;
    // In this case the id needs to be fully qualified with a table name, since
    // the content provider joins the location & weather tables in the background
    // (both have an _id column)
    // On the one hand, that's annoying.  On the other, you can search the weather table
    // using the location set by the user, which is only in the Location table.
    // So the convenience is worth it.
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    private String dateString;
    private String mForecast;

    private ImageView mIconView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mFriendlyDateView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreatView called");

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(DetailActivity.DATE_KEY)) {
            this.dateString = getArguments().getString(DetailActivity.DATE_KEY);
        }

        initMemberVariables(rootView);

        return rootView;
    }

    private void initMemberVariables(View view) {
        mIconView = (ImageView) view.findViewById(R.id.detail_icon);
        mDateView = (TextView) view.findViewById(R.id.detail_date_textView);
        mFriendlyDateView = (TextView) view.findViewById(R.id.detail_day_textView);
        mDescriptionView = (TextView) view.findViewById(R.id.detail_forecast_textView);
        mHighTempView = (TextView) view.findViewById(R.id.detail_high_textView);
        mLowTempView = (TextView) view.findViewById(R.id.detail_low_textView);
        mHumidityView = (TextView) view.findViewById(R.id.detail_humidity_textView);
        mWindView = (TextView) view.findViewById(R.id.detail_wind_textView);
        mPressureView = (TextView) view.findViewById(R.id.detail_pressure_textView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onActivityCreated called");
        if (this.dateString != null) {
            getLoaderManager().initLoader(DETAILS_LOADER, null, this);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);

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
        if (data != null && data.moveToFirst()) {
            int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            // Use placeholder Image
            mIconView.setImageResource(ImageUtility.getArtResourceForWeatherCondition(weatherId));

            // Read date from cursor and update views for day of week and date
            String date = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT));
            String friendlyDateText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(date);
            mFriendlyDateView.setText(friendlyDateText);
            mDateView.setText(dateText);

            // Read description from cursor and update view
            String description = data.getString(data.getColumnIndex(
                    WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
            mDescriptionView.setText(description);

            // Read high temperature from cursor and update view
            boolean isMetric = Utility.isMetric(getActivity());

            double high = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
            String highString = Utility.formatTemperature(getActivity(), high, isMetric);
            mHighTempView.setText(highString);

            // Read low temperature from cursor and update view
            double low = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
            String lowString = Utility.formatTemperature(getActivity(), low, isMetric);
            mLowTempView.setText(lowString);

            // Read humidity from cursor and update view
            float humidity = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY));
            mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

            // Read wind speed and direction from cursor and update view
            float windSpeedStr = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
            float windDirStr = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES));
            mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

            // Read pressure from cursor and update view
            float pressure = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE));
            mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

            // We still need this for the share intent
            mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
