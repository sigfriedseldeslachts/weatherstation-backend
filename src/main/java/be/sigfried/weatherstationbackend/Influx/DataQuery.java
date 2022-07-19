package be.sigfried.weatherstationbackend.Influx;

import be.sigfried.weatherstationbackend.Models.SensorType;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class DataQuery {

    @Autowired
    public InfluxDBClient influxClient;

    public Map<String, TreeMap<Instant, Object>> getData(Flux flux) {
        Map<String, TreeMap<Instant, Object>> result = new TreeMap<>();
        Map<String, SensorType> sensors = SensorType.getSensorsMapFromEntityId();

        List<FluxTable> tables = influxClient.getQueryApi().query(flux.toString());

        for (FluxTable table : tables) {
            List<FluxRecord> records = table.getRecords();

            for (FluxRecord record : records) {
                SensorType sensorType = sensors.get(record.getValueByKey("entity_id"));

                // Add the sensor to the result if it doesn't exist yet
                if (!result.containsKey(sensorType.getName())) {
                    result.put(sensorType.getName(), new TreeMap<>());
                }

                // Add the value to the result
                result.get(sensorType.getName()).put(record.getTime(), record.getValue());
            }
        }

        return result;
    }

    public static Restrictions[] getRestrictionsForSensors(List<SensorType> sensorTypes) {
        return sensorTypes.stream().map(sensorType -> Restrictions.column("entity_id").equal(sensorType.getEntityId())).toArray(Restrictions[]::new);
    }

    public static Restrictions[] getRestrictionsForAllSensors() {
        return getRestrictionsForSensors(List.of(SensorType.values()));
    }

    public static String getQueryMapFunctionForAllSensors() {
        // If the sensor is a pressure sensor, calculate it to hPa instead of Pa
        return "({r with _value: if r.entity_id == \"weer_station_luchtdruk\" then r._value / 100.0 else r._value })";
    }

}
