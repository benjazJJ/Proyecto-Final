package Servicio.MicroservicioRolesYPermisos.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Servicio.MicroservicioRolesYPermisos.Model.CredencialesRequest;
import Servicio.MicroservicioRolesYPermisos.Model.Usuario;
import Servicio.MicroservicioRolesYPermisos.Service.UsuarioService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/autenticacion")
public class AutenticacionController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Registrar un nuevo usuario con rol ESTUDIANTE por defecto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "409", description = "El correo o RUT ya existe o el correo no es institucional", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            Usuario nuevo = usuarioService.registrar(usuario);
            return ResponseEntity.ok(nuevo);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @Operation(summary = "Autenticar usuario con correo y contraseña (login)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Usuario usuario) {
        boolean autenticado = usuarioService.autenticar(usuario.getCorreo(), usuario.getContrasena());
        return autenticado
                ? ResponseEntity.ok("Autenticación exitosa")
                : ResponseEntity.status(401).body("Credenciales inválidas");
    }

    @Operation(summary = "Validar credenciales de un usuario (usado por otros microservicios)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario autenticado y datos retornados", content = @Content(schema = @Schema(implementation = ValidacionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content)
    })
    @GetMapping("/validar")
    public ResponseEntity<?> validar(
            @RequestParam String correo,
            @RequestParam String contrasena) {

        Optional<Usuario> usuarioOpt = usuarioService.autenticarYObtener(correo, contrasena);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        Usuario usuario = usuarioOpt.get();
        return ResponseEntity.ok(new ValidacionResponse(
                true,
                usuario.getId(),
                usuario.getCorreo(),
                usuario.getRol().getNombreRol()));
    }

    @Operation(summary = "Editar datos personales del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos actualizados correctamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado para modificar otra cuenta", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto con correo existente o inválido", content = @Content)
    })
    @PutMapping("/editarcuenta/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable int id, @RequestBody Usuario cambios) {
        try {
            Usuario actualizado = usuarioService.actualizarUsuario(id, cambios);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @Operation(summary = "Eliminar cuenta de usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content),
            @ApiResponse(responseCode = "403", description = "No puedes eliminar esta cuenta", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada", content = @Content)
    })
    @DeleteMapping("/usuario/{id}")
    public ResponseEntity<?> eliminarUsuario(
            @PathVariable int id,
            @RequestBody CredencialesRequest credenciales) {

        try {
            Optional<Usuario> solicitanteOpt = usuarioService.autenticarYObtener(
                    credenciales.getCorreo(), credenciales.getContrasena());

            if (solicitanteOpt.isEmpty()) {
                return ResponseEntity.status(401).body("No autorizado");
            }

            Usuario solicitante = solicitanteOpt.get();
            Usuario aEliminar = usuarioService.obtenerPorId(id);
            if (aEliminar == null) {
                return ResponseEntity.status(404).body("Cuenta no encontrada");
            }

            boolean esAdmin = solicitante.getRol().getNombreRol().equalsIgnoreCase("ADMINISTRADOR");
            if (!esAdmin) {
                return ResponseEntity.status(403).body("Solo los administradores pueden eliminar cuentas");
            }

            usuarioService.eliminar(id);
            return ResponseEntity.ok("Cuenta eliminada correctamente");

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // 💡 útil si lo estás viendo por consola
            return ResponseEntity.status(500).body("Error interno al eliminar: " + e.getMessage());
        }
    }

    @Operation(summary = "Obtener usuario por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @GetMapping("/usuario/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable int id) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        return usuario != null
                ? ResponseEntity.ok(usuario)
                : ResponseEntity.status(404).body("Usuario no encontrado");
    }

    @Operation(summary = "Obtener usuario por RUT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado por RUT"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado por RUT", content = @Content)
    })
    @GetMapping("/usuario/rut/{rut}")
    public ResponseEntity<?> obtenerUsuarioPorRut(@PathVariable String rut) {
        Usuario usuario = usuarioService.obtenerPorRut(rut);
        return usuario != null
                ? ResponseEntity.ok(usuario)
                : ResponseEntity.status(404).body("Usuario no encontrado por RUT");
    }

    // DTO para validación de credenciales desde otros microservicios
    @Schema(description = "Respuesta de validación de usuario con información básica")
    static class ValidacionResponse {

        @Schema(description = "Indica si el usuario fue autenticado correctamente", example = "true")
        public boolean autenticado;

        @Schema(description = "ID del usuario autenticado", example = "5")
        public int idUsuario;

        @Schema(description = "Correo electrónico del usuario", example = "usuario@correo.cl")
        public String correo;

        @Schema(description = "Rol del usuario", example = "ESTUDIANTE")
        public String rol;

        public ValidacionResponse(boolean autenticado, int idUsuario, String correo, String rol) {
            this.autenticado = autenticado;
            this.idUsuario = idUsuario;
            this.correo = correo;
            this.rol = rol;
        }
    }
}
