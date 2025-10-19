import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class WeatherApp {

    public static JSONArray LocationData(String locationName) {
        try {
            // Encode the location to make sure there are no special characters in the URL
            locationName = URLEncoder.encode(locationName, "UTF-8");
            String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + locationName +
                    "&count=5&language=en&format=json";

            HttpURLConnection connection = fetchAPIResponse(urlString);

            if (connection.getResponseCode() != 200) {
                System.out.println("Error: " + connection.getResponseMessage());
                return null;
            } else {
                StringBuilder resultJSON = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNext()) {
                    resultJSON.append(scanner.nextLine());
                }
                scanner.close();
                connection.disconnect();

                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(resultJSON.toString());
                return (JSONArray) jsonObject.get("results");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getWeatherData(double latitude, double longitude) {
        try {
            String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
                    "&longitude=" + longitude +
                    "&hourly=temperature_2m,weathercode&timezone=auto";

            HttpURLConnection connection = fetchAPIResponse(urlString);
            if (connection.getResponseCode() != 200) {
                System.out.println("Error: " + connection.getResponseMessage());
                return null;
            } else {
                StringBuilder resultJSON = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNext()) {
                    resultJSON.append(scanner.nextLine());
                }
                scanner.close();
                connection.disconnect();

                JSONParser parser = new JSONParser();
                return (JSONObject) parser.parse(resultJSON.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection fetchAPIResponse(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        return conn;
    }
}
