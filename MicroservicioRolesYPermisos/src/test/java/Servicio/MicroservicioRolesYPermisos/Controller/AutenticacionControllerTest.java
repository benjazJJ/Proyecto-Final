package Servicio.MicroservicioRolesYPermisos.Controller;

import Servicio.MicroservicioRolesYPermisos.Model.Rol;
import Servicio.MicroservicioRolesYPermisos.Model.Usuario;
import Servicio.MicroservicioRolesYPermisos.Service.UsuarioService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AutenticacionController.class)
public class AutenticacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrarUsuario_deberiaRetornar200SiExitoso() throws Exception {
        Usuario nuevo = new Usuario();
        nuevo.setCorreo("test@correo.cl");
        nuevo.setNombre("Juan Pérez");
        nuevo.setContrasena("1234");
        nuevo.setRut("12345678-9");

        when(usuarioService.registrar(any())).thenReturn(nuevo);

        mockMvc.perform(post("/api/v1/autenticacion/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isOk());
    }

    @Test
    void registrarUsuario_deberiaRetornar409SiCorreoYaExiste() throws Exception {
        Usuario duplicado = new Usuario();
        duplicado.setCorreo("duplicado@correo.cl");
        duplicado.setContrasena("1234");

        when(usuarioService.registrar(any())).thenThrow(new IllegalStateException("Ya existe una cuenta con este correo."));

        mockMvc.perform(post("/api/v1/autenticacion/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_deberiaRetornar200SiCredencialesSonValidas() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setCorreo("correo@valido.cl");
        usuario.setContrasena("clave123");

        when(usuarioService.autenticar("correo@valido.cl", "clave123")).thenReturn(true);

        mockMvc.perform(post("/api/v1/autenticacion/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(content().string("Autenticación exitosa"));
    }

    @Test
    void login_deberiaRetornar401SiCredencialesSonInvalidas() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setCorreo("correo@invalido.cl");
        usuario.setContrasena("claveIncorrecta");

        when(usuarioService.autenticar("correo@invalido.cl", "claveIncorrecta")).thenReturn(false);

        mockMvc.perform(post("/api/v1/autenticacion/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciales inválidas"));
    }

    @Test
    void validar_deberiaRetornar200SiUsuarioEsValido() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(5);
        usuario.setCorreo("usuario@correo.cl");
        usuario.setContrasena("1234");
        usuario.setRol(new Rol(2, "ESTUDIANTE"));

        when(usuarioService.autenticarYObtener("usuario@correo.cl", "1234"))
                .thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/v1/autenticacion/validar")
                .param("correo", "usuario@correo.cl")
                .param("contrasena", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.correo").value("usuario@correo.cl"))
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.idUsuario").value(5));
    }

    @Test
    void validar_deberiaRetornar401SiCredencialesSonInvalidas() throws Exception {
        when(usuarioService.autenticarYObtener("x@x.cl", "123")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/autenticacion/validar")
                .param("correo", "x@x.cl")
                .param("contrasena", "123"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciales inválidas"));
    }
}