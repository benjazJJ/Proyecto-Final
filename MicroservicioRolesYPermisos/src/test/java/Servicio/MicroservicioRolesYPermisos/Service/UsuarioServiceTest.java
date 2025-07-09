package Servicio.MicroservicioRolesYPermisos.Service;
import Servicio.MicroservicioRolesYPermisos.Model.Rol;
import Servicio.MicroservicioRolesYPermisos.Model.Usuario;
import Servicio.MicroservicioRolesYPermisos.Repository.RolRepository;
import Servicio.MicroservicioRolesYPermisos.Repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void registrar_exito_creaUsuarioConRolEstudianteYEncriptaClave() {
        // Arrange
        Usuario nuevo = new Usuario();
        nuevo.setCorreo("test@correo.cl");
        nuevo.setRut("12345678-9");
        nuevo.setContrasena("clave123");

        Rol rolEstudiante = new Rol(2, "ESTUDIANTE");

        when(usuarioRepository.findByCorreo("test@correo.cl")).thenReturn(Optional.empty());
        when(usuarioRepository.findByRut("12345678-9")).thenReturn(Optional.empty());
        when(rolRepository.findByNombreRol("ESTUDIANTE")).thenReturn(Optional.of(rolEstudiante));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Usuario guardado = usuarioService.registrar(nuevo);

        // Assert
        assertThat(guardado.getRol()).isEqualTo(rolEstudiante);
        assertThat(guardado.getContrasena()).isNotEqualTo("clave123");
        assertThat(Encriptador.comparar("clave123", guardado.getContrasena())).isTrue();
    }

    @Test
    void registrar_fallaPorCorreoExistente() {
        Usuario repetido = new Usuario();
        repetido.setCorreo("test@correo.cl");
        repetido.setRut("12345678-9");

        when(usuarioRepository.findByCorreo("test@correo.cl")).thenReturn(Optional.of(repetido));

        assertThatThrownBy(() -> usuarioService.registrar(repetido))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("correo");
    }

    @Test
    void registrar_fallaPorRutExistente() {
        Usuario repetido = new Usuario();
        repetido.setCorreo("nuevo@correo.cl");
        repetido.setRut("12345678-9");

        when(usuarioRepository.findByCorreo("nuevo@correo.cl")).thenReturn(Optional.empty());
        when(usuarioRepository.findByRut("12345678-9")).thenReturn(Optional.of(repetido));

        assertThatThrownBy(() -> usuarioService.registrar(repetido))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RUT");
    }

    @Test
    void autenticarYObtener_devuelveUsuarioSiCredencialesValidas() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("test@correo.cl");
        usuario.setContrasena(Encriptador.encriptar("clave123"));

        when(usuarioRepository.findByCorreo("test@correo.cl")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.autenticarYObtener("test@correo.cl", "clave123");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCorreo()).isEqualTo("test@correo.cl");
    }

    @Test
    void autenticarYObtener_retornaVacioSiClaveIncorrecta() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("test@correo.cl");
        usuario.setContrasena(Encriptador.encriptar("clave123"));

        when(usuarioRepository.findByCorreo("test@correo.cl")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.autenticarYObtener("test@correo.cl", "claveIncorrecta");

        assertThat(resultado).isEmpty();
    }

    @Test
    void autenticar_trueSiCredencialesValidas() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("test@correo.cl");
        usuario.setContrasena(Encriptador.encriptar("clave123"));

        when(usuarioRepository.findByCorreo("test@correo.cl")).thenReturn(Optional.of(usuario));

        boolean resultado = usuarioService.autenticar("test@correo.cl", "clave123");

        assertThat(resultado).isTrue();
    }

    @Test
    void autenticar_falseSiClaveIncorrecta() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("test@correo.cl");
        usuario.setContrasena(Encriptador.encriptar("clave123"));

        when(usuarioRepository.findByCorreo("test@correo.cl")).thenReturn(Optional.of(usuario));

        boolean resultado = usuarioService.autenticar("test@correo.cl", "otraClave");

        assertThat(resultado).isFalse();
    }

    @Test
    void obtenerPorId_devuelveUsuarioSiExiste() {
        Usuario usuario = new Usuario();
        usuario.setId(5);

        when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.obtenerPorId(5);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(5);
    }

    @Test
    void obtenerPorId_devuelveNullSiNoExiste() {
        when(usuarioRepository.findById(10)).thenReturn(Optional.empty());

        Usuario resultado = usuarioService.obtenerPorId(10);

        assertThat(resultado).isNull();
    }

    @Test
    void obtenerPorRut_devuelveUsuarioSiExiste() {
        Usuario usuario = new Usuario();
        usuario.setRut("12345678-9");

        when(usuarioRepository.findByRut("12345678-9")).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.obtenerPorRut("12345678-9");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getRut()).isEqualTo("12345678-9");
    }

    @Test
    void obtenerPorRut_devuelveNullSiNoExiste() {
        when(usuarioRepository.findByRut("99999999-9")).thenReturn(Optional.empty());

        Usuario resultado = usuarioService.obtenerPorRut("99999999-9");

        assertThat(resultado).isNull();
    }
}