package me.jimmyhuang.travelerstrack.utility;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import me.jimmyhuang.travelerstrack.BuildConfig;

public class NetworkUtil {
    private final static String WEATHER_BASE_URL = "http://api.openweathermap.org/data/2.5/weather";
    private final static String WEATHER_UNIT_PARAM = "units";
    private final static String WEATHER_LAT_PARAM = "lat";
    private final static String WEATHER_LON_PARAM = "lon";
    private final static String WEATHER_API_PARAM = "apiKey";

    public static URL buildWeatherUrl(String lat, String lon) {
        Uri uri = Uri.parse(WEATHER_BASE_URL).buildUpon()
                .appendQueryParameter(WEATHER_UNIT_PARAM, "imperial")
                .appendQueryParameter(WEATHER_LAT_PARAM, lat)
                .appendQueryParameter(WEATHER_LON_PARAM,  lon)
                .appendQueryParameter(WEATHER_API_PARAM, BuildConfig.OpenWeatherApiKey)
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
