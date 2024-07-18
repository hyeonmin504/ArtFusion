package _2.ArtFusion.service.webClientService;

import _2.ArtFusion.controller.editStoryApiController.editForm.ContentEditForm;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.repository.r2dbc.CharacterR2DBCRepository;
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
    private final CharacterR2DBCRepository characterR2DBCRepository;
    private final SceneFormatWebClientService sceneFormatWebClientService;

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
    public Mono<SceneFormat> contentEdit(Mono<ContentEditForm> form, Mono<Long> sceneId) {
        return sceneId
                .flatMap(id -> sceneFormatR2DBCRepository.findById(id)
                        .flatMap(sceneFormat -> characterR2DBCRepository.findByStoryId(Mono.just(sceneFormat.getStoryId()))
                                .collectList()
                                .flatMap(characters -> form.flatMap(contentEditForm -> {
                                    if (isContentUnchanged(sceneFormat, contentEditForm)) {

                                        SceneFormat newScene = sceneFormat.editContentForm(contentEditForm.getDescription(), contentEditForm.getBackground(), contentEditForm.getDialogue());
                                        return sceneFormatR2DBCRepository.save(newScene);
                                    } else {
                                        SceneFormat newScene = sceneFormat.editContentForm(contentEditForm.getDescription(), contentEditForm.getBackground(), contentEditForm.getDialogue());
                                        String actorsPrompt = sceneFormatWebClientService.findMatchingActors(newScene.getActors(), characters);
                                        return sceneFormatR2DBCRepository.findStyleById(Mono.just(newScene.getStoryId()))
                                                .flatMap(style -> sceneFormatWebClientService.generateSceneFormatForDallE(newScene, actorsPrompt, style))
                                                .flatMap(sceneFormatR2DBCRepository::save);
                                    }
                                }))
                        ))
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
}
