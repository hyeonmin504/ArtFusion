package _2.ArtFusion.controller.archiveApiController.archiveform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ArchiveData {
    private Long postId;
    private String coverImg;
    private String title;
    private String summary;
    private String nickname;
    private List<String> hashTag = new ArrayList<>();

    public ArchiveData(Long postId, String coverImg, String title, String summary, String nickname) {
        this.postId = postId;
        this.coverImg = coverImg;
        this.title = title;
        this.summary = summary;
        this.nickname = nickname;
    }

    public void addHashTags(String hashTag) {
        this.hashTag = List.of(hashTag.split(","));
    }
}
