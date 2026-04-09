package com.proyecto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ReceiveController {
    private final IntegrityService integrityService;
    
    public ReceiveController(IntegrityService integrityService) {
        this.integrityService = integrityService;
    }
    
    @PostMapping("/receive")
    public ResponseEntity<?> receiveMessage(@RequestBody Map<String, String> body) {
        try {
            String payload = body.get("payload");
            String hmac = body.get("hmac");
            
            System.out.println("=== MENSAJE RECIBIDO ===");
            
            boolean integro = integrityService.verificarIntegridad(payload, hmac);
            
            if (integro) {
                System.out.println("RESPUESTA: Mensaje ACEPTADO");
                return ResponseEntity.ok(Map.of("status", "ACCEPTED", "message", "Mensaje íntegro"));
            } else {
                System.out.println("RESPUESTA: Mensaje RECHAZADO");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "REJECTED", "message", "Mensaje alterado"));
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
