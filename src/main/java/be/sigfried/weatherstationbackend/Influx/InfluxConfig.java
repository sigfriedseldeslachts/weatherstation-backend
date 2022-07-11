package be.sigfried.weatherstationbackend.Influx;

import be.sigfried.weatherstationbackend.Models.SensorCache;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxConfig {

    @Value("${INFLUX_HOST}")
    public String INFLUX_HOST;

    @Value("${INFLUX_TOKEN}")
    public String INFLUX_TOKEN;

    @Value("${INFLUX_ORG}")
    public String INFLUX_ORG;

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(INFLUX_HOST, INFLUX_TOKEN.toCharArray(), INFLUX_ORG);
    }

    @Bean
    public SensorCache sensorCache() {
        return new SensorCache();
    }

}
