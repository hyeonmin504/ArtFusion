package _2.ArtFusion.repository;

import _2.ArtFusion.domain.scene.TemporaryPhotoStorage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporaryPhotoRepository extends JpaRepository<TemporaryPhotoStorage,Long> {
}
