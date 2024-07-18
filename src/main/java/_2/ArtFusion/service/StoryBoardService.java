package _2.ArtFusion.service;

import _2.ArtFusion.controller.generateStoryController.TemporaryStoryController;
import _2.ArtFusion.controller.generateStoryController.storyForm.ActorAndStoryIdForm;
import _2.ArtFusion.controller.generateStoryController.storyForm.GenerateTemporaryForm;
import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.r2dbcVersion.Characters;
import _2.ArtFusion.domain.r2dbcVersion.StoryBoard;
import _2.ArtFusion.domain.storyboard.CaptureImage;
import _2.ArtFusion.domain.storyboard.Style;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.repository.jpa.*;
import _2.ArtFusion.repository.r2dbc.CharacterR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.StoryBoardR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.UserR2DBCRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static _2.ArtFusion.service.convertUtil.ConvertUtil.convertCharacter;
import static _2.ArtFusion.service.convertUtil.ConvertUtil.convertStoryBoard;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoryBoardService {

    private final UserR2DBCRepository userR2DBCRepository;
    private final ArchiveRepository archiveRepository;
    private final CaptureImageRepository captureImageRepository;
    private final CharacterR2DBCRepository characterR2DBCRepository;
    private final UserRepository userRepository;
    private final StoryBoardR2DBCRepository storyBoardR2DBCRepository;
    private final CommentRepository commentRepository;

    /**
     * userId 값을 받고 storyBoard 생성 및 actor 생성
     * Actor은 나중에 재사용을 위한 반환
     * @param form
     * @param userId
     * @return Actor And StoryId 값을 가진 ActorAndStoryIdForm 을 반환
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<ActorAndStoryIdForm> generateStoryBoardAndCharacter(GenerateTemporaryForm form, Long userId) {
        return userR2DBCRepository.findByUserId(userId)
                .flatMap(user -> {
                    StoryBoard storyBoard = convertStoryBoard(form, userId);
                    return storyBoardR2DBCRepository.save(storyBoard)
                            .flatMap(savedStoryBoard -> {
                                List<Characters> characters = convertCharacter(form.getCharacters(), savedStoryBoard);
                                return Flux.fromIterable(characters)
                                        .flatMap(characterR2DBCRepository::save)
                                        .then(Mono.just(new ActorAndStoryIdForm(characters,savedStoryBoard.getId())));
                            });
                })
                .doOnSuccess(id -> log.info("Generated StoryBoard and Characters with user Id={}", id))
                .doOnError(e -> log.error("Error in generateStoryBoardAndCharacter", e));
    }

    @Transactional
    public void testForGetAllArchives() {
        User user = new User("hyunmin");
        userRepository.save(user);

        for (int i = 1; i <= 50; i++) {
            _2.ArtFusion.domain.storyboard.StoryBoard storyBoard = new _2.ArtFusion.domain.storyboard.StoryBoard("여기다가 광마회귀 스토리 프롬프트를 작성해서 넘겨주면 gpt openai를 통해서 내 프롬프트를 포멧시켜주겠죠??", "광마회귀" + i, Style.KOR_WEBTOON, "SIMPLE", "무협");
            StoryPost storyPost = new StoryPost("미친 사내가 미치기 전의 평범했던 시절로 돌아간다면. 사내는 다시 미치게 될 것인가? 아니면 사내의 적들이 미치게 될 것인가.광마 이자하, 점소이 시절로 회귀하다.", "무협,판타지", "이미지 url" + i, user, storyBoard);
            CaptureImage captureImage = new CaptureImage("captureImageUrl" + i, i, storyBoard);
            Comment comment = new Comment("미친", 1, user, storyPost);
            archiveRepository.save(storyPost);
            captureImageRepository.save(captureImage);
            commentRepository.save(comment);
        }
    }
}
