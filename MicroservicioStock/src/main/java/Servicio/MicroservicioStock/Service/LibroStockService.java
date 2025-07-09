package Servicio.MicroservicioStock.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import Servicio.MicroservicioStock.Model.Categoria;
import Servicio.MicroservicioStock.Model.EstadoLibro;
import Servicio.MicroservicioStock.Model.LibroStock;
import Servicio.MicroservicioStock.Repository.CategoriaRepository;
import Servicio.MicroservicioStock.Repository.EstadoLibroRepository;
import Servicio.MicroservicioStock.Repository.LibroStockRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LibroStockService {
    @Autowired
    private LibroStockRepository libroStockRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EstadoLibroRepository estadoLibroRepository;

    public List<LibroStock> obtenerLibroStock() {
        return libroStockRepository.findAll();
    }

    public Optional<LibroStock> obtenerId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
        return libroStockRepository.findById(id);
    }

    public LibroStock guardarLibroStock(LibroStock libroStock) {
        if (libroStock == null) {
            throw new IllegalArgumentException("El libro no puede ser nulo.");
        }
        if (libroStock.getNombreLibro() == null || libroStock.getNombreLibro().isBlank()) {
            throw new IllegalArgumentException("El nombre del libro no puede estar vacío.");
        }
        if (libroStock.getEstante() == null || libroStock.getEstante().isBlank()) {
            throw new IllegalArgumentException("El estante no puede estar vacío.");
        }
        if (libroStock.getFila() == null || libroStock.getFila().isBlank()) {
            throw new IllegalArgumentException("La fila no puede estar vacía.");
        }
        if (libroStock.getCantidad() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }

        return libroStockRepository.save(libroStock);
    }

    public String actualizarCantidadLibro(Long id, int nuevaCantidad) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }

        Optional<LibroStock> libroOpt = libroStockRepository.findById(id);
        if (libroOpt.isEmpty()) {
            throw new IllegalArgumentException("Libro no encontrado con ID: " + id);
        }

        LibroStock libro = libroOpt.get();
        libro.setCantidad(nuevaCantidad);
        libroStockRepository.save(libro);

        return "Cantidad actualizada correctamente";
    }

    public void eliminar(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
        if (!libroStockRepository.existsById(id)) {
            throw new IllegalArgumentException("El libro con ID " + id + " no existe.");
        }
        libroStockRepository.deleteById(id);
    }

    public ValidacionResponse validarUsuario(String correo, String contrasena) {
        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();

        try {
            return client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/autenticacion/validar")
                            .queryParam("correo", correo)
                            .queryParam("contrasena", contrasena)
                            .build())
                    .retrieve()
                    .bodyToMono(ValidacionResponse.class)
                    .block();
        } catch (WebClientResponseException.Unauthorized e) {
            ValidacionResponse r = new ValidacionResponse();
            r.setAutenticado(false);
            r.setRol("DESCONOCIDO");
            return r;
        }
    }

    public LibroStock mapToLibroStock(Map<String, Object> datos) {
        String nombreLibro = datos.get("nombreLibro").toString();
        String estante = datos.get("estante").toString();
        String fila = datos.get("fila").toString();
        int cantidad = Integer.parseInt(datos.get("cantidad").toString());
        String nombreCategoria = datos.get("categoria").toString();
        String nombreEstado = datos.get("estado").toString();

        Categoria categoria = categoriaRepository.findAll().stream()
                .filter(c -> c.getNombreCategoria().equalsIgnoreCase(nombreCategoria))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoría no válida. Usa una de las siguientes: Ciencia Ficción, Fantasía, Historia, Educación."));

        EstadoLibro estado = estadoLibroRepository.findAll().stream()
                .filter(e -> e.getNombreEstado().equalsIgnoreCase(nombreEstado))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Estado no válido. Usa una de las siguientes opciones: Disponible, Agotado."));

        LibroStock libro = new LibroStock();
        libro.setNombreLibro(nombreLibro);
        libro.setEstante(estante);
        libro.setFila(fila);
        libro.setCantidad(cantidad);
        libro.setCategoria(categoria);
        libro.setEstado(estado);

        return libro;
    }
}
