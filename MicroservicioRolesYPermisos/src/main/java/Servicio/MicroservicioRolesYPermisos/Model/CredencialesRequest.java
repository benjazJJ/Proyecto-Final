package Servicio.MicroservicioRolesYPermisos.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Objeto utilizado para enviar credenciales de acceso")
public class CredencialesRequest {

    @Schema(description = "Correo electrónico del usuario", example = "admin@instituto.cl")
    private String correo;

    @Schema(description = "Contraseña del usuario", example = "123456")
    private String contrasena;

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
