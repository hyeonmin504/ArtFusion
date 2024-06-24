package _2.ArtFusion.service;

import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.storyboard.CaptureImage;
import _2.ArtFusion.domain.storyboard.GenerateType;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.storyboard.Style;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.repository.ArchiveRepository;
import _2.ArtFusion.repository.CaptureImageRepository;
import _2.ArtFusion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;

@Service
@RequiredArgsConstructor
public class StoryBoardService {

    private final ArchiveRepository archiveRepository;
    private final CaptureImageRepository captureImageRepository;
    private final UserRepository userRepository;

    @Transactional
    public void testForGetAllArchives() {
        User user = new User("hyunmin");
        userRepository.save(user);


        for (int i = 1; i <=50; i++) {
            StoryBoard storyBoard = new StoryBoard("여기다가 광마회귀 스토리 프롬프트를 작성해서 넘겨주면 gpt openai를 통해서 내 프롬프트를 포멧시켜주겠죠??","광마회귀" + i, Style.KOR_WEBTOON, GenerateType.SIMPLE,"무협");
            StoryPost storyPost = new StoryPost("미친 사내가 미치기 전의 평범했던 시절로 돌아간다면. 사내는 다시 미치게 될 것인가? 아니면 사내의 적들이 미치게 될 것인가.광마 이자하, 점소이 시절로 회귀하다.","무협,판타지","이미지 url"+i, user, storyBoard);
            CaptureImage captureImage = new CaptureImage("captureImageUrl"+i, i, storyBoard);
            archiveRepository.save(storyPost);
            captureImageRepository.save(captureImage);
        }


    }
}
