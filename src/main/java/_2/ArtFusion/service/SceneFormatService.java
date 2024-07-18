package _2.ArtFusion.service;

import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.jpa.SceneFormatRepository;
import _2.ArtFusion.repository.jpa.StoryBoardRepository;
import _2.ArtFusion.repository.jpa.UserRepository;
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

    @Transactional(readOnly = true)
    public StoryBoard getSceneFormatData(Long userId, Long storyId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundUserException("유저를 찾을 수 없습니다"));

        boolean existStory = storyBoardRepository.findStoryBoardByUser(user, storyId);

        if (!existStory) {
            throw new NotFoundContentsException("해당 유저의 스토리보드를 찾을 수 없습니다");
        }
        StoryBoard storyBoard = storyBoardRepository.findById(storyId).orElseThrow();

        List<SceneFormat> scenes = sceneFormatRepository.findScenesByStoryBoard(storyBoard);
        for (SceneFormat scene : scenes) {
            log.info("scene.getId()={}", scene.getId());
        }

        storyBoard.setSceneFormats(scenes);

        return storyBoard;
    }
}
