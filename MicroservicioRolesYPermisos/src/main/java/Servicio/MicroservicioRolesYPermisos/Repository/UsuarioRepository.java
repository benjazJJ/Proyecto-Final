package Servicio.MicroservicioRolesYPermisos.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import Servicio.MicroservicioRolesYPermisos.Model.Usuario;


public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Buscar por correo (para verificar existencia o login)
    Optional<Usuario> findByCorreo(String correo);

    // Buscar por rut (evita duplicados al registrar)
    Optional<Usuario> findByRut(String rut);

    // Buscar por correo y contraseña (para autenticación segura)
    Optional<Usuario> findByCorreoAndContrasena(String correo, String contrasena);
}
