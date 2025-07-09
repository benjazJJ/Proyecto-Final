package Servicio.MicroservicioMultas.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Servicio.MicroservicioMultas.Model.Multa;

@Repository
public interface MultaRepository extends JpaRepository<Multa, Long> {
    
}