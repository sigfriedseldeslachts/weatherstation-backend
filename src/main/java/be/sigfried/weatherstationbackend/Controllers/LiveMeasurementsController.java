package be.sigfried.weatherstationbackend.Controllers;

import be.sigfried.weatherstationbackend.Influx.DataQuery;
import be.sigfried.weatherstationbackend.Models.SensorCache;
import be.sigfried.weatherstationbackend.Models.SensorType;
import be.sigfried.weatherstationbackend.Models.SensorValue;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/measurements")
@CrossOrigin(origins = "*")
public class LiveMeasurementsController {

    @Value("${INFLUX_BUCKET}")
    public String INFLUX_BUCKET;

    @Autowired
    public DataQuery dataQuery;

    @Autowired
    public SensorCache cache;

    @GetMapping("")
    public Map<String, TreeMap<Instant, Object>> getLiveMeasurements() {
        Flux flux = Flux.from(INFLUX_BUCKET)
                .range(-60L, ChronoUnit.MINUTES)
                .filter(Restrictions.or(DataQuery.getRestrictionsForAllSensors())) // For all sensors
                .filter(Restrictions.field().equal("value"))
                .aggregateWindow()
                    .withEvery("60s")
                    .withAggregateFunction("mean")
                .limit(120);

        return dataQuery.getData(flux);
    }

    @GetMapping("latest")
    public Map<SensorType, SensorValue> getActualMeasurements() {
        return cache.getCache();
    }

}
