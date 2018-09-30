package me.jimmyhuang.travelerstrack.utility;

import org.json.JSONObject;

public class JsonUtil {

    public static int getWeatherTemp(String json) {
        try {
            JSONObject weatherObj = new JSONObject(json);

            JSONObject mainObj = weatherObj.getJSONObject("main");

            return mainObj.optInt("temp", 0);
        } catch (Exception e){
            return 0;
        }
    }
}
