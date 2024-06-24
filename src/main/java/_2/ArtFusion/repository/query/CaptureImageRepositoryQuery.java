package _2.ArtFusion.repository.query;

import _2.ArtFusion.controller.archiveApiController.ArchiveController;
import _2.ArtFusion.domain.storyboard.CaptureImage;

import java.util.List;

import static _2.ArtFusion.controller.archiveApiController.ArchiveController.*;

public interface CaptureImageRepositoryQuery {
    List<String> findCaptureImagesByStoryId(Long storyId);
}
