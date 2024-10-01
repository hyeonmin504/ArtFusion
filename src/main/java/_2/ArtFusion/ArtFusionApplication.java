package _2.ArtFusion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;

import jakarta.servlet.MultipartConfigElement;

//@EntityScan(basePackages = {"_2.ArtFusion.config.jwt"})  // Token 클래스가 있는 패키지를 명시
@SpringBootApplication
public class ArtFusionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArtFusionApplication.class, args);
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.ofMegabytes(10));
		factory.setMaxRequestSize(DataSize.ofMegabytes(10));
		return factory.createMultipartConfig();
	}
}