package com.circleguard.promotion.controller;

import com.circleguard.promotion.service.LocationResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/location")
@RequiredArgsConstructor
public class LocationSignalController {
    private final LocationResolutionService locationResolutionService;

    @PostMapping("/signal")
    public ResponseEntity<Void> receiveSignal(@RequestBody Map<String, Object> request) {
        // Validar que request no sea null
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Object apMacObj = request.get("apMac");
        Object deviceMacObj = request.get("deviceMac");
        Object rssiObj = request.get("rssi");
        
        // Validar campos requeridos
        if (apMacObj == null || deviceMacObj == null || rssiObj == null) {
            return ResponseEntity.badRequest().build();
        }
        
        String apMac = apMacObj.toString();
        String deviceMac = deviceMacObj.toString();
        Double rssi;
        
        try {
            rssi = Double.valueOf(rssiObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        locationResolutionService.processSignal(apMac, deviceMac, rssi);
        return ResponseEntity.ok().build();
    }
}