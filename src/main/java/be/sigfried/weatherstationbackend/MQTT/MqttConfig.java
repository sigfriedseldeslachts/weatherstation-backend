package be.sigfried.weatherstationbackend.MQTT;

import be.sigfried.weatherstationbackend.Models.SensorCache;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
public class MqttConfig {

    @Value("${MQTT_HOST}")
    public String MQTT_HOST;

    @Value("${MQTT_USERNAME}")
    public String MQTT_USERNAME;

    @Value("${MQTT_PASSWORD}")
    public String MQTT_PASSWORD;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private SensorCache cache;

    @Bean
    public IMqttClient mqttInputChannel() throws MqttException {
        LoggerFactory.getLogger(MqttConfig.class).info("Connecting to MQTT broker...");
        MqttClient client = new MqttClient(MQTT_HOST, "java-weather-station-backend");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(MQTT_USERNAME);
        options.setPassword(MQTT_PASSWORD.toCharArray());
        options.setAutomaticReconnect(true);

        client.connect(options);
        client.subscribe("weatherstation/#", (new MqttListener(template, cache)));

        return client;
    }

}
