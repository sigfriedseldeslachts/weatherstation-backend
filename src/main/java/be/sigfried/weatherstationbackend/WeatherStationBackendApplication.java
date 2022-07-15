package be.sigfried.weatherstationbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WeatherStationBackendApplication {

	public static void main(String[] args) {
		// Set timezone to Europe/Brussels
		System.setProperty("user.timezone", "Europe/Brussels");

		SpringApplication.run(WeatherStationBackendApplication.class, args);
	}

}
