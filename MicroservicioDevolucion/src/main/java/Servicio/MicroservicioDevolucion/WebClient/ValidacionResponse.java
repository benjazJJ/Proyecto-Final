package Servicio.MicroservicioDevolucion.WebClient;

import lombok.Data;

@Data
public class ValidacionResponse {
    private boolean autenticado;
    private int idUsuario;
    private String correo;
    private String rol;
}
