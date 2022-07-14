package be.sigfried.weatherstationbackend.Controllers;

import be.sigfried.weatherstationbackend.Influx.DataQuery;
import be.sigfried.weatherstationbackend.Util.Cache;
import be.sigfried.weatherstationbackend.Util.ResponseHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class HistoryController {

    @Value("${INFLUX_BUCKET}")
    public String INFLUX_BUCKET;

    private final DataQuery dataQuery;

    private final Cache cache;

    public HistoryController(DataQuery dataQuery, Cache cache) {
        this.dataQuery = dataQuery;
        this.cache = cache;
    }

    @GetMapping("day")
    public ResponseEntity<Object> getHistoryDay() throws Exception {
        Object response = cache.remember("historyDay", () -> {
            Flux flux = Flux.from(INFLUX_BUCKET)
                            .range(-1L, ChronoUnit.DAYS)
                            .filter(Restrictions.or(DataQuery.getRestrictionsForAllSensors())) // For all sensors
                            .filter(Restrictions.field().equal("value"))
                            .aggregateWindow()
                            .withEvery("60m")
                            .withAggregateFunction("mean")
                            .limit(24);
            return dataQuery.getData(flux);
        }, Duration.ofMinutes(30), Map.class);

        return ResponseHandler.generateResponse(response);
    }

    @GetMapping("week")
    public ResponseEntity<Object> getHistoryWeek() throws Exception {
        Object response = cache.remember("historyWeek", () -> {
            Flux flux = Flux.from(INFLUX_BUCKET)
                            .range(-7L, ChronoUnit.DAYS)
                            .filter(Restrictions.or(DataQuery.getRestrictionsForAllSensors())) // For all sensors
                            .filter(Restrictions.field().equal("value"))
                            .aggregateWindow()
                            .withEvery("24h")
                            .withAggregateFunction("mean")
                            .limit(7);
            return dataQuery.getData(flux);
        }, Duration.ofHours(2), new TypeReference<HashMap<String, TreeMap<Instant, Object>>>() {});

        return ResponseHandler.generateResponse(response);
    }

}
