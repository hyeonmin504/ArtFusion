package _2.ArtFusion.service;

import _2.ArtFusion.domain.archive.Heart;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.jpa.ArchiveRepository;
import _2.ArtFusion.repository.jpa.HeartRepository;
import _2.ArtFusion.repository.jpa.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LikeService {
    private final ArchiveRepository archiveRepository;
    private final HeartRepository heartRepository;
    private final UserRepository userRepository;

    @Transactional
    // 주어진 postId를 사용하여 StoryPost를 검색
    // 게시글이 존재하지 않으면 NotFoundContentsException 선언
    public void isLikeStatus(Long postId,Long userId){
        StoryPost storyPost = archiveRepository.findById(postId).orElseThrow(
                () -> new NotFoundContentsException("해당 게시글이 존재하지 않습니다.")
        );

        // 주어진 userId를 사용하여 User를 검색
        // 사용자가 존재하지 않으면 NotFoundUserException 선언
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundUserException("해당 유저를 찾을 수 없습니다")
        );

        //로그 확인
        log.info("isLikePost={}", storyPost.getHeart());

        // 스토리 포스트의 좋아요 상태를 확인하고 업데이트
        if(storyPost.getHeart()==null){
            // 좋아요가 처음 눌린 경우, 새로운 IsLikePost 객체 생성 및 저장
            Heart heart = new Heart(true,1,user, storyPost);
            heartRepository.save(heart);
        } else {
            // 기존 좋아요 상태를 반전시키고, 좋아요 수를 업데이트
            if (!storyPost.getHeart().getIsLike()) {
                storyPost.getHeart().changeLike(true);
                storyPost.getHeart().changeLikeCnt(true);
            } else {
                storyPost.getHeart().changeLike(false);
                storyPost.getHeart().changeLikeCnt(false);
            }
        }
    }

}
