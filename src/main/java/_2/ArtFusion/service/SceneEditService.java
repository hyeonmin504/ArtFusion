package _2.ArtFusion.service;

import _2.ArtFusion.controller.editStorycontroller.editForm.ContentEditForm;
import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.repository.SceneFormatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SceneEditService {
    
    private final SceneFormatRepository sceneFormatRepository;
    private final OpenAiService openAiService;
    private final SceneFormatService sceneFormatService;
    
    @Transactional
    public void contentEdit(ContentEditForm form) {
        SceneFormat scene = sceneFormatRepository.findById(form.getSceneId()).orElseThrow(
                () -> new NotFoundContentsException("해당 장면을 찾을 수 없습니다")
        );

        /**
         * if문이 true인 경우
         * 변경 o -> 대사 + 변경 전에도 대사가 있었던 경우
         *              -> 만약 변경 전에 대사가 없었다면 장면 변화가 생김
         * 변경 x -> 배경, 내용
         */
        if (scene.getBackground().equals(form.getBackground().trim()) && scene.getDescription().equals(form.getDescription().trim()) && !scene.getDialogue().isEmpty()) {
            scene.editContentForm(form.getDescription(), form.getBackground(), form.getDialogue());
            return;
        }

        //변경감지
        scene.editContentForm(form.getDescription(), form.getBackground(), form.getDialogue());
        //promptEn 재 생성 로직
        SceneFormat sceneFormat = sceneFormatService.combineToPromptAndTransEnglish(scene);
        //장면 생성및 저장
        openAiService.generateImage(sceneFormat);
    }

    @Transactional
    public void randomEdit(Long sceneId) {
        SceneFormat scene = sceneFormatRepository.findById(sceneId).orElseThrow(
                () -> new NotFoundContentsException("해당 장면을 찾을 수 없습니다")
        );
        //장면 생성및 저장
        openAiService.generateImage(scene);
    }
}
