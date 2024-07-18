package _2.ArtFusion.service;

import _2.ArtFusion.controller.editStorycontroller.editForm.ContentEditForm;
import _2.ArtFusion.controller.editStorycontroller.editForm.DetailEditForm;
import _2.ArtFusion.controller.editStorycontroller.editForm.SceneSeqForm;
import _2.ArtFusion.controller.editStorycontroller.editForm.SequenceForm;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.domain.scene.TemporaryPhotoStorage;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.repository.jpa.SceneFormatRepository;
import _2.ArtFusion.repository.jpa.TemporaryPhotoRepository;
import _2.ArtFusion.repository.r2dbc.CharacterR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.SceneFormatR2DBCRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SceneEditService {
    
    private final SceneFormatRepository sceneFormatRepository;
    private final OpenAiGPTService openAiService;
    private final TemporaryPhotoRepository temporaryPhotoRepository;

    private final SceneFormatR2DBCRepository sceneFormatR2DBCRepository;
    private final CharacterR2DBCRepository characterR2DBCRepository;

    private final SceneFormatService sceneFormatService;

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
                                String actorsPrompt = sceneFormatService.findMatchingActors(newScene.getActors(), characters);
                                return sceneFormatR2DBCRepository.findStyleById(Mono.just(newScene.getStoryId()))
                                        .flatMap(style -> sceneFormatService.generateSceneFormatForDallE(newScene, actorsPrompt, style))
                                        .flatMap(sceneFormatR2DBCRepository::save);
                            }
                        }))
                ))
            .onErrorResume(e -> {
                log.error("Error editing content", e);
                return Mono.error(new NotFoundContentsException(e.getMessage()));
            });
    }

    @Transactional
    public void randomEdit(Long sceneId) {
        _2.ArtFusion.domain.scene.SceneFormat scene = sceneFormatRepository.findById(sceneId).orElseThrow(
                () -> new NotFoundContentsException("해당 장면을 찾을 수 없습니다")
        );
        //장면 생성및 저장
        openAiService.generateImage(scene);
    }

    @Transactional
    public void detailEdit(DetailEditForm form,Long sceneId) {
        _2.ArtFusion.domain.scene.SceneFormat scene = sceneFormatRepository.findById(sceneId).orElseThrow(
                () -> new NotFoundContentsException("해당 장면을 찾을 수 없습니다")
        );

        TemporaryPhotoStorage storage = temporaryPhotoRepository.findById(form.getImageId()).orElseThrow(
                () -> new NotFoundContentsException("해당 이미지를 찾을 수 없습니다")
        );

        //해당 Url을 이미지 데이터로 바꾼 후 데이터로서 달리에 요청


        //sceneData를 통해 이미지 변환 요청
        openAiService.variationImage(form.getSceneModifyPrompt());

        //저장

    }

    @Transactional
    public void sequenceEdit(SceneSeqForm form) {
        List<SequenceForm> sequenceForms = form.getScene();

        for (SequenceForm sequenceForm : sequenceForms) {
            _2.ArtFusion.domain.scene.SceneFormat sceneFormat = sceneFormatRepository.findById(sequenceForm.getSceneId()).orElseThrow(
                    () -> new NotFoundContentsException(sequenceForm.getNewSceneSeq() + "번 째 장면을 찾을 수 없습니다")
            );

            //저장된 장면의 순서가 요청받은 장면의 순서가 다를 경우만 변경감지 저장
            if (sequenceForm.getNewSceneSeq() != sceneFormat.getSceneSequence()) {
                sceneFormat.changeSequence(sequenceForm.getNewSceneSeq());
            }
        }
    }

    private static boolean isContentUnchanged(SceneFormat sceneFormat, ContentEditForm contentEditForm) {
        return sceneFormat.getBackground().equals(contentEditForm.getBackground().trim()) &&
                sceneFormat.getDescription().equals(contentEditForm.getDescription().trim()) &&
                !sceneFormat.getDialogue().isEmpty();
    }
}