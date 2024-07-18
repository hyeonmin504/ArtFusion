package _2.ArtFusion.domain.actor;

import _2.ArtFusion.domain.storyboard.StoryBoard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "actor")
public class Actor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "actor_id")
    private Long id;

    private String clothes;
    private String personality;
    @Column(name = "actor_prompt")
    private String characterPrompt;
    private String name;
    private String appearance;

    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "story_id")
    private StoryBoard storyBoard;

    @Builder
    public Actor(String characterPrompt,  Gender gender, String name,StoryBoard storyBoard) {
        this.characterPrompt = characterPrompt;
        this.gender = gender;
        this.name = name;
        setStoryBoard(storyBoard);
    }

    // -- 연관 관계 세팅 메서드 -- //
    public void setStoryBoard(StoryBoard storyBoard) {
        this.storyBoard = storyBoard;
        storyBoard.getActors().add(this);
    }
}
