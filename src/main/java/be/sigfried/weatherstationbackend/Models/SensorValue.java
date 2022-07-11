package be.sigfried.weatherstationbackend.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;

public class SensorValue {

    private Object value;
    private Instant timestamp;
    private SensorType sensorType;

    public SensorValue(Object value, Instant timestamp, SensorType sensorType) {
        this.value = value;
        this.timestamp = timestamp;
        this.sensorType = sensorType;
    }

    public SensorValue(Object value, SensorType sensorType) {
        this(value, Instant.now(), sensorType);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @JsonIgnore
    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }
}
