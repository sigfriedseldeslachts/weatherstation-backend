package be.sigfried.weatherstationbackend.Models;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;

public enum SensorType {
    TEMPERATURE("temperature", "weer_station_temperatuur"),
    HUMIDITY("humidity", "weer_station_luchtvochtigheid"),
    PRESSURE("pressure", "weer_station_luchtdruk"),
    ILLUMINANCE("illuminance", "weer_station_verlichtingssterkte"),
    PM25("pm25", "weer_station_pm25"),
    PM10("pm10", "weer_station_pm10");

    private final String name;
    private final String entityId;

    SensorType(String name, String entityId) {
        this.name = name;
        this.entityId = entityId;
    }

    public String getName() {
        return name;
    }

    public String getEntityId() {
        return entityId;
    }

    public static SensorType fromName(String name) {
        for (SensorType sensorType : SensorType.values()) {
            if (sensorType.getName().equals(name)) {
                return sensorType;
            }
        }

        return null;
    }

    public static SensorType fromEntityId(String entityId) {
        for (SensorType sensorType : SensorType.values()) {
            if (sensorType.getEntityId().equals(entityId)) {
                return sensorType;
            }
        }

        return null;
    }

    public static Map<String, SensorType> getSensorsMapFromEntityId() {
        Map<String, SensorType> sensorsMap = new java.util.HashMap<>();
        for (SensorType sensorType : SensorType.values()) {
            sensorsMap.put(sensorType.getEntityId(), sensorType);
        }
        return sensorsMap;
    }

    @JsonValue
    public String getJsonValue() {
        return name;
    }
}
