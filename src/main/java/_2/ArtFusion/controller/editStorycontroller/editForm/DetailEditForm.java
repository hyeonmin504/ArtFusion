package _2.ArtFusion.controller.editStorycontroller.editForm;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetailEditForm {
    private Long imageId;
    @Size(max = 60000)
    private String sceneModifyPrompt;
}
