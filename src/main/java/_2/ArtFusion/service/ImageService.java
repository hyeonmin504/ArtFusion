package _2.ArtFusion.service;

import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.storyboard.StoryImage;
import _2.ArtFusion.exception.ConvertException;
import _2.ArtFusion.repository.jpa.SceneFormatRepository;
import _2.ArtFusion.repository.jpa.StoryImageRepository;
import _2.ArtFusion.service.util.convertUtil.ImageUrlConvertToPng;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final S3Client s3Client;
    private final StoryImageRepository storyImageRepository;
    private final SceneFormatRepository sceneFormatRepository;
    private final ImageUrlConvertToPng imageUrlConvertToPng;

    // 파일을 저장할 경로를 지정합니다.
    @Value("${file.upload.dir}")
    private String uploadDir;
    @Value("${aws.s3.bucket}")
    private String bucketName;

    //이미지 업로드
    @Transactional
    public StoryBoard uploadImage(StoryBoard storyBoard) throws IOException {

        //장면 저장
        saveScenes(storyBoard);

        return storyBoard;

        //마지막 이미지 번호
//        int maxSequence = storyImageRepository.findMaxSequenceByStoryBoard(storyBoard);
//        int newSequence = maxSequence + 1;

        //전체 캡쳐본 임시 비활성화
//        File file = convertMultiPartFileToFile(image);
//        String fileName = image.getOriginalFilename() + "_" + newSequence;
//        //s3 저장
//        s3Saver(fileName, file);

        //db에 이미지 s3 url 저장
        //return saveImage(storyBoard, fileName, newSequence);
    }

    //해당 스토리보드의 장면을 저장
    @Transactional
    protected void saveScenes(StoryBoard storyBoard) {
        log.info("saveScenes");
        List<SceneFormat> scenes = sceneFormatRepository.findScenesByStoryBoard(storyBoard);

        scenes.forEach(sceneFormat ->  {
            log.info(sceneFormat.getSceneSequence() + "번 째 장면을 이미지로 변환중");
            String url = sceneFormat.getSceneImage().getUrl();

            ByteArrayResource byteArrayResource = imageUrlConvertToPng.downloadImageAndConvertToPng(url);

            try {
                log.info(sceneFormat.getSceneSequence() + "번 째 장면을 s3에 저장중");
                String fileName = LocalDateTime.now().toString() + sceneFormat.getSceneSequence();
                File file = convertByteArrayResourceToFile(byteArrayResource, fileName);

                //s3 저장
                s3Saver(file.getName(),file);

                //scene_image S3에서 URL 가져와서 업데이트
                String imageUrl = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toExternalForm();
                log.info("imageUrl={}",imageUrl + ".png");
                sceneFormat.getSceneImage().updateUrl(imageUrl + ".png");
                file.deleteOnExit();
            } catch (IOException e) {
                log.error("error",e);
                throw new ConvertException("이미지 파일 변환 중 오류 발생");
            }
        });
    }

    //전체 캡쳐본 이미지 저장 로직
    protected StoryBoard saveImage(StoryBoard storyBoard,String fileName, int newSequence) {
        //S3에서 URL 가져와서 저장하기
        String imageUrl = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toExternalForm();
        log.info("imageUrl={}",imageUrl);

        StoryImage storyImage = storyImageRepository.findByStoryBoard(storyBoard);

        if (storyImage == null) {
            StoryImage image = new StoryImage(imageUrl, newSequence, storyBoard);
            storyImageRepository.save(image);
        } else storyImage.updateImage(imageUrl, newSequence, storyBoard);
        return storyBoard;
    }

    //s3에 저장하는 로직
    protected void s3Saver(String fileName, File file) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        //저장
        s3Client.putObject(putObjectRequest, file.toPath());

        //file.delete();
        log.info("저장 완료");
    }

    //캡쳐본 이미지 파일에 임시 저장및 png 변환
    private File convertMultiPartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(uploadDir + Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    //장면 이미지 png 변환 및 저장
    public File convertByteArrayResourceToFile(ByteArrayResource byteArrayResource, String fileName) throws IOException {
        // 해당 경로에 파일 생성
        File file = new File(uploadDir + fileName + ".png");
        //File file = File.createTempFile(fileName, ".png");

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            // ByteArrayResource의 데이터를 파일에 쓰기
            outputStream.write(byteArrayResource.getByteArray());
        } catch (IOException e) {
            log.error("error",e);
            throw new ConnectException("이미지 변환중 오류 발생");
        }
        return file;
    }
}
