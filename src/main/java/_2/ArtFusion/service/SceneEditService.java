package _2.ArtFusion.service;

import _2.ArtFusion.controller.editStoryApiController.editForm.DetailEditForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.SceneSeqForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.SequenceForm;
import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.scene.TemporaryPhotoStorage;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.repository.jpa.SceneFormatRepository;
import _2.ArtFusion.repository.jpa.TemporaryPhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SceneEditService {
    
    private final SceneFormatRepository sceneFormatRepository;
    private final OpenAiGPTService openAiService;
    private final TemporaryPhotoRepository temporaryPhotoRepository;

    @Transactional
    public void randomEdit(Long sceneId) {
        SceneFormat scene = sceneFormatRepository.findById(sceneId).orElseThrow(
                () -> new NotFoundContentsException("해당 장면을 찾을 수 없습니다")
        );
        //장면 생성및 저장
        openAiService.generateImage(scene);
    }

    @Transactional
    public void detailEdit(DetailEditForm form,Long sceneId) {
        SceneFormat scene = sceneFormatRepository.findById(sceneId).orElseThrow(
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
            SceneFormat sceneFormat = sceneFormatRepository.findById(sequenceForm.getSceneId()).orElseThrow(
                    () -> new NotFoundContentsException(sequenceForm.getNewSceneSeq() + "번 째 장면을 찾을 수 없습니다")
            );

            //저장된 장면의 순서가 요청받은 장면의 순서가 다를 경우만 변경감지 저장
            if (sequenceForm.getNewSceneSeq() != sceneFormat.getSceneSequence()) {
                sceneFormat.changeSequence(sequenceForm.getNewSceneSeq());
            }
        }
    }

    @Transactional
    public void deleteScene(Long sceneId) {
        SceneFormat sceneFormat = sceneFormatRepository.findById(sceneId).orElseThrow(
                () -> new NotFoundContentsException("해당 장면을 찾을 수 없습니다")
        );

        sceneFormatRepository.delete(sceneFormat);
    }
}