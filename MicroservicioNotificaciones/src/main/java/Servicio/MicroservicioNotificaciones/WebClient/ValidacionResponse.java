package Servicio.MicroservicioNotificaciones.WebClient;


import lombok.Data;

@Data
public class ValidacionResponse {
    private boolean autenticado;
    private String rol;
    private int idUsuario;
    private String correo;
}
