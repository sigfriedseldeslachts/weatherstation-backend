package be.sigfried.weatherstationbackend.Controllers;

import be.sigfried.weatherstationbackend.Util.ResponseHandler;
import com.influxdb.client.InfluxDBClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final IMqttClient mqttClient;

    private final StatefulRedisConnection<String, String> redisConnection;

    private final InfluxDBClient influxDBClient;

    public HealthController(IMqttClient mqttClient, StatefulRedisConnection<String, String> connection, InfluxDBClient influxDBClient) {
        this.mqttClient = mqttClient;
        this.redisConnection = connection;
        this.influxDBClient = influxDBClient;
    }

    @GetMapping("/health")
    public ResponseEntity<Object> health() {
        Map<String, Boolean> data = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        boolean mqttConnected = mqttClient.isConnected();
        boolean redisConnected = redisConnection.isOpen();
        boolean influxConnected = influxDBClient.ping();

        data.put("mqtt", mqttConnected);
        data.put("redis", redisConnected);
        data.put("influx", influxConnected);

        // If any of the variables are false, return an serivce unavailable response
        if (!mqttConnected || !redisConnected) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }

        return ResponseHandler.generateResponse(data, status);
    }

}
