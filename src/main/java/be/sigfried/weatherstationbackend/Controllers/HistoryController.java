package be.sigfried.weatherstationbackend.Controllers;

import be.sigfried.weatherstationbackend.Influx.DataQuery;
import be.sigfried.weatherstationbackend.Models.HistoryEnum;
import be.sigfried.weatherstationbackend.Util.Cache;
import be.sigfried.weatherstationbackend.Util.ResponseHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
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

    @GetMapping("{type}")
    public ResponseEntity<Object> getHistoryView(@PathVariable("type") HistoryEnum type,
                                                 @RequestParam(value = "start", required = false) String start,
                                                 @RequestParam(value = "end", required = false) String end,
                                                 @RequestParam(value = "since_last", required = false) boolean sinceLast) throws Exception {

        Instant startTime = parseInstant(start);
        Instant endTime = parseInstant(end);

        Object response = cache.remember(type + "-" + sinceLast, () -> {
            Flux flux = Flux.from(INFLUX_BUCKET);

            flux = switch (type) {
                case day -> setFluxForDay(flux, startTime, endTime, sinceLast);
                case week -> setFluxForWeek(flux, startTime, endTime, sinceLast);
                case month -> setFluxForMonth(flux, startTime, endTime, sinceLast);
                default -> throw new Exception("Invalid type");
            };

            return dataQuery.getData(flux);
        }, getCachingLength(type), new TypeReference<HashMap<String, TreeMap<Instant, Object>>>() {});

        return ResponseHandler.generateResponse(response);
    }

    /**
     * Returns the Flux query for the day view.
     * @param flux
     * @param start
     * @param end
     * @param sinceLast
     * @return
     */
    private Flux setFluxForDay(Flux flux, Instant start, Instant end, boolean sinceLast) {
        long startRange = -86400; // seconds in a day = 86400
        long endRange = 0;

        if (!sinceLast) {
            startRange = Duration.between(Instant.now(), Instant.now().truncatedTo(ChronoUnit.DAYS)).getSeconds();
            endRange = 86400 - startRange;
        }

        return flux
                .range(startRange, endRange, ChronoUnit.SECONDS)
                .filter(Restrictions.or(DataQuery.getRestrictionsForAllSensors())) // For all sensors
                .filter(Restrictions.field().equal("value"))
                .aggregateWindow()
                .withEvery("60m")
                .withAggregateFunction("mean")
                .limit(24);
    }

    /**
     * Returns the Flux query for the week view.
     * @param flux
     * @param start
     * @param end
     * @param sinceLast
     * @return
     */
    private Flux setFluxForWeek(Flux flux, Instant start, Instant end, boolean sinceLast) {
        long startRange = -604800; // 7 days = 604800 seconds
        long endRange = 86400L - Duration.between(Instant.now().truncatedTo(ChronoUnit.DAYS), Instant.now()).getSeconds() - 1; // End of the day

        if (!sinceLast) {
            LocalDateTime startOfWeekDateTime = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            startRange = Duration.between(LocalDateTime.now(), startOfWeekDateTime).getSeconds();
        }

        return flux
                .range(startRange, endRange, ChronoUnit.SECONDS)
                .filter(Restrictions.or(DataQuery.getRestrictionsForAllSensors())) // For all sensors
                .filter(Restrictions.field().equal("value"))
                .aggregateWindow()
                .withEvery("1d")
                .withAggregateFunction("mean")
                .limit(7);
    }

    /**
     *
     * @param flux
     * @param startTime
     * @param endTime
     * @param sinceLast
     * @return
     */
    private Flux setFluxForMonth(Flux flux, Instant startTime, Instant endTime, boolean sinceLast) {
        long startRange = -86400 * 30;
        long endRange = 0;

        if (!sinceLast) {
            startRange = Duration.between(LocalDateTime.now(), LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth())).toSeconds();
            endRange = Duration.between(LocalDateTime.now(), LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth())).toSeconds();
        }

        return flux
                .range(startRange, endRange, ChronoUnit.SECONDS)
                .filter(Restrictions.or(DataQuery.getRestrictionsForAllSensors())) // For all sensors
                .filter(Restrictions.field().equal("value"))
                .aggregateWindow()
                .withEvery("1d")
                .withAggregateFunction("mean")
                .limit(30);
    }

    /**
     * Returns the duration of the cache for the given type.
     * @param type The type of the history view. (Ex: day, week, month)
     * @return Duration
     */
    private Duration getCachingLength(HistoryEnum type) {
        return switch (type) {
            case day -> Duration.ofMinutes(30);
            case week, month -> Duration.ofHours(12);
            default -> Duration.ofHours(6);
        };
    }

    private Instant parseInstant(String instant) {
        try {
            return Instant.parse(instant);
        } catch (Exception e) {
            return null;
        }
    }
}
