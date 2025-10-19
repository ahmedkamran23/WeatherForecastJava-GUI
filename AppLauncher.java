import javax.swing.*;

public class AppLauncher {
    public static void main(String[] args) {

        //System.out.println("Hello World!");

        // Ensure that the GUI runs on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Create an instance of WeatherAppGUI and display it
            WeatherAppGUI app = new WeatherAppGUI();
            app.setVisible(true);

            System.out.println(WeatherApp.LocationData("Karachi"));
        });
    }
}
