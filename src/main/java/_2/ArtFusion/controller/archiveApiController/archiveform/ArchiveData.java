package _2.ArtFusion.controller.archiveApiController.archiveform;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArchiveData {
    private Long postId;
    private String coverImg;
    private String title;
    private String summary;
    private String nickname;
    private String hashTag;
}
