package _2.ArtFusion.controller.editStorycontroller.editForm;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContentEditForm {
    private String background;
    @NotEmpty
    private String description;
    private String dialogue;
}