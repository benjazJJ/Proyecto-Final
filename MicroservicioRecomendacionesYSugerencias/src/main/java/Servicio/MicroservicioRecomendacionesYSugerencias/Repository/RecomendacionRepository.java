package Servicio.MicroservicioRecomendacionesYSugerencias.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Servicio.MicroservicioRecomendacionesYSugerencias.Model.RecomendacionesySugerencias;
import io.micrometer.common.lang.NonNull;

public interface RecomendacionRepository extends JpaRepository<RecomendacionesySugerencias, Integer> {

    Optional<RecomendacionesySugerencias> findById(@NonNull Integer idEncuesta);

    boolean existsByIdUsuario(Integer idUsuario);
}

