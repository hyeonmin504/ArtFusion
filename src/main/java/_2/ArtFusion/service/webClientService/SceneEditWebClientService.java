package _2.ArtFusion.service.webClientService;

import _2.ArtFusion.controller.editStoryApiController.editForm.ContentEditForm;
import _2.ArtFusion.controller.generateStoryApiController.FailApiResponseForm;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.repository.r2dbc.ActorR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.SceneFormatR2DBCRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class SceneEditWebClientService {

    private final SceneFormatR2DBCRepository sceneFormatR2DBCRepository;
    private final ActorR2DBCRepository actorR2DBCRepository;
    private final SceneFormatWebClientService sceneFormatWebClientService;
    private final DallEConnectionWebClientService dallEConnectionWebClientService;

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
                        //로직에 따른 내용 수정이 크게 일어나지 않은 경우
                        if (isContentUnchanged(sceneFormat, contentEditForm)) {
                            //내용만 수정
                            SceneFormat newScene = sceneFormat.editContentForm(contentEditForm.getDescription(), contentEditForm.getBackground(), contentEditForm.getDialogue());
                            return sceneFormatR2DBCRepository.save(newScene)
                                    //-1L 값을 반환
                                    .then(Mono.just(-1L));
                        } else {//로직에 따른 내용 수정이 크게 일어난 경우
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

    private static boolean isContentUnchanged(SceneFormat sceneFormat, ContentEditForm contentEditForm) {
        return sceneFormat.getBackground().equals(contentEditForm.getBackground().trim()) &&
                sceneFormat.getDescription().equals(contentEditForm.getDescription().trim()) &&
                !sceneFormat.getDialogue().isEmpty();
    }

    public Mono<FailApiResponseForm> singleTransImage(Long sceneId) {
        return sceneFormatR2DBCRepository.findById(sceneId)
                .flatMap(sceneFormat -> dallEConnectionWebClientService.transImageForDallE(Mono.just(sceneFormat)))
                .switchIfEmpty(Mono.empty());
    }
}
