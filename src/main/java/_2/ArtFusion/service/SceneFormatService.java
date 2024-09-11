package _2.ArtFusion.service;

import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.scene.SceneImage;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.jpa.SceneFormatRepository;
import _2.ArtFusion.repository.jpa.SceneImageRepository;
import _2.ArtFusion.repository.jpa.StoryBoardRepository;
import _2.ArtFusion.repository.jpa.UserRepository;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SceneFormatService {

    private final UserRepository userRepository;
    private final StoryBoardRepository storyBoardRepository;
    private final SceneFormatRepository sceneFormatRepository;
    private final SceneImageRepository sceneImageRepository;

    @Transactional(readOnly = true)
    public StoryBoard getSceneFormatData(Long userId, Long storyId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundUserException("유저를 찾을 수 없습니다"));

        try {
            StoryBoard storyBoard = storyBoardRepository.findStoryBoardByUser(user, storyId).orElseThrow(
                    () -> new NotFoundContentsException("해당 유저의 스토리보드를 찾을 수 없습니다")
            );

            if (storyBoard != null) {
                //장면 순서대로 검색
                List<SceneFormat> scenes = sceneFormatRepository.findScenesByStoryBoard(storyBoard);

                if (!scenes.isEmpty()) {
                    //장면에 해당하는 image 검색
                    for (SceneFormat scene : scenes) {
                        SceneImage sceneImage = sceneImageRepository.findBySceneFormat(scene).orElseGet(
                                () -> new SceneImage("기본 이미지 url", scene)
                        );
                        //연결
                        sceneImage.setSceneFormat(scene);
                    }
                    storyBoard.setSceneFormats(scenes);
                }
            }

            return storyBoard;
        } catch (NoResultException e) {
            throw new NotFoundContentsException("해당 컨텐츠를 찾을 수 없습니다");
        }
    }
}
