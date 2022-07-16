package be.sigfried.weatherstationbackend.Controllers;

import be.sigfried.weatherstationbackend.Models.HistoryEnum;
import be.sigfried.weatherstationbackend.Repositories.MeasurementRepository;
import be.sigfried.weatherstationbackend.Util.ResponseHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class HistoryController {

    private final MeasurementRepository repository;

    public HistoryController(MeasurementRepository repository) {
        this.repository = repository;
    }

    @GetMapping("all")
    public ResponseEntity<Object> getAllHistoryViews() throws Exception {
        HashMap<String, Object> result = new HashMap<>();

        // Completable fure for each of the history views
        CompletableFuture<Object> hourView = CompletableFuture.supplyAsync(repository::getHourMeasurements);
        CompletableFuture<Object> last24View = CompletableFuture.supplyAsync(() -> repository.getDayMeasurements(true, null, null));
        CompletableFuture<Object> dayView = CompletableFuture.supplyAsync(() -> repository.getDayMeasurements(false, null, null));
        CompletableFuture<Object> weekView = CompletableFuture.supplyAsync(() -> repository.getWeekMeasurements(false, null, null));
        CompletableFuture<Object> monthView = CompletableFuture.supplyAsync(() -> repository.getMonthMeasurements(false, null, null));

        // Wait for all the futures to complete
        CompletableFuture.allOf(hourView, last24View, dayView, weekView, monthView).join();

        // Add the results to the result map
        result.put("hour", hourView.get());
        result.put("last24", last24View.get());
        result.put("day", dayView.get());
        result.put("week", weekView.get());
        result.put("month", monthView.get());

        return ResponseHandler.generateResponse(result);
    }

    @GetMapping("{type}")
    public ResponseEntity<Object> getHistoryView(@PathVariable("type") HistoryEnum type,
                                                 @RequestParam(value = "start", required = false) String start,
                                                 @RequestParam(value = "end", required = false) String end,
                                                 @RequestParam(value = "since_last", required = false) boolean sinceLast) throws Exception {

        Instant startTime = parseInstant(start);
        Instant endTime = parseInstant(end);

        Object response = switch (type) {
            case hour -> repository.getHourMeasurements();
            case day -> repository.getDayMeasurements(sinceLast, startTime, endTime);
            case week -> repository.getWeekMeasurements(sinceLast, startTime, endTime);
            case month -> repository.getMonthMeasurements(sinceLast, startTime, endTime);
            default -> throw new Exception("Invalid type");
        };

        return ResponseHandler.generateResponse(response);
    }

    private Instant parseInstant(String instant) {
        try {
            return Instant.parse(instant);
        } catch (Exception e) {
            return null;
        }
    }
}
