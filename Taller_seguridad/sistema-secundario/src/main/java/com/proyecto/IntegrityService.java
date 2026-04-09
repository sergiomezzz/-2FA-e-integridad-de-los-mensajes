package com.proyecto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class IntegrityService {
    @Value("${hmac.secret}")
    private String secret;
    
    public boolean verificarIntegridad(String payload, String hmacRecibido) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder hmacCalculado = new StringBuilder();
        for (byte b : hmacBytes) {
            hmacCalculado.append(String.format("%02x", b));
        }
        
        System.out.println("Payload recibido: " + payload);
        System.out.println("HMAC recibido: " + hmacRecibido);
        System.out.println("HMAC calculado: " + hmacCalculado.toString());
        
        boolean valido = hmacCalculado.toString().equals(hmacRecibido);
        System.out.println("Verificación: " + (valido ? "ÉXITO - Mensaje íntegro" : "FALLO - Mensaje alterado"));
        
        return valido;
    }
}
