package _2.ArtFusion.service;

import _2.ArtFusion.controller.generateStoryApiController.storyForm.ActorAndStoryIdForm;
import _2.ArtFusion.controller.generateStoryApiController.storyForm.GenerateTemporaryForm;
import _2.ArtFusion.domain.r2dbcVersion.Actor;
import _2.ArtFusion.domain.r2dbcVersion.StoryBoard;
import _2.ArtFusion.repository.r2dbc.ActorR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.StoryBoardR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.UserR2DBCRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static _2.ArtFusion.service.util.convertUtil.ConvertUtil.convertCharacter;
import static _2.ArtFusion.service.util.convertUtil.ConvertUtil.convertStoryBoard;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoryBoardService {

    private final UserR2DBCRepository userR2DBCRepository;
    private final ActorR2DBCRepository actorR2DBCRepository;
    private final StoryBoardR2DBCRepository storyBoardR2DBCRepository;

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
                                List<Actor> characters = convertCharacter(form.getCharacters(), savedStoryBoard);
                                return Flux.fromIterable(characters)
                                        .flatMap(actorR2DBCRepository::save)
                                        .then(Mono.just(new ActorAndStoryIdForm(characters, savedStoryBoard.getId())));
                            });
                })
                .doOnSuccess(id -> log.info("Generated StoryBoard and Characters with user Id={}", id))
                .doOnError(e -> log.error("Error in generateStoryBoardAndCharacter", e));
    }
}