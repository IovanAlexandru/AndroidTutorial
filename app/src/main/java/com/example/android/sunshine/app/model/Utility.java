package com.example.android.sunshine.app.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.data.WeatherContract;

import java.text.DateFormat;
import java.util.Date;

/**
 * Utility class. Used for getting settings information and also for handling formatting.
 */
public class Utility {
    public static boolean isMetric(Context context) {
        return getPreferredTemperatureUnit(context).equals(context.getString(R.string.pref_unit_metric));
    }

    public static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }

        return String.format("%.0f", temp);
    }

    public static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }

    public static String getLocationPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static String getPreferredTemperatureUnit(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(context.getString(R.string.pref_unit_key),
                context.getString(R.string.pref_unit_metric));
    }
}
