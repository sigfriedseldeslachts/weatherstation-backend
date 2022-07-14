package be.sigfried.weatherstationbackend.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseHandler {

    public static ResponseEntity<Object> generateResponse(String message, HttpStatus status, Object responseObj, Map<String, Object> additionalData) {
        Map<String, Object> map = new HashMap<>();

        // Merge data in additionalData with map
        if (additionalData != null) {
            map.putAll(additionalData);
        }

        // if message is not null or not empty, add it to the map
        if (message != null && !message.isEmpty()) {
            map.put("message", message);
        }

        map.put("data", responseObj);

        return new ResponseEntity<>(map, status);
    }

    public static ResponseEntity<Object> generateResponse(Object responseObj) {
        return generateResponse(null, HttpStatus.OK, responseObj, null);
    }

    public static ResponseEntity<Object> generateResponse(Object responseObj, HttpStatus status) {
        return generateResponse(null, status, responseObj, null);
    }

    public static ResponseEntity<Object> generateResponse(Object responseObj, Map<String, Object> additionalData) {
        return generateResponse(null, HttpStatus.OK, responseObj, additionalData);
    }
}

