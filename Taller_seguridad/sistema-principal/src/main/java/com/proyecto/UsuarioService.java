package com.proyecto;

import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final GoogleAuthenticator googleAuth = new GoogleAuthenticator();
    
    // Requisitos de contraseña
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z]).{8,}$";
    
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    
    /**
     * Valida que la contraseña cumpla los requisitos mínimos:
     * - Mínimo 8 caracteres
     * - Al menos una letra mayúscula
     * - Al menos una letra minúscula
     */
    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("La contraseña no puede estar vacía");
        }
        
        if (password.length() < PASSWORD_MIN_LENGTH) {
            throw new RuntimeException("La contraseña debe tener mínimo 8 caracteres");
        }
        
        if (!password.matches(PASSWORD_REGEX)) {
            throw new RuntimeException("La contraseña debe contener al menos una letra mayúscula y una minúscula");
        }
    }
    
    public Usuario register(String username, String password) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new RuntimeException("Usuario ya existe");
        }
        
        // Validar contraseña antes de registrar
        validatePassword(password);
        
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(password); // Texto plano para simplicidad
        GoogleAuthenticatorKey key = googleAuth.createCredentials();
        usuario.setSecret2fa(key.getKey());
        return usuarioRepository.save(usuario);
    }
    
    public boolean validateCredentials(String username, String password) {
        return usuarioRepository.findByUsername(username)
            .map(u -> u.getPassword().equals(password))
            .orElse(false);
    }
    
    public Usuario getByUsername(String username) {
        return usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    
    public Usuario getById(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    
    public boolean verify2FA(String username, int code) {
        Usuario usuario = getByUsername(username);
        return googleAuth.authorize(usuario.getSecret2fa(), code);
    }
    
    public String getQRData(Long userId) {
        Usuario usuario = getById(userId);
        return String.format("otpauth://totp/SeguridadApp:%s?secret=%s&issuer=SeguridadApp",
            usuario.getUsername(), usuario.getSecret2fa());
    }
}
