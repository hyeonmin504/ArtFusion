package _2.ArtFusion.service;

import _2.ArtFusion.domain.storyboard.CaptureImage;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.repository.CaptureImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Client s3Client;
    private final CaptureImageRepository captureImageRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Transactional
    public void uploadImage(MultipartFile image, StoryBoard storyBoard) throws IOException {
        //마지막 이미지 번호
        int maxSequence = captureImageRepository.findMaxSequenceByStoryBoard(storyBoard);
        int newSequence = maxSequence + 1;

        File file = convertMultiPartFileToFile(image);
        String fileName = image.getOriginalFilename() + "_" + newSequence;
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        //저장
        s3Client.putObject(putObjectRequest, file.toPath());

        //file.delete(): 로컬에 임시로 생성된 파일을 삭제.
        file.delete();

        //S3에서 URL 가져와서 저장하기
        String imageUrl = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toExternalForm();
        CaptureImage captureImage = new CaptureImage(imageUrl,newSequence,storyBoard);
        captureImageRepository.save(captureImage);
    }

    private File convertMultiPartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}
