package Servicio.MicroservicioMultas.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import Servicio.MicroservicioMultas.Model.Multa;
import Servicio.MicroservicioMultas.Repository.MultaRepository;
import Servicio.MicroservicioMultas.WebClient.MultasMult;
import Servicio.MicroservicioMultas.WebClient.MultasClient;
import Servicio.MicroservicioMultas.WebClient.ValidacionResponse;

@Service
public class MultaService {

    @Autowired
    private MultaRepository multaRepository;

    @Autowired
    private MultasMult multasMult;

    @Autowired
    private MultasClient multasClient; // Cliente para validar runUsuario

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8081") // Microservicio de autenticación
            .build();

    public List<Multa> obtenerTodasLasMultas() {
        return multaRepository.findAll();
    }

    public Optional<Multa> obtenerMultaPorId(Long id) {
        return multaRepository.findById(id);
    }

    public Multa crearMulta(Multa multa, String correo, String contrasena) {
        // Validar que quien crea la multa tiene rol válido
        ValidacionResponse response = validarUsuario(correo, contrasena);

        if (!response.getRol().equalsIgnoreCase("ADMINISTRADOR") &&
            !response.getRol().equalsIgnoreCase("BIBLIOTECARIO")) {
            throw new RuntimeException("Solo administradores o bibliotecarios pueden registrar multas.");
        }

        // Validar que la devolución asociada existe
        Map<String, Object> mult = multasMult.getDevolucionById(multa.getIdDevolucion());
        if (mult == null || mult.isEmpty()) {
            throw new RuntimeException("Devolución no encontrada. No se puede asignar la multa.");
        }

        // Validar que el RUT pertenece a un usuario real
        if (!multasClient.validarUsuarioPorRut(multa.getRunUsuario())) {
            throw new RuntimeException("El RUT proporcionado no pertenece a ningún usuario registrado.");
        }

        return multaRepository.save(multa);
    }

    public Optional<Multa> actualizarMulta(Long id, Multa multaActualizada) {
        return multaRepository.findById(id).map(multa -> {
            multa.setRunUsuario(multaActualizada.getRunUsuario());
            multa.setTipoSancion(multaActualizada.getTipoSancion());
            multa.setSancion(multaActualizada.getSancion());
            return multaRepository.save(multa);
        });
    }

    public boolean eliminarMulta(Long id) {
        if (multaRepository.existsById(id)) {
            multaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Método para validar correo y clave
    public ValidacionResponse validarUsuario(String correo, String contrasena) {
        ValidacionResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/autenticacion/validar")
                        .queryParam("correo", correo)
                        .queryParam("contrasena", contrasena)
                        .build())
                .retrieve()
                .bodyToMono(ValidacionResponse.class)
                .block();

        if (response == null || !response.isAutenticado()) {
            throw new RuntimeException("Credenciales inválidas o usuario no encontrado.");
        }

        return response;
    }
}
