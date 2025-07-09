package Servicio.MicroservicioStock.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import Servicio.MicroservicioStock.Model.LibroStock;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroStockRepository extends JpaRepository<LibroStock, Long>{
    
    List<LibroStock> findByNombreLibro(String nombreLibro); //Buscar Libro por nombre

    Optional<LibroStock> findByEstanteAndFila(String estante, String fila); //Buscar libro por estante y fila

}
