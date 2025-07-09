package Servicio.MicroservicioSolicitudesAyuda.WebClient;

import lombok.Data;

@Data
public class ValidacionResponse {

    private boolean autenticado;
    private String rol;

}