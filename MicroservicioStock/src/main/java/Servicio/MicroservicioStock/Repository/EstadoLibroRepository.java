package Servicio.MicroservicioStock.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Servicio.MicroservicioStock.Model.EstadoLibro;

public interface EstadoLibroRepository extends JpaRepository<EstadoLibro, Long> {

 }
