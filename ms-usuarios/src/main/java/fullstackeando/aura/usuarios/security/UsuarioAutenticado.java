package fullstackeando.aura.usuarios.security;

import fullstackeando.aura.usuarios.model.Usuario;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.List;
import java.util.Locale;

//el id permite comprobar si el cliente consulta sus propios datos
public class UsuarioAutenticado extends User {
    private final Long id;

    public UsuarioAutenticado(Usuario usuario) {
        super(usuario.getCorreo(), usuario.getClave(), usuario.isActivo(), true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().toUpperCase(Locale.ROOT))));
        this.id = usuario.getId();
    }

    public Long getId() { return id; }
}
