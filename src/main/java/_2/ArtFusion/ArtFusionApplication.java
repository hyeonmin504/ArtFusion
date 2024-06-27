package _2.ArtFusion;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;



@SpringBootApplication
public class ArtFusionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArtFusionApplication.class, args);
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.ofMegabytes(10)); // 최대 파일 크기 설정
		factory.setMaxRequestSize(DataSize.ofMegabytes(10)); // 최대 요청 크기 설정
		return factory.createMultipartConfig();
	}

}
