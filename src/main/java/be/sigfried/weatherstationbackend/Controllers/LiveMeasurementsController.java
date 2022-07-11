package be.sigfried.weatherstationbackend.Controllers;

import be.sigfried.weatherstationbackend.Influx.DataQuery;
import be.sigfried.weatherstationbackend.Models.SensorCache;
import be.sigfried.weatherstationbackend.Models.SensorType;
import be.sigfried.weatherstationbackend.Models.SensorValue;
import be.sigfried.weatherstationbackend.Util.ResponseHandler;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Object> getLiveMeasurements() {
        Flux flux = Flux.from(INFLUX_BUCKET)
                .range(-60L, ChronoUnit.MINUTES)
                .filter(Restrictions.or(DataQuery.getRestrictionsForAllSensors())) // For all sensors
                .filter(Restrictions.field().equal("value"))
                .aggregateWindow()
                    .withEvery("60s")
                    .withAggregateFunction("mean")
                .limit(120);

        return ResponseHandler.generateResponse(dataQuery.getData(flux));
    }

    @GetMapping("latest")
    public ResponseEntity<Object> getActualMeasurements() {
        Map<SensorType, SensorValue> cacheMap = cache.getCache();

        // Get the latest timestamp from the cache
        Instant latestTimestamp = cacheMap.values().stream().map(SensorValue::getTimestamp).max(Comparator.naturalOrder()).orElse(null);

        return ResponseHandler.generateResponse(
                cacheMap, Collections.singletonMap("latestTimestamp", latestTimestamp)
        );
    }

    @ResponseBody
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public String handleHttpMediaTypeNotAcceptableException() {
        return "No acceptable media type found";
    }

}
