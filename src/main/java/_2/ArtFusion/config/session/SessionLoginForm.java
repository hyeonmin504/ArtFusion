package _2.ArtFusion.config.session;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SessionLoginForm {
    @NotNull
    private String email;
}
