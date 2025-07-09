package Servicio.MicroservicioStock.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Servicio.MicroservicioStock.Model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

 }
