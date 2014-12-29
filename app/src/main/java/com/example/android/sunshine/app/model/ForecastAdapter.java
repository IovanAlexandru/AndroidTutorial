package com.example.android.sunshine.app.model;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.ui.main.ForecastFragment;

/**
 * ForecastAdapter
 */
public class ForecastAdapter extends CursorAdapter {
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;
    private boolean mUseTodayLayout = true;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void setUseTodayLayout(boolean value) {
        this.mUseTodayLayout = value;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && this.mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId;
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
            default:
                throw new UnsupportedOperationException("View type " + viewType + " is unknown");
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
        // Use placeholder image for now
        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                viewHolder.iconView.setImageResource(ImageUtility.getArtResourceForWeatherCondition(weatherId));
                break;
            case VIEW_TYPE_FUTURE_DAY:
                viewHolder.iconView.setImageResource(ImageUtility.getIconResourceForWeatherCondition(weatherId));
                break;
        }

        // Read date from cursor
        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateString));

        // Read weather forecast from cursor
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        // Find TextView and set weather forecast on it
        viewHolder.descriptionView.setText(description);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
        String highStr = Utility.formatTemperature(context, high, isMetric);
        viewHolder.highTempView.setText(highStr);

        // Read low temperature from cursor
        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
        String lowStr = Utility.formatTemperature(context, low, isMetric);
        viewHolder.lowTempView.setText(lowStr);
    }
}
