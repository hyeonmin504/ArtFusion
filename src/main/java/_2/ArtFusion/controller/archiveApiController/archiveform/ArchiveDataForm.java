package _2.ArtFusion.controller.archiveApiController.archiveform;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArchiveDataForm {
    private Long postId;
    private String coverImg;
    private String title;
    private String summary;
    private String nickName;
    private String hashTag;
}
