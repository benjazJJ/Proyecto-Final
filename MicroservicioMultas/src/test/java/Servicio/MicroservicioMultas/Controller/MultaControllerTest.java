package Servicio.MicroservicioMultas.Controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import Servicio.MicroservicioMultas.Model.Multa;
import Servicio.MicroservicioMultas.Service.MultaService;
import Servicio.MicroservicioMultas.WebClient.ValidacionResponse;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(MultaController.class)
public class MultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MultaService multaService;

    @Test
    void listarTodas_retornaMultasParaEstudiante() throws Exception {
        ValidacionResponse auth = new ValidacionResponse(true, 0, "correo@correo.cl", "ESTUDIANTE");
        List<Multa> lista = List.of(new Multa(1L, "12345678-9", "Retraso", "Bloqueo 7 días", 1));

        when(multaService.validarUsuario("correo@correo.cl", "1234")).thenReturn(auth);
        when(multaService.obtenerTodasLasMultas()).thenReturn(lista);

        String json = "{\"correo\":\"correo@correo.cl\",\"contrasena\":\"1234\"}";

        mockMvc.perform(post("/api/v1/multas/listar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listarTodas_conRolNoAutorizado() throws Exception {
        ValidacionResponse auth = new ValidacionResponse(true, 0, "admin@correo.cl", "ADMINISTRADOR");
        when(multaService.validarUsuario("admin@correo.cl", "admin123")).thenReturn(auth);

        String json = "{\"correo\":\"admin@correo.cl\",\"contrasena\":\"admin123\"}";

        mockMvc.perform(post("/api/v1/multas/listar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerPorId_retornaMultaParaDocente() throws Exception {
        ValidacionResponse auth = new ValidacionResponse(true, 1, "correo@correo.cl", "DOCENTE");
        Multa multa = new Multa(2L, "22222222-2", "Grave", "Suspensión", 2);

        when(multaService.validarUsuario("correo@correo.cl", "1234")).thenReturn(auth);
        when(multaService.obtenerMultaPorId(2L)).thenReturn(Optional.of(multa));

        String json = "{\"correo\":\"correo@correo.cl\",\"contrasena\":\"1234\"}";

        mockMvc.perform(post("/api/v1/multas/ver/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void obtenerPorId_conRolNoAutorizado() throws Exception {
        ValidacionResponse auth = new ValidacionResponse(true, 4, "admin@correo.cl", "ADMINISTRADOR");
        when(multaService.validarUsuario("admin@correo.cl", "1234")).thenReturn(auth);

        String json = "{\"correo\":\"admin@correo.cl\",\"contrasena\":\"1234\"}";

        mockMvc.perform(post("/api/v1/multas/ver/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearMulta_retorna201ParaAdmin() throws Exception {
        ValidacionResponse auth = new ValidacionResponse(true, 2, "admin@correo.cl", "ADMINISTRADOR");
        Multa nueva = new Multa(10L, "88888888-8", "Grave", "Bloqueo indefinido", 4);

        when(multaService.validarUsuario("admin@correo.cl", "admin123")).thenReturn(auth);
        when(multaService.crearMulta(any(Multa.class), eq("admin@correo.cl"), eq("admin123"))).thenReturn(nueva);

        String json = "{" +
                "\"correo\":\"admin@correo.cl\"," +
                "\"contrasena\":\"admin123\"," +
                "\"runUsuario\":\"88888888-8\"," +
                "\"tipoSancion\":\"Grave\"," +
                "\"sancion\":\"Bloqueo indefinido\"," +
                "\"idDevolucion\":4}";

        mockMvc.perform(post("/api/v1/multas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void crearMulta_conRolInvalido_retorna403() throws Exception {
        ValidacionResponse auth = new ValidacionResponse(true, 5, "cliente@correo.cl", "CLIENTE");
        when(multaService.validarUsuario("cliente@correo.cl", "0000")).thenReturn(auth);

        String json = "{" +
                "\"correo\":\"cliente@correo.cl\"," +
                "\"contrasena\":\"0000\"," +
                "\"runUsuario\":\"11111111-1\"," +
                "\"tipoSancion\":\"Leve\"," +
                "\"sancion\":\"Advertencia\"," +
                "\"idDevolucion\":1}";

        mockMvc.perform(post("/api/v1/multas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizarMulta_conRolInvalido_retorna403() throws Exception {
        ValidacionResponse auth = new ValidacionResponse(true, 6, "invalido@correo.cl", "CLIENTE");
        when(multaService.validarUsuario("invalido@correo.cl", "0000")).thenReturn(auth);

        String json = "{" +
                "\"correo\":\"invalido@correo.cl\"," +
                "\"contrasena\":\"0000\"," +
                "\"runUsuario\":\"11111111-1\"," +
                "\"tipoSancion\":\"Media\"," +
                "\"sancion\":\"Bloqueo 3 días\"}";

        mockMvc.perform(put("/api/v1/multas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void eliminarMulta_conRolInvalido_retorna403() throws Exception {
        ValidacionResponse auth = new ValidacionResponse(true, 7, "otro@correo.cl", "CLIENTE");
        when(multaService.validarUsuario("otro@correo.cl", "0000")).thenReturn(auth);

        String json = "{\"correo\":\"otro@correo.cl\",\"contrasena\":\"0000\"}";

        mockMvc.perform(delete("/api/v1/multas/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }
}
