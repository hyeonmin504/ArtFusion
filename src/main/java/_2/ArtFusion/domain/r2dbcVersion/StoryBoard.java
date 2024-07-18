package _2.ArtFusion.domain.r2dbcVersion;

import _2.ArtFusion.domain.storyboard.Style;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("story_board")
public class StoryBoard {

    @Id
    @Column("story_id")
    private Long id;

    @Column("story_prompt")
    private String promptKor;
    private String title;
    private Style style;
    @Column("generate_type")
    private String generateType;
    private String genre;
    @Column("cut_cnt")
    private int wishCutCount;
    @Column("user_id")
    private Long userId;  // userId를 직접 필드로 추가

    @Builder
    public StoryBoard(String promptKor, String title, Style style, String generateType, String genre, int wishCutCount, Long userId) {
        this.promptKor = promptKor;
        this.title = title;
        this.style = style;
        this.generateType = generateType;
        this.genre = genre;
        this.wishCutCount = wishCutCount;
        this.userId = userId;
    }
}
