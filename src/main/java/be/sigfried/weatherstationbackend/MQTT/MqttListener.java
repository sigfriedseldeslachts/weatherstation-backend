package be.sigfried.weatherstationbackend.MQTT;

import be.sigfried.weatherstationbackend.Models.SensorCache;
import be.sigfried.weatherstationbackend.Models.SensorType;
import be.sigfried.weatherstationbackend.Models.SensorValue;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.Map;

public class MqttListener implements IMqttMessageListener {

    private final SimpMessagingTemplate template;
    private final SensorCache cache;

    public MqttListener(SimpMessagingTemplate template, SensorCache cache) {
        this.template = template;
        this.cache = cache;
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        // Get the sensor name (only the last part of the topic)
        String sensorName = s.substring(s.lastIndexOf("/") + 1);
        SensorType sensor = SensorType.fromName(sensorName);

        // Only allow sensors that exist
        if (sensor == null) {
            return;
        }

        // Create SensorValue object
        SensorValue sensorValue = new SensorValue(
                Float.parseFloat(mqttMessage.toString()),
                sensor
        );

        // Send to websocket clients
        this.template.convertAndSend("/measurements/update", Collections.singletonMap(sensor, sensorValue));

        cache.put(sensorValue);
    }
}
