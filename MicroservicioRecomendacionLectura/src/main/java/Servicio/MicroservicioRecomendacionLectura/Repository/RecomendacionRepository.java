package Servicio.MicroservicioRecomendacionLectura.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Servicio.MicroservicioRecomendacionLectura.Model.Recomendacion;

import java.util.List;

public interface RecomendacionRepository extends JpaRepository<Recomendacion, Integer> {
    List<Recomendacion> findByCategoria(String categoria);
}
