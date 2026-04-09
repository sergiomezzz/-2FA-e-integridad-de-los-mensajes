# Proyecto Seguridad 2FA + Integridad de Mensajes

## Requisitos
- Java 17
- Maven

## Pasos para ejecutar

1. **Abrir dos terminales**

2. **Terminal 1 - Sistema Secundario** (puerto 8081)
```bash
cd sistema-secundario
mvn clean spring-boot:run
```

3. **Terminal 2 - Sistema Principal** (puerto 8080)
```bash
cd sistema-principal
mvn clean spring-boot:run
```

4. **Abrir index.html** en el navegador (Chrome, Edge, Firefox)

## Cómo probar los escenarios obligatorios

### Escenario 1: Autenticación exitosa
1. Registrar usuario
2. Escanear QR con Google Authenticator
3. Login con usuario/contraseña
4. Ingresar código de 6 dígitos de Google Authenticator
5. ✅ Acceso concedido

### Escenario 2: 2FA incorrecto
1. Login correcto
2. Ingresar código 2FA inválido
3. ❌ Acceso denegado

### Escenario 3: Mensaje íntegro
1. Autenticarse
2. Escribir mensaje, NO marcar "alterar"
3. Enviar
4. ✅ Sistema secundario acepta el mensaje

### Escenario 4: Mensaje alterado
1. Autenticarse
2. Escribir mensaje, MARCAR "alterar"
3. Enviar
4. ❌ Sistema secundario rechaza el mensaje


### 🔐 Validación de Contraseñas

Se implementó validación robusta de contraseñas con los siguientes requisitos:

**Requisitos Mínimos de Contraseña:**
- ✅ Mínimo 8 caracteres**
- ✅ Al menos una letra MAYÚSCULA (A-Z)
- ✅ Al menos una letra minúscula (a-z)
- Método `validatePassword()` que valida contra los requisitos

**Frontend (index.html):**
- Sección visual con **lista de requisitos** en forma de checklist
- **Validación en tiempo real** mientras se escribe la contraseña
- Indicadores visuales **verde ✓** cuando se cumplen requisitos
- Indicadores visuales **gris** cuando no se cumplen
- Botón "Registrar Usuario" **deshabilitado hasta cumplir todos los requisitos**
- Cambio visual y de cursor para feedback al usuario

**Ejemplos de Contraseñas:**
- ❌ `pass123` - Solo 7 caracteres
- ❌ `password` - Sin mayúscula
- ❌ `PASSWORD` - Sin minúscula
- ✅ `Pass1234` - Cumple todos los requisitos
- ✅ `Seguridad2024` - Cumple todos los requisitos

## Evidencias
- En consola del sistema secundario se ven los logs con HMACs
- En la interfaz web se ve el historial de mensajes aceptados/rechazados
- El formulario de registro muestra requisitos y valida en tiempo real

