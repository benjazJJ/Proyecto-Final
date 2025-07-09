package Servicio.MicroservicioRolesYPermisos.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import Servicio.MicroservicioRolesYPermisos.Model.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    // Buscar por nombre del rol (ej: "ESTUDIANTE", "DOCENTE", "ADMINISTRADOR", "BIBLIOTECARIO")
    Optional<Rol> findByNombreRol(String nombreRol);
}