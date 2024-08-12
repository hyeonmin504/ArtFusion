package _2.ArtFusion.service;

import _2.ArtFusion.controller.editStoryApiController.editForm.SceneSeqForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.SequenceForm;
import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.repository.jpa.SceneFormatRepository;
import _2.ArtFusion.repository.jpa.SceneImageRepository;
import _2.ArtFusion.service.webClientService.OpenAiGPTWebClientService;
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

    @Transactional
    public void sequenceEdit(SceneSeqForm form) {
        List<SequenceForm> sequenceForms = form.getScene();

        for (SequenceForm sequenceForm : sequenceForms) {
            SceneFormat sceneFormat = sceneFormatRepository.findById(sequenceForm.getSceneId()).orElseThrow(
                    () -> new NotFoundContentsException(sequenceForm.getNewSceneSeq() + "번 째 장면을 찾을 수 없습니다")
            );

            //저장된 장면의 순서가 요청받은 장면의 순서가 다를 경우만 변경감지 저장
            if (sequenceForm.getNewSceneSeq() != sceneFormat.getSceneSequence()) {
                log.info("sequenceForm.getNewSceneSeq()={} != sceneFormat.getSceneSequence()={}",sequenceForm.getNewSceneSeq(),sceneFormat.getSceneSequence());
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