package be.sigfried.weatherstationbackend.Models;

import java.util.HashMap;
import java.util.Map;

public class SensorCache {

    private final Map<SensorType, SensorValue> cache = new HashMap<>();

    public Map<SensorType, SensorValue> getCache() {
        return cache;
    }

    public SensorValue get(SensorType sensorType) {
        return cache.get(sensorType);
    }

    public void put(SensorValue sensorValue) {
        cache.put(sensorValue.getSensorType(), sensorValue);
    }

}
