package _2.ArtFusion.service.util.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenAiImageResponseForm {
    private int created;
    private List<ImageUrl> data;
}
