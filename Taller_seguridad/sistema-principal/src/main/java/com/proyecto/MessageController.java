package com.proyecto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class MessageController {
    private final HMACService hmacService;
    private final Map<String, String> tokens = new HashMap<>(); // Simular tokens activos
    
    @Value("${secondary.url}")
    private String secondaryUrl;
    
    public MessageController(HMACService hmacService) {
        this.hmacService = hmacService;
    }
    
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestHeader("Authorization") String token, 
                                          @RequestBody Map<String, String> body) {
        // Validar token simple
        if (!token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token inválido"));
        }
        
        String message = body.get("message");
        String payload = "{\"message\":\"" + message + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
        
        try {
            String hmac = hmacService.calcularHMAC(payload);
            
            // Reintentos (3 intentos con espera exponencial)
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("payload", payload);
            requestBody.put("hmac", hmac);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            
            int intentos = 3;
            int esperaMs = 1000;
            ResponseEntity<String> response = null;
            
            for (int i = 0; i < intentos; i++) {
                try {
                    System.out.println("Intento " + (i+1) + "/3 enviando a sistema secundario");
                    response = restTemplate.postForEntity(secondaryUrl + "/receive", entity, String.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        System.out.println("Mensaje enviado exitosamente");
                        return ResponseEntity.ok(Map.of("status", "Mensaje aceptado", "hmac", hmac));
                    }
                } catch (Exception e) {
                    System.out.println("Error en intento " + (i+1) + ": " + e.getMessage());
                    if (i < intentos - 1) {
                        System.out.println("Reintentando en " + esperaMs + "ms...");
                        Thread.sleep(esperaMs);
                        esperaMs *= 2;
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Sistema secundario no disponible después de " + intentos + " intentos"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al enviar mensaje: " + e.getMessage()));
        }
    }
}
