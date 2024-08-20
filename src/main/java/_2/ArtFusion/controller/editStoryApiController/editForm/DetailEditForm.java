package _2.ArtFusion.controller.editStoryApiController.editForm;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetailEditForm {
    @NotNull
    @JsonProperty("image_id")
    private Long imageId;

    public DetailEditForm() {}

    public DetailEditForm(Long imageId) {
        this.imageId = imageId;
    }
}
