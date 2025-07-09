package Servicio.MicroservicioNotificaciones.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Servicio.MicroservicioNotificaciones.Model.Notificaciones;

import java.util.List;

@Repository
public interface NotificacionesRepository extends JpaRepository<Notificaciones, Integer> {

    // Buscar notificaciones por correo del emisor
    List<Notificaciones> findByCorreoEmisor(String correoEmisor);

    // Buscar notificaciones por correo del receptor
    List<Notificaciones> findByCorreoReceptor(String correoReceptor);
}