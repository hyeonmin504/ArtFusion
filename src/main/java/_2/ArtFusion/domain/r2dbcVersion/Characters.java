package _2.ArtFusion.domain.r2dbcVersion;

import _2.ArtFusion.domain.Character.Gender;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("actor")
public class Characters {

    @Id
    @Column("actor_id")
    private Long id;

    private String clothes;
    private String personality;
    @Column("actor_prompt")
    private String characterPrompt;
    private String name;
    private String appearance;
    private Gender gender;

    @Column("story_id")
    private Long storyId;

    @Builder
    public Characters(String characterPrompt, Gender gender, String name, Long storyId) {
        this.characterPrompt = characterPrompt;
        this.gender = gender;
        this.name = name;
        this.storyId = storyId;
    }
}