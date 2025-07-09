package Servicio.MicroservicioMultas.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Servicio.MicroservicioMultas.Model.Multa;
import Servicio.MicroservicioMultas.Service.MultaService;
import Servicio.MicroservicioMultas.WebClient.ValidacionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v1/multas")
public class MultaController {

    @Autowired
    private MultaService multaService;
    
    @Operation(summary = "Listar todas las multas (solo ESTUDIANTE o DOCENTE)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Multas obtenidas con éxito", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acceso denegado por rol", content = @Content)
    })
    @PostMapping("/listar")
    public ResponseEntity<?> listarTodas(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String contrasena = body.get("contrasena");

        ValidacionResponse usuario = multaService.validarUsuario(correo, contrasena);
        if (!usuario.getRol().equalsIgnoreCase("ESTUDIANTE") &&
            !usuario.getRol().equalsIgnoreCase("DOCENTE")) {
            return ResponseEntity.status(403).body("Solo estudiantes o docentes pueden ver las multas.");
        }

        return ResponseEntity.ok(multaService.obtenerTodasLasMultas());
    }

    @Operation(summary = "Obtener multa por ID (solo ESTUDIANTE o DOCENTE)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Multa encontrada", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acceso denegado por rol", content = @Content),
        @ApiResponse(responseCode = "404", description = "Multa no encontrada", content = @Content)
    })
    @PostMapping("/ver/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String contrasena = body.get("contrasena");

        ValidacionResponse usuario = multaService.validarUsuario(correo, contrasena);
        if (!usuario.getRol().equalsIgnoreCase("ESTUDIANTE") &&
            !usuario.getRol().equalsIgnoreCase("DOCENTE")) {
            return ResponseEntity.status(403).body("Solo estudiantes o docentes pueden ver multas.");
        }

        return multaService.obtenerMultaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear nueva multa (solo ADMINISTRADOR o BIBLIOTECARIO)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Multa creada exitosamente", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acceso denegado por rol", content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> crearMulta(@RequestBody Map<String, Object> datos) {
        try {
            String correo = datos.get("correo").toString();
            String contrasena = datos.get("contrasena").toString();

            ValidacionResponse usuario = multaService.validarUsuario(correo, contrasena);
            if (!usuario.getRol().equalsIgnoreCase("ADMINISTRADOR") &&
                !usuario.getRol().equalsIgnoreCase("BIBLIOTECARIO")) {
                return ResponseEntity.status(403).body("Solo administradores o bibliotecarios pueden crear multas.");
            }

            Multa nuevaMulta = new Multa();
            nuevaMulta.setRunUsuario(datos.get("runUsuario").toString());
            nuevaMulta.setTipoSancion(datos.get("tipoSancion").toString());
            nuevaMulta.setSancion(datos.get("sancion").toString());
            nuevaMulta.setIdDevolucion(Integer.parseInt(datos.get("idDevolucion").toString()));

            Multa multaCreada = multaService.crearMulta(nuevaMulta, correo, contrasena);
            return ResponseEntity.status(201).body(multaCreada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @Operation(summary = "Actualizar multa existente (solo ADMINISTRADOR o BIBLIOTECARIO)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Multa actualizada exitosamente", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acceso denegado por rol", content = @Content),
        @ApiResponse(responseCode = "404", description = "Multa no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> datos) {
        try {
            String correo = datos.get("correo").toString();
            String contrasena = datos.get("contrasena").toString();

            ValidacionResponse usuario = multaService.validarUsuario(correo, contrasena);
            if (!usuario.getRol().equalsIgnoreCase("ADMINISTRADOR") &&
                !usuario.getRol().equalsIgnoreCase("BIBLIOTECARIO")) {
                return ResponseEntity.status(403).body("Solo administradores o bibliotecarios pueden actualizar multas.");
            }

            Multa multaActualizada = new Multa();
            multaActualizada.setRunUsuario(datos.get("runUsuario").toString());
            multaActualizada.setTipoSancion(datos.get("tipoSancion").toString());
            multaActualizada.setSancion(datos.get("sancion").toString());

            return multaService.actualizarMulta(id, multaActualizada)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(404).body("Multa no encontrada"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @Operation(summary = "Eliminar multa por ID (solo ADMINISTRADOR o BIBLIOTECARIO)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Multa eliminada exitosamente", content = @Content),
        @ApiResponse(responseCode = "403", description = "Acceso denegado por rol", content = @Content),
        @ApiResponse(responseCode = "404", description = "Multa no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String contrasena = body.get("contrasena");

        try {
            ValidacionResponse usuario = multaService.validarUsuario(correo, contrasena);
            if (!usuario.getRol().equalsIgnoreCase("ADMINISTRADOR") &&
                !usuario.getRol().equalsIgnoreCase("BIBLIOTECARIO")) {
                return ResponseEntity.status(403).body("Solo administradores o bibliotecarios pueden eliminar multas.");
            }

            boolean eliminado = multaService.eliminarMulta(id);
            if (eliminado) {
                return ResponseEntity.ok("Multa eliminada correctamente.");
            } else {
                return ResponseEntity.status(404).body("Multa no encontrada.");
            }

        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

}
