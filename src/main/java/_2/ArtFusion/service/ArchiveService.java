package _2.ArtFusion.service;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.archiveApiController.archiveform.ArchiveData;
import _2.ArtFusion.controller.archiveApiController.archiveform.ArchiveDataForm;
import _2.ArtFusion.controller.archiveApiController.archiveform.DetailArchiveDataForm;
import _2.ArtFusion.controller.userApiController.userController;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NoPermissionException;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.repository.jpa.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static _2.ArtFusion.controller.archiveApiController.ArchiveController.*;
import static _2.ArtFusion.controller.generateStoryApiController.TemporaryStoryController.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final StoryBoardRepository storyBoardRepository;
    private final SceneFormatService sceneFormatService;
    private final UserRepository userRepository;
    private final ArchiveService archiveService;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public AllArchivesResponse getArchiveList(Pageable pageable) {
        // Slice 객체로 Form 데이터를 가져옴
        Slice<ArchiveDataForm> archiveDataFormsSlice = archiveRepository.findAllArchiveForm(pageable);

        // 아카이브 데이터를 리스트로 변환
        List<ArchiveDataForm> archiveDataForms = archiveDataFormsSlice.getContent();

        List<ArchiveData> archiveData = new ArrayList<>();
        for (ArchiveDataForm archiveDataForm : archiveDataForms) {
            ArchiveData archiveDataSet = new ArchiveData(archiveDataForm.getPostId(), archiveDataForm.getCoverImg(), archiveDataForm.getTitle(), archiveDataForm.getSummary(), archiveDataForm.getNickname());
            archiveDataSet.addHashTags(archiveDataForm.getHashTag());
            archiveData.add(archiveDataSet);
        }

        // PostFormResponse 객체 생성 및 반환
        return AllArchivesResponse.builder()
                .archiveDataForms(archiveData)
                .offset(pageable.getOffset())
                .pageNum(archiveDataFormsSlice.getNumber())
                .numberOfElements(archiveDataFormsSlice.getNumberOfElements())
                .size(pageable.getPageSize())
                .isLast(archiveDataFormsSlice.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public AllArchivesResponse getArchiveListForUser(Pageable pageable, String nickname) {
        // Slice 객체로 Form 데이터를 가져옴
        Slice<ArchiveDataForm> archiveDataFormsSlice = archiveRepository.findAllArchiveFormForNickname(pageable, nickname);

        // 아카이브 데이터를 리스트로 변환
        List<ArchiveDataForm> archiveDataForms = archiveDataFormsSlice.getContent();

        List<ArchiveData> archiveData = new ArrayList<>();
        for (ArchiveDataForm archiveDataForm : archiveDataForms) {
            ArchiveData archiveDataSet = new ArchiveData(archiveDataForm.getPostId(), archiveDataForm.getCoverImg(), archiveDataForm.getTitle(), archiveDataForm.getSummary(), archiveDataForm.getNickname());
            archiveDataSet.addHashTags(archiveDataForm.getHashTag());
            archiveData.add(archiveDataSet);
        }

        // PostFormResponse 객체 생성 및 반환
        return AllArchivesResponse.builder()
                .archiveDataForms(archiveData)
                .offset(pageable.getOffset())
                .pageNum(archiveDataFormsSlice.getNumber())
                .numberOfElements(archiveDataFormsSlice.getNumberOfElements())
                .size(pageable.getPageSize())
                .isLast(archiveDataFormsSlice.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public DetailArchivesResponse getArchive(Long postId,String nickname) {
        User userData = userRepository.findByNickname(nickname);

        log.info("findDetailArchiveForm");
        //해당 아카이브를 찾아오기
        DetailArchiveDataForm detailArchiveDataForm = archiveRepository.findDetailArchiveForm(postId).orElseThrow(
                () -> new NotFoundContentsException("해당 아카이브를 찾을 수 없습니다"));

        List<String> hashTags = Arrays.asList(detailArchiveDataForm.getHashTag().split(","));

//        log.info("findStoryImagesByStoryId");
//        //시퀀스에따라 이미지를 불러옴
//        List<String> urls = storyImageRepository.findStoryImagesByStoryId(detailArchiveDataForm.getStoryId());

        log.info("getSceneFormatData");
        //모든 장면의 데이터도 불러옴
        StoryBoard storyBoard = sceneFormatService.getSceneFormatData(userData.getId(), detailArchiveDataForm.getStoryId());

        /**
         * 해당 작품을 폼으로 변환 하는 로직
         */
        List<SceneFormatForm> sceneFormatForms = new ArrayList<>();

        for (SceneFormat format : storyBoard.getSceneFormats()) {
            log.info("sceneFormat 생성");
            SceneFormatForm sceneFormatForm = new SceneFormatForm(format.getId(),format.getSceneImage().getId(),
                    format.getSceneSequence(),format.getSceneImage().getUrl(),format.getBackground(),format.getDescription(),format.getDialogue());
            sceneFormatForms.add(sceneFormatForm);
        }

        return DetailArchivesResponse.builder()
                .storyId(detailArchiveDataForm.getStoryId())
                .title(storyBoard.getTitle())
                .nickName(detailArchiveDataForm.getNickName())
                .createDate(detailArchiveDataForm.getCreateDate())
                .hashTag(hashTags)
                .sceneImage(sceneFormatForms)
                .build();
    }

    public StoryPost getStoryPostById(Long postId) {
        return archiveRepository.findById(postId).orElseThrow(
                () -> new NotFoundContentsException("해당 게시글을 찾을 수 없습니다.")
        );
    }

    public StoryPost getStoryPostByStoryId(Long storyId) {
        return archiveRepository.findById(storyId).orElseThrow(
                () -> new NotFoundContentsException("해당 스토리보드를 찾을 수 없습니다.")
        );
    }
    @Transactional
    public void deleteArchive(Long postId, User userData) {
        StoryPost storyPost = archiveService.getStoryPostById(postId);
        //게시글 작성자와 요청한 사용자가 동일한지 확인
        if(!storyPost.getUser().getId().equals(userData.getId())){
            throw new NoPermissionException("권한이 없습니다");
        }

        // 아카이브 삭제
        storyBoardRepository.deleteById(storyPost.getStoryBoard().getId());
        archiveRepository.deleteById(postId);
    }

    @Transactional
    public void registerStoryPostBefore(Long storyId, User userData) throws IOException {
        StoryBoard storyBoard = sceneFormatService.getSceneFormatData(userData.getId(),storyId);

        log.info("storyBoard={}",storyBoard);

        //이미지 저장
        log.info("uploadImage");
        StoryBoard savedStoryBoard = imageService.uploadImage(storyBoard);

        //post 생성
        archiveService.registerStoryPost(savedStoryBoard,userData);
    }
    @Transactional
    public void registerStoryPost(StoryBoard storyBoard, User user) {
        //프롬프트의 앞 부분 30자만 추출
        String shortenedPrompt = storyBoard.getPromptKor().length() > 60 ? storyBoard.getPromptKor().substring(0, 60) : storyBoard.getPromptKor();

        //첫 째 장면 이미지 추출
        String url = storyBoard.getSceneFormats().get(0).getSceneImage().getUrl();

        StoryPost post = archiveRepository.findByStoryBoard(storyBoard);
        if (post == null){
            StoryPost storyPost = new StoryPost(shortenedPrompt,storyBoard.getGenre(),url, user, storyBoard);
            archiveRepository.save(storyPost);
        } else post.updatePost(shortenedPrompt,storyBoard.getGenre(),url, user, storyBoard);
    }

    @Transactional
    public void deleteStoryBoard(Long storyId) throws NotFoundContentsException {
        StoryBoard storyBoard = storyBoardRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundContentsException("스토리보드를 찾을 수 없습니다."));

        StoryPost storyPost = storyBoard.getStoryPost();
        if (storyPost != null) {
            storyBoard.setStoryPost(null);
        }

        storyBoardRepository.delete(storyBoard);
    }
}