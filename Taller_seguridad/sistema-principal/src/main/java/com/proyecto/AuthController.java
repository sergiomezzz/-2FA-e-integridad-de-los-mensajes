package com.proyecto;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {
    private final UsuarioService usuarioService;
    private final Map<String, String> tokens = new HashMap<>(); // token -> username
    private String pending2FAUser = null;
    
    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            Usuario user = usuarioService.register(body.get("username"), body.get("password"));
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("message", "Usuario registrado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping(value = "/qrcode/{userId}", produces = MediaType.IMAGE_PNG_VALUE)
    public void getQRCode(@PathVariable Long userId, HttpServletResponse response) throws Exception {
        String qrData = usuarioService.getQRData(userId);
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 300, 300);
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        OutputStream out = response.getOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        out.close();
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (usuarioService.validateCredentials(username, password)) {
            pending2FAUser = username;
            return ResponseEntity.ok(Map.of("status", "2FA_REQUIRED", "username", username));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas"));
    }
    
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FA(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        int code = Integer.parseInt(body.get("code"));
        if (pending2FAUser != null && pending2FAUser.equals(username) && usuarioService.verify2FA(username, code)) {
            String token = UUID.randomUUID().toString();
            tokens.put(token, username);
            pending2FAUser = null;
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Código 2FA inválido"));
    }
}
