package com.example.android.sunshine.app.model;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.android.sunshine.app.data.WeatherDataParser;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class FetchWeatherTask extends AsyncTask<Void, Void, String[]> {
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private final String location;
    private final ArrayAdapter<String> adapter;
    private final String temperatureUnit;

    public FetchWeatherTask(String location, ArrayAdapter<String> adapter, String temperatureUnit) {
        this.location = location;
        this.adapter = adapter;
        this.temperatureUnit = temperatureUnit;
    }

    @Override
    protected String[] doInBackground(Void... params) {
        final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";

        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, location)
                .appendQueryParameter(FORMAT_PARAM, "json")
                .appendQueryParameter(UNITS_PARAM, "metric")
                .appendQueryParameter(DAYS_PARAM, Integer.toString(7))
                .build();

        URL url = null;
        try {
            Log.d(LOG_TAG, "Build uri: " + builtUri.toString());
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error", e);
        }
        String dataJson = getForecastJson(url);
        try {
            String[] data = WeatherDataParser.getWeatherDataFromJson(
                    dataJson,
                    this.temperatureUnit);
            return data;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Parsing json error", e);
            return new String[0];
        }
    }

    @Override
    protected void onPostExecute(String[] results) {
        if (results != null) {
            this.adapter.clear();
            this.adapter.addAll(Arrays.asList(results));
        }
    }

    private String getForecastJson(URL url) {
        String forecastJson = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();
            if (inputStream == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }

            if (builder.length() == 0) {
                return null;
            }

            forecastJson = builder.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        Log.d(LOG_TAG, forecastJson);
        return forecastJson;
    }
}