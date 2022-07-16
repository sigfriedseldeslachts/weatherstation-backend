package be.sigfried.weatherstationbackend.Repositories;

import be.sigfried.weatherstationbackend.Influx.DataQuery;
import be.sigfried.weatherstationbackend.Models.HistoryEnum;
import be.sigfried.weatherstationbackend.Util.Cache;
import com.fasterxml.jackson.core.type.TypeReference;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Repository
public class MeasurementRepository {

    @Value("${INFLUX_BUCKET}")
    public String INFLUX_BUCKET;

    private final DataQuery dataQuery;

    private final Cache cache;

    public MeasurementRepository(DataQuery dataQuery, Cache cache) {
        this.dataQuery = dataQuery;
        this.cache = cache;
    }

    /**
     * Get the measurements of the past hour.
     * @return
     */
    public Map<String, TreeMap<Instant, Object>> getHourMeasurements() {
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

    /**
     * Get the data for the day view.
     * @param sinceLast if true it will use the last 24h otherwise the current day.
     * @param startTime the start time of the query.
     * @param endTime the end time of the query.
     * @return the flux query for the month view.
     * @throws Exception
     */
    public Map<String, TreeMap<Instant, Object>> getDayMeasurements(boolean sinceLast, Instant startTime, Instant endTime) {
        return cache.remember("day-" + sinceLast, () -> {
            long startRange = -86400; // seconds in a day = 86400
            long endRange = 0;

            if (!sinceLast) {
                startRange = Duration.between(Instant.now(), Instant.now().truncatedTo(ChronoUnit.DAYS)).getSeconds();
                endRange = 86400 - startRange;
            }

            Flux flux = Flux.from(INFLUX_BUCKET)
                    .range(startRange, endRange, ChronoUnit.SECONDS)
                    .filter(Restrictions.or(DataQuery.getRestrictionsForAllSensors())) // For all sensors
                    .filter(Restrictions.field().equal("value"))
                    .aggregateWindow()
                    .withEvery("60m")
                    .withAggregateFunction("mean")
                    .limit(24);

            return dataQuery.getData(flux);
        }, HistoryEnum.day.getCacheLength(), new TypeReference<HashMap<String, TreeMap<Instant, Object>>>() {});
    }

    /**
     * Get the data for the week view.
     * @param sinceLast if true it will use the last 7 days otherwise the current week.
     * @param startTime the start time of the query.
     * @param endTime the end time of the query.
     * @return the flux query for the month view.
     * @throws Exception
     */
    public Map<String, TreeMap<Instant, Object>> getWeekMeasurements(boolean sinceLast, Instant startTime, Instant endTime) {
        return cache.remember("week-" + sinceLast, () -> {
            long startRange = -604800; // 7 days = 604800 seconds
            long endRange = 86400L - Duration.between(Instant.now().truncatedTo(ChronoUnit.DAYS), Instant.now()).getSeconds() - 1; // End of the day

            if (!sinceLast) {
                LocalDateTime startOfWeekDateTime = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                startRange = Duration.between(LocalDateTime.now(), startOfWeekDateTime).getSeconds();
            }

            Flux flux = Flux.from(INFLUX_BUCKET)
                    .range(startRange, endRange, ChronoUnit.SECONDS)
                    .filter(Restrictions.or(DataQuery.getRestrictionsForAllSensors())) // For all sensors
                    .filter(Restrictions.field().equal("value"))
                    .aggregateWindow()
                    .withEvery("1d")
                    .withAggregateFunction("mean")
                    .limit(7);

            return dataQuery.getData(flux);
        }, HistoryEnum.week.getCacheLength(), new TypeReference<HashMap<String, TreeMap<Instant, Object>>>() {});
    }

    /**
     * Get the data for the month view.
     * @param sinceLast if true it will use the last 30 days otherwise the current month.
     * @param startTime the start time of the query.
     * @param endTime the end time of the query.
     * @return the flux query for the month view.
     * @throws Exception
     */
    public Map<String, TreeMap<Instant, Object>> getMonthMeasurements(boolean sinceLast, Instant startTime, Instant endTime) {
        return cache.remember("month-" + sinceLast, () -> {
            long startRange = -86400 * 30;
            long endRange = 0;

            if (!sinceLast) {
                startRange = Duration.between(LocalDateTime.now(), LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth())).toSeconds();
                endRange = Duration.between(LocalDateTime.now(), LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth())).toSeconds();
            }

            Flux flux = Flux.from(INFLUX_BUCKET)
                    .range(startRange, endRange, ChronoUnit.SECONDS)
                    .filter(Restrictions.or(DataQuery.getRestrictionsForAllSensors())) // For all sensors
                    .filter(Restrictions.field().equal("value"))
                    .aggregateWindow()
                    .withEvery("1d")
                    .withAggregateFunction("mean")
                    .limit(30);

            return dataQuery.getData(flux);
        }, HistoryEnum.month.getCacheLength(), new TypeReference<HashMap<String, TreeMap<Instant, Object>>>() {});
    }
}
