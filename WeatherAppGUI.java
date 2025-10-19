import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WeatherAppGUI extends JFrame {

    private JTextField searchField;
    private JLabel temperatureLabel, weatherLabel, locationLabel, weatherIconLabel;
    private JComboBox<String> cityDropdown;//hardest thing to make here
    private JTextArea forecastArea;
    private static final String FILE_PATH = "searched_cities.txt";// actually this was the hardest

    public WeatherAppGUI() {
        super("Weather App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();

        // Creating search panel
        JPanel searchPanel = createSearchPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(searchPanel, gbc);

        // Creating weather info panel
        JPanel weatherInfoPanel = createWeatherInfoPanel();
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(weatherInfoPanel, gbc);

        // Creating footer panel for forecast
        JPanel footerPanel = createFooterPanel();
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(footerPanel, gbc);

        displayWeatherForDefaultLocation();
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchField = new JTextField(30);
        searchField.setFont(new Font("Dialog", Font.PLAIN, 16));

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchWeatherData());

        cityDropdown = new JComboBox<>();
        loadCitiesFromFile();
        cityDropdown.addActionListener(e -> {
            String selectedCity = (String) cityDropdown.getSelectedItem();
            if (selectedCity != null && !selectedCity.isEmpty()) {
                displayWeatherForLocation(selectedCity);
            }
        });

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(cityDropdown);

        return searchPanel;
    }

    private JPanel createWeatherInfoPanel() {
        JPanel weatherInfoPanel = new JPanel();
        weatherInfoPanel.setLayout(new BoxLayout(weatherInfoPanel, BoxLayout.Y_AXIS));

        locationLabel = new JLabel("Location: Unknown");
        locationLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        locationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        temperatureLabel = new JLabel("Temperature: 10 *C");
        temperatureLabel.setFont(new Font("Dialog", Font.BOLD, 24));
        temperatureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        weatherLabel = new JLabel("Weather: Sunny");
        weatherLabel.setFont(new Font("Dialog", Font.PLAIN, 20));
        weatherLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        weatherIconLabel = new JLabel();
        weatherIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        weatherInfoPanel.add(locationLabel);
        weatherInfoPanel.add(temperatureLabel);
        weatherInfoPanel.add(weatherLabel);
        weatherInfoPanel.add(weatherIconLabel);

        return weatherInfoPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BorderLayout());

        forecastArea = new JTextArea(5, 60);
        forecastArea.setEditable(false);
        forecastArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(forecastArea);

        footerPanel.add(new JLabel("7-Day Weather Forecast:"), BorderLayout.NORTH);
        footerPanel.add(scrollPane, BorderLayout.CENTER);

        return footerPanel;
    }

    private void displayWeatherForDefaultLocation() {
//        String defaultLocation = "Karachi"; old code where location was karachi by default
//        displayWeatherForLocation(defaultLocation);
        try {
            // Fetch location from a geolocation API
            String urlString = "https://ip-api.com/json/"; // Free geolocation API
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == 200) {
                // get response from API
                StringBuilder response = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();
                connection.disconnect();

                // get the city name ?
                JSONObject jsonResponse = (JSONObject) new org.json.simple.parser.JSONParser().parse(response.toString());
                String city = (String) jsonResponse.get("city");

                if (city != null && !city.isEmpty()) {
                    displayWeatherForLocation(city); // Display weather for fetched location
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback to Karachi if geolocation fails
        String defaultLocation = "Karachi";
        displayWeatherForLocation(defaultLocation);
    }

    private void searchWeatherData() {
        String location = searchField.getText().trim();
        if (!location.isEmpty()) {
            displayWeatherForLocation(location);
            saveCityToFile(location);
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a location.", "Input Required", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void displayWeatherForLocation(String location) {
        try {
            JSONArray locationData = WeatherApp.LocationData(location);

            if (locationData != null && !locationData.isEmpty()) {
                JSONObject locationInfo = (JSONObject) locationData.get(0);
                double latitude = (Double) locationInfo.get("latitude");
                double longitude = (Double) locationInfo.get("longitude");
                fetchAndDisplayWeather(latitude, longitude, location);
            } else {
                JOptionPane.showMessageDialog(this, "Location not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching weather data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fetchAndDisplayWeather(double latitude, double longitude, String location) {
        try {
            JSONObject weatherData = WeatherApp.getWeatherData(latitude, longitude);
            if (weatherData != null) {
                JSONObject hourlyData = (JSONObject) weatherData.get("hourly");
                JSONArray temperatures = (JSONArray) hourlyData.get("temperature_2m");
                JSONArray conditions = (JSONArray) hourlyData.get("weathercode");

                double temperature = (double) temperatures.get(0);
                String weatherCode = String.valueOf(conditions.get(0));
                String weather = getWeatherFromCode(weatherCode);

                temperatureLabel.setText("Temperature: " + temperature + " °C");
                weatherLabel.setText("Weather: " + weather);
                locationLabel.setText("Location: " + location);

                updateWeatherIcon(weather);
                update7DayForecast(hourlyData);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to retrieve weather data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error displaying weather data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void update7DayForecast(JSONObject hourlyData) {
        JSONArray temperatures = (JSONArray) hourlyData.get("temperature_2m");
        JSONArray conditions = (JSONArray) hourlyData.get("weathercode");
        StringBuilder forecastBuilder = new StringBuilder();

        for (int i = 0; i < Math.min(7, conditions.size()); i++) {
            String weatherCode = String.valueOf(conditions.get(i));
            String weather = getWeatherFromCode(weatherCode);
            double temperature = (double) temperatures.get(i);
            forecastBuilder.append("Day ").append(i + 1).append(": ")
                    .append(weather).append(" (").append(temperature).append(" °C)").append("\n");
        }

        forecastArea.setText(forecastBuilder.toString());
    }

    private String getWeatherFromCode(String code) {
        return switch (code) {
            case "0" -> "Clear";
            case "1" -> "Partly cloudy";
            case "2" -> "Cloudy";
            case "3" -> "Rain";
            case "4" -> "Snow";
            default -> "Unknown";
        };
    }

    private void updateWeatherIcon(String weather) {
        String iconPath = switch (weather.toLowerCase()) {
            case "clear" -> "src/assets/clear.png";
            case "rain" -> "src/assets/rain.png";
            case "snow" -> "src/assets/snow.png";
            case "cloudy" -> "src/assets/cloudy.png";
            default -> "src/assets/cloudy.png"; // not sure what the weather is but it probably has clouds so deafult response
        };

        ImageIcon icon = new ImageIcon(iconPath);
        weatherIconLabel.setIcon(icon);
    }

    private void saveCityToFile(String city) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(city);
            writer.newLine();
            if (cityDropdown.getItemCount() == 0 || !cityExistsInDropdown(city)) {
                cityDropdown.addItem(city);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCitiesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!cityExistsInDropdown(line)) {
                    cityDropdown.addItem(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean cityExistsInDropdown(String city) {
        for (int i = 0; i < cityDropdown.getItemCount(); i++) {
            if (cityDropdown.getItemAt(i).equalsIgnoreCase(city)) {
                return true;
            }
        }
        return false;
    }
}// add temp to scoll panel accuracy of API
