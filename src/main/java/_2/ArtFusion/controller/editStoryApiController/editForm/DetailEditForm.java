package _2.ArtFusion.controller.editStoryApiController.editForm;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
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
