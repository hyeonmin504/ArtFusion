package _2.ArtFusion.controller.editStoryApiController.editForm;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContentEditForm {
    @NotEmpty
    private String background;
    @NotEmpty
    private String description;
    private String dialogue;
}
