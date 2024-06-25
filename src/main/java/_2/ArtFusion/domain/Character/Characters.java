package _2.ArtFusion.domain.Character;

import _2.ArtFusion.domain.storyboard.StoryBoard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Characters {

    @Id @GeneratedValue
    @Column(name = "character_id")
    private Long id;

    private String clothes;
    private String personality;
    private String characterPrompt;
    private String name;
    private String appearance;

    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "story_id")
    private StoryBoard storyBoard;

    @Builder
    public Characters(String characterPrompt,  Gender gender, String name,StoryBoard storyBoard) {
        this.characterPrompt = characterPrompt;
        this.gender = gender;
        this.name = name;
        setStoryBoard(storyBoard);
    }

    // -- 연관 관계 세팅 메서드 -- //
    public void setStoryBoard(StoryBoard storyBoard) {
        this.storyBoard = storyBoard;
        storyBoard.getCharacters().add(this);
    }
}
