package be.sigfried.weatherstationbackend.Controllers;

import be.sigfried.weatherstationbackend.Models.SensorCache;
import be.sigfried.weatherstationbackend.Models.SensorType;
import be.sigfried.weatherstationbackend.Models.SensorValue;
import be.sigfried.weatherstationbackend.Repositories.MeasurementRepository;
import be.sigfried.weatherstationbackend.Util.ResponseHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/measurements")
@CrossOrigin(origins = "*")
public class LiveMeasurementsController {

    private final MeasurementRepository repository;
    private final SensorCache cache;

    public LiveMeasurementsController(MeasurementRepository measurementRepository, SensorCache cache) {
        this.repository = measurementRepository;
        this.cache = cache;
    }

    @GetMapping("actual")
    public ResponseEntity<Object> getActualMeasurements() {
        Map<SensorType, SensorValue> cacheMap = cache.getCache();

        // Get the latest timestamp from the cache
        Instant latestTimestamp = cacheMap.values().stream().map(SensorValue::getTimestamp).max(Comparator.naturalOrder()).orElse(null);

        return ResponseHandler.generateResponse(cacheMap, Collections.singletonMap("latestTimestamp", latestTimestamp));
    }

    @ResponseBody
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public String handleHttpMediaTypeNotAcceptableException() {
        return "No acceptable media type found";
    }

}
