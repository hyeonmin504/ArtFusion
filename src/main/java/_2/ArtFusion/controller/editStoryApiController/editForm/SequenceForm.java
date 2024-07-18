package _2.ArtFusion.controller.editStoryApiController.editForm;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SequenceForm {
    @NotEmpty
    private Long sceneId;
    @NotNull
    private int newSceneSeq;
}
