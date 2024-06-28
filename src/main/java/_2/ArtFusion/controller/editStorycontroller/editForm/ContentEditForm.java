package _2.ArtFusion.controller.editStorycontroller.editForm;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ContentEditForm {
    @NotNull
    private Long sceneId;
    private String background;
    @NotEmpty
    private String description;
    private String dialogue;
}
