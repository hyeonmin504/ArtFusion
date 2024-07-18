package _2.ArtFusion.controller.generateStoryApiController.storyForm;

import _2.ArtFusion.domain.r2dbcVersion.Characters;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class ActorAndStoryIdForm {
    private List<Characters> characters;
    private Long storyId;
}
