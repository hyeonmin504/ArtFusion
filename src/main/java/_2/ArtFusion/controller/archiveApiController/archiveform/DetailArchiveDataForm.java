package _2.ArtFusion.controller.archiveApiController.archiveform;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class DetailArchiveDataForm {
    private Long storyId;
    private String nickName;
    private LocalDateTime createDate;
    private String hashTag;

    public DetailArchiveDataForm(Long storyId, String nickName, LocalDateTime createDate, String hashTag) {
        this.storyId = storyId;
        this.nickName = nickName;
        this.createDate = createDate;
        this.hashTag = hashTag;
    }
}
