package Servicio.MicroservicioRolesYPermisos.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Servicio.MicroservicioRolesYPermisos.Model.Rol;
import Servicio.MicroservicioRolesYPermisos.Model.Usuario;
import Servicio.MicroservicioRolesYPermisos.Repository.RolRepository;
import Servicio.MicroservicioRolesYPermisos.Repository.UsuarioRepository;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    // Valida que el correo termine en @instituto.cl
    private void validarCorreoInstitucional(String correo) {
        if (correo == null || !correo.toLowerCase().endsWith("@instituto.cl")) {
            throw new IllegalStateException("Solo se permiten correos institucionales (@instituto.cl).");
        }
    }

    // Registra un nuevo usuario como estudiante
    public Usuario registrar(Usuario usuario) {
        validarCorreoInstitucional(usuario.getCorreo());

        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            throw new IllegalStateException("Ya existe una cuenta con este correo.");
        }

        if (usuarioRepository.findByRut(usuario.getRut()).isPresent()) {
            throw new IllegalStateException("Ya existe una cuenta con este RUT.");
        }

        Rol rolEstudiante = rolRepository.findByNombreRol("ESTUDIANTE")
                .orElseThrow(() -> new RuntimeException("Rol ESTUDIANTE no encontrado."));
        usuario.setRol(rolEstudiante);

        String encriptada = Encriptador.encriptar(usuario.getContrasena());
        usuario.setContrasena(encriptada);

        return usuarioRepository.save(usuario);
    }

    // Actualizar datos personales del usuario
    public Usuario actualizarUsuario(int id, Usuario cambios) {
        Usuario original = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado."));

        if (!original.getCorreo().equals(cambios.getCorreo())) {
            validarCorreoInstitucional(cambios.getCorreo());

            usuarioRepository.findByCorreo(cambios.getCorreo()).ifPresent(usuarioExistente -> {
                if (usuarioExistente.getId() != original.getId()) {
                    throw new IllegalStateException("Ya existe una cuenta con este correo.");
                }
            });

            original.setCorreo(cambios.getCorreo());
        }

        if (!original.getRut().equals(cambios.getRut())) {
            usuarioRepository.findByRut(cambios.getRut()).ifPresent(usuarioExistente -> {
                if (usuarioExistente.getId() != original.getId()) {
                    throw new IllegalStateException("Ya existe una cuenta con este RUT.");
                }
            });

            original.setRut(cambios.getRut());
        }

        original.setNombre(cambios.getNombre());
        original.setTelefono(cambios.getTelefono());

        if (cambios.getContrasena() != null && !cambios.getContrasena().isBlank()) {
            String nuevaClave = Encriptador.encriptar(cambios.getContrasena());
            original.setContrasena(nuevaClave);
        }

        return usuarioRepository.save(original);
    }

    public Optional<Usuario> autenticarYObtener(String correo, String contrasena) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        if (usuarioOpt.isPresent() &&
                Encriptador.comparar(contrasena, usuarioOpt.get().getContrasena())) {
            return usuarioOpt;
        }
        return Optional.empty();
    }

    public Usuario obtenerPorId(int id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario obtenerPorRut(String rut) {
        return usuarioRepository.findByRut(rut).orElse(null);
    }

    public boolean autenticar(String correo, String contrasena) {
        return autenticarYObtener(correo, contrasena).isPresent();
    }

    public void eliminar(int id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalStateException("El usuario no existe.");
        }
        try {
            usuarioRepository.deleteById(id);
        } catch (Exception e) {
            throw new IllegalStateException("No se puede eliminar el usuario. Está relacionado con otros datos.");
        }
    }

}
