package db.migration;

import org.flywaydb.core.api.migration.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.ArrayList;

//se ejecuta una vez; conserva las claves actuales, guardandolas como hashes BCrypt
public class V3__encriptar_claves extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        record Clave(Long id, String valor) {}
        var claves = new ArrayList<Clave>();
        try (var consulta = context.getConnection().prepareStatement("SELECT id, clave FROM usuario");
             var filas = consulta.executeQuery()) {
            while (filas.next()) claves.add(new Clave(filas.getLong("id"), filas.getString("clave")));
        }
        var encoder = new BCryptPasswordEncoder();
        try (var actualizar = context.getConnection().prepareStatement("UPDATE usuario SET clave = ? WHERE id = ?")) {
            for (var clave : claves) {
                if (!clave.valor().matches("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}")) {
                    actualizar.setString(1, encoder.encode(clave.valor()));
                    actualizar.setLong(2, clave.id());
                    actualizar.executeUpdate();
                }
            }
        }
    }
}
