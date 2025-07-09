package Servicio.MicroservicioPrestamo.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import Servicio.MicroservicioPrestamo.Model.Prestamo;
import Servicio.MicroservicioPrestamo.Service.PrestamoService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrestamoController.class)
public class PrestamoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrestamoService prestamoService;

    /**
     * POST /api/v1/prestamos
     * Crea un nuevo préstamo si las credenciales son válidas.
     */
    @Test
    void crearPrestamo_valido_retorna201() throws Exception {
        Prestamo prestamo = new Prestamo(null, 1, 2L, "11111111-1", Date.valueOf(LocalDate.now()), null, 7);
        when(prestamoService.crearPrestamoSiEsValido(any(), any(), any())).thenReturn(prestamo);

        String body = """
            {
                "correo": "estudiante@correo.cl",
                "contrasena": "1234",
                "runSolicitante": "11111111-1",
                "idLibro": 2,
                "cantidadDias": 7
            }
        """;

        mockMvc.perform(post("/api/v1/prestamos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.runSolicitante").value("11111111-1"));
    }

    /**
     * GET /api/v1/prestamos
     * Devuelve lista de todos los préstamos.
     */
    @Test
    void obtenerTodosLosPrestamos_retornaLista() throws Exception {
        Prestamo prestamo = new Prestamo(1, 1, 2L, "11111111-1", Date.valueOf(LocalDate.now()), null, 7);
        when(prestamoService.obtenerTodosLosPrestamos()).thenReturn(List.of(prestamo));

        mockMvc.perform(get("/api/v1/prestamos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPrestamo").value(1));
    }

    /**
     * GET /api/v1/prestamos/1
     * Devuelve préstamo por ID si existe.
     */
    @Test
    void obtenerPrestamoPorId_existente_retornaPrestamo() throws Exception {
        Prestamo prestamo = new Prestamo(1, 1, 2L, "11111111-1", Date.valueOf(LocalDate.now()), null, 7);
        when(prestamoService.obtenerPrestamoPorId(1)).thenReturn(prestamo);

        mockMvc.perform(get("/api/v1/prestamos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPrestamo").value(1));
    }

    /**
     * GET /api/v1/prestamos/run/{run}
     * Devuelve préstamos filtrados por RUN del solicitante.
     */
    @Test
    void obtenerPrestamosPorRun_conResultados() throws Exception {
        Prestamo prestamo = new Prestamo(1, 1, 2L, "11111111-1", Date.valueOf(LocalDate.now()), null, 7);
        when(prestamoService.obtenerPrestamosPorRun("11111111-1")).thenReturn(List.of(prestamo));

        mockMvc.perform(get("/api/v1/prestamos/run/11111111-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].runSolicitante").value("11111111-1"));
    }

    /**
     * GET /api/v1/prestamos/pendientes
     * Devuelve préstamos marcados como pendientes.
     */
    @Test
    void obtenerPrestamosPendientes_conResultados() throws Exception {
        Prestamo prestamo = new Prestamo(1, 1, 2L, "11111111-1", Date.valueOf(LocalDate.now()), null, 7);
        when(prestamoService.obtenerPrestamoPendientes()).thenReturn(List.of(prestamo));

        mockMvc.perform(get("/api/v1/prestamos/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].runSolicitante").value("11111111-1"));
    }
}