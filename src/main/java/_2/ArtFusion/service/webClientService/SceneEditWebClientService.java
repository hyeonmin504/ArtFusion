package _2.ArtFusion.service.webClientService;

import _2.ArtFusion.controller.editStoryApiController.editForm.ContentEditForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.DetailEditForm;
import _2.ArtFusion.controller.generateStoryApiController.storyForm.ResultApiResponseForm;
import _2.ArtFusion.domain.r2dbcVersion.Actor;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.repository.r2dbc.ActorR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.SceneFormatR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.SceneImageR2DBCRepository;
import _2.ArtFusion.service.processor.DallE2QueueProcessor;
import _2.ArtFusion.service.processor.DallE3QueueProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SceneEditWebClientService {

    private final SceneFormatR2DBCRepository sceneFormatR2DBCRepository;
    private final ActorR2DBCRepository actorR2DBCRepository;
    private final SceneFormatWebClientService sceneFormatWebClientService;
    private final DallE3QueueProcessor dallE3QueueProcessor;
    private final DallE2QueueProcessor dallE2QueueProcessor;
    private final SceneImageR2DBCRepository sceneImageR2DBCRepository;

    /**
     * if문에서
     * 변경 o -> 대사 + 변경 전에도 대사가 있었던 경우
     * -> 만약 변경 전에 대사가 없었다면 장면 변화가 생김
     * 변경 x -> 배경, 내용
     * 위 내용에 부합한 경우 true를 반환
     * => content만 업데이트
     * <p>
     * false인 경우
     * => 이미지 재요청
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<Long> contentEdit(Mono<ContentEditForm> form, Mono<Long> sceneId) {
        //sceneFormat 조회
        return sceneId.flatMap(id -> sceneFormatR2DBCRepository.findById(id)
            //actor 조회
            .flatMap(sceneFormat -> actorR2DBCRepository.findByStoryId(Mono.just(sceneFormat.getStoryId()))
                    .collectList()
                    .flatMap(actors -> form.flatMap(contentEditForm -> {
                        log.info("content edit");
                        if (isContentUnchanged(sceneFormat, contentEditForm)) {
                            //로직에 따른 내용 수정이 거의 없는 경우 - updateContent
                            SceneFormat newScene = sceneFormat.editContentForm(contentEditForm.getDescription(), contentEditForm.getBackground(), contentEditForm.getDialogue());
                            return sceneFormatR2DBCRepository.save(newScene)
                                    //-1L 값을 반환
                                    .then(Mono.just(-1L));
                        } else {
                            //로직에 따른 내용 수정이 크게 일어난 경우
                            return updateContentAndImage(sceneId, sceneFormat, actors, contentEditForm);
                        }
                    }))
            )
        )
        //장면을 찾을 수 없는 경우 빈 값을 반환
        .switchIfEmpty(Mono.empty())
        .onErrorResume(e -> {
        log.error("Error editing content", e);
        return Mono.error(new NotFoundContentsException(e.getMessage()));
        });
    }

    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<ResultApiResponseForm> detailEdit(DetailEditForm form, Long sceneId) {
        return sceneFormatR2DBCRepository.findById(sceneId)
                .switchIfEmpty(Mono.error(new NotFoundContentsException("해당 장면을 찾을 수 없습니다" + sceneId)))
                .flatMap(sceneFormat ->
                        sceneImageR2DBCRepository.findById(form.getImageId())
                                .flatMap(sceneImage ->
                                        dallE2QueueProcessor.updateImageForDallE(sceneImage, Mono.just(sceneFormat))
                                )
                                .switchIfEmpty(Mono.error(new NotFoundContentsException("해당 이미지를 찾을 수 없습니다" + form.getImageId())))
                )
                .onErrorResume(e -> {
                    log.error("Error editing detail", e);
                    return Mono.error(new NotFoundContentsException(e.getMessage()));
                });
    }

    /**
     * 이미지, 내용 수정
     * @param sceneId
     * @param sceneFormat
     * @param actors -> 장면에 등장하는 actor
     * @param contentEditForm -> 변경하려는 내용
     * @return
     */
    @NotNull
    private Mono<Long> updateContentAndImage(Mono<Long> sceneId, SceneFormat sceneFormat, List<Actor> actors, ContentEditForm contentEditForm) {
        log.info("content, image edit");
        //내용 수정
        SceneFormat newScene = sceneFormat.editContentForm(contentEditForm.getDescription(), contentEditForm.getBackground(), contentEditForm.getDialogue());
        //각 내용에 등장하는 actor 정보를 삽입
        String actorsPrompt = sceneFormatWebClientService.findMatchingActors(newScene.getActors(), actors);
        //프롬프트 수정
        return sceneFormatR2DBCRepository.findStyleById(Mono.just(newScene.getStoryId()))
                .flatMap(style -> sceneFormatWebClientService.generateSceneFormatForDallE(newScene, actorsPrompt, style))
                .flatMap(sceneFormatR2DBCRepository::save)
                //이미지를 변환할 장면 id 반환
                .then(sceneId);
    }

    /**
     * 이미지 변경 정도
     * @param sceneFormat -> 기존의 장면
     * @param contentEditForm -> 새로운 장면
     * @return 비교 후 결과 값 반환 true = 이미지, 내용 수정 / false = 내용 수정
     */
    private static boolean isContentUnchanged(SceneFormat sceneFormat, ContentEditForm contentEditForm) {
        return sceneFormat.getBackground().equals(contentEditForm.getBackground().trim()) &&
                sceneFormat.getDescription().equals(contentEditForm.getDescription().trim()) &&
                !sceneFormat.getDialogue().isEmpty();
    }

    /**
     * 수정api를 위한 단일 이미지 수정 요청
     * @param sceneId -> 장면 id 값
     * @return FailApiResponseForm -> 성공 여부
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<ResultApiResponseForm> singleTransImage(Long sceneId, User user) {
        return sceneFormatR2DBCRepository.findById(sceneId)
                .flatMap(sceneFormat -> dallE3QueueProcessor.transImageForDallE(Mono.just(sceneFormat),user))
                .switchIfEmpty(Mono.empty());
    }
}
