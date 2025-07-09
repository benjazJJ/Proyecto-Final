package Servicio.MicroservicioStock.Controller;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Servicio.MicroservicioStock.Model.LibroStock;
import Servicio.MicroservicioStock.Repository.EstadoLibroRepository;
import Servicio.MicroservicioStock.Repository.LibroStockRepository;
import Servicio.MicroservicioStock.Service.LibroStockService;
import Servicio.MicroservicioStock.Service.ValidacionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v1/librostock")
public class LibroStockController {

    @Autowired
    private LibroStockRepository libroStockRepository;

    @Autowired
    private EstadoLibroRepository estadoLibroRepository;

    @Autowired
    private LibroStockService libroStockService;

    private boolean noTienePermiso(ValidacionResponse validacion) {
        return !validacion.isAutenticado() ||
                validacion.getRol().equalsIgnoreCase("ESTUDIANTE") ||
                validacion.getRol().equalsIgnoreCase("DOCENTE");
    }

    @Operation(summary = "Obtener todos los libros en stock", description = "Devuelve una lista de todos los libros disponibles en stock con enlaces HATEOAS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libros encontrados", content = @Content(schema = @Schema(implementation = LibroStock.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<LibroStock>>> obtenerLibroStock() {
        List<LibroStock> lista = libroStockRepository.findAll();

        List<EntityModel<LibroStock>> recursos = lista.stream()
                .map(libro -> EntityModel.of(libro,
                        WebMvcLinkBuilder.linkTo(
                                WebMvcLinkBuilder.methodOn(LibroStockController.class)
                                        .obtenerLibroPorId(libro.getIdLibroStock()))
                                .withSelfRel()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(recursos));
    }

    @Operation(summary = "Obtener un libro por ID", description = "Devuelve la información de un libro específico si existe, incluyendo enlaces HATEOAS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro encontrado", content = @Content(schema = @Schema(implementation = LibroStock.class))),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerLibroPorId(@PathVariable Long id) {
        Optional<LibroStock> libroOpt = libroStockRepository.findById(id);
        if (libroOpt.isPresent()) {
            LibroStock libro = libroOpt.get();
            EntityModel<LibroStock> recurso = EntityModel.of(libro);
            recurso.add(WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(LibroStockController.class)
                            .obtenerLibroPorId(id))
                    .withSelfRel());
            recurso.add(WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(LibroStockController.class)
                            .obtenerLibroStock())
                    .withRel("todos"));
            return ResponseEntity.ok(recurso);
        } else {
            return ResponseEntity.status(404).body("Libro no encontrado");
        }
    }

    @Operation(summary = "Crear libro en stock", description = "Crea un nuevo libro en el stock o actualiza la cantidad si ya existe. Requiere rol ADMINISTRADOR o BIBLIOTECARIO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro creado o actualizado exitosamente", content = @Content(schema = @Schema(implementation = LibroStock.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado por rol insuficiente", content = @Content)
    })
    @PostMapping("/crear")
    public ResponseEntity<?> crearLibroStock(@RequestBody Map<String, Object> datos) {
        try {
            String correo = datos.get("correo").toString();
            String contrasena = datos.get("contrasena").toString();
            ValidacionResponse validacion = libroStockService.validarUsuario(correo, contrasena);

            if (noTienePermiso(validacion)) {
                return ResponseEntity.status(403).body("Acceso denegado.");
            }

            LibroStock nuevoLibro = libroStockService.mapToLibroStock(datos);

            Optional<LibroStock> libroExistenteOpt = libroStockRepository.findAll().stream()
                    .filter(l -> l.getNombreLibro().equalsIgnoreCase(nuevoLibro.getNombreLibro()))
                    .findFirst();

            LibroStock resultado;
            if (libroExistenteOpt.isPresent()) {
                LibroStock existente = libroExistenteOpt.get();
                existente.setCantidad(existente.getCantidad() + nuevoLibro.getCantidad());
                resultado = libroStockRepository.save(existente);
            } else {
                resultado = libroStockRepository.save(nuevoLibro);
            }

            EntityModel<LibroStock> recurso = EntityModel.of(resultado);
            recurso.add(WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(LibroStockController.class)
                            .obtenerLibroPorId(resultado.getIdLibroStock()))
                    .withSelfRel());
            recurso.add(WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(LibroStockController.class)
                            .obtenerLibroStock())
                    .withRel("todos"));

            return ResponseEntity.ok(recurso);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno: " + e.getMessage());
        }
    }

    @Operation(summary = "Eliminar libro del stock", description = "Elimina un libro existente por ID. Solo ADMINISTRADOR o BIBLIOTECARIO pueden eliminar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro eliminado exitosamente", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado por rol insuficiente", content = @Content),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, @RequestBody Map<String, String> datos) {
        ValidacionResponse validacion = libroStockService.validarUsuario(datos.get("correo"), datos.get("contrasena"));
        if (noTienePermiso(validacion)) {
            return ResponseEntity.status(403).body("Acceso denegado.");
        }

        if (libroStockRepository.existsById(id)) {
            libroStockRepository.deleteById(id);
            return ResponseEntity.ok("Libro eliminado del stock");
        } else {
            return ResponseEntity.status(404).body("Libro no encontrado");
        }
    }

    @Operation(summary = "Actualizar cantidad de libro", description = "Modifica el número de ejemplares disponibles de un libro. No requiere validación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada correctamente", content = @Content),
            @ApiResponse(responseCode = "400", description = "Cantidad inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado", content = @Content)
    })
    @PutMapping("/actualizar-stock/{id}")
    public ResponseEntity<?> actualizarCantidad(@PathVariable Long id, @RequestBody Map<String, Integer> datos) {
        try {
            int nuevaCantidad = datos.getOrDefault("cantidad", -1);
            String mensaje = libroStockService.actualizarCantidadLibro(id, nuevaCantidad);
            return ResponseEntity.ok(mensaje);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
