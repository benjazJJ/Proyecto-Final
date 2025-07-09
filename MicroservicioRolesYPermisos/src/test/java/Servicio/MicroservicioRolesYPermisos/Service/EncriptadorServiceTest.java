package Servicio.MicroservicioRolesYPermisos.Service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EncriptadorServiceTest {

    @Test
    void encriptarYComparar_deberiaRetornarTrueConContrasenaCorrecta() {
        // Arrange
        String original = "miClave123";

        // Act
        String hash = Encriptador.encriptar(original);
        boolean resultado = Encriptador.comparar(original, hash);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    void comparar_conContrasenaIncorrecta_deberiaRetornarFalse() {
        // Arrange
        String original = "miClave123";
        String incorrecta = "otraClave";

        // Act
        String hash = Encriptador.encriptar(original);
        boolean resultado = Encriptador.comparar(incorrecta, hash);

        // Assert
        assertThat(resultado).isFalse();
    }

    @Test
    void encriptar_deberiaGenerarHashDiferenteCadaVez() {
        // Arrange
        String clave = "miClave123";

        // Act
        String hash1 = Encriptador.encriptar(clave);
        String hash2 = Encriptador.encriptar(clave);

        // Assert
        assertThat(hash1).isNotEqualTo(hash2); // porque BCrypt usa sal interna
        assertThat(Encriptador.comparar(clave, hash1)).isTrue();
        assertThat(Encriptador.comparar(clave, hash2)).isTrue();
    }
}