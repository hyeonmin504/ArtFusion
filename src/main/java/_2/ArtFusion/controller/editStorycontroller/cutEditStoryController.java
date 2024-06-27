package _2.ArtFusion.controller.editStorycontroller;

import _2.ArtFusion.controller.ResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class cutEditStoryController {

    @PutMapping("/cuts/{cutId}/refresh")
    public ResponseForm imageRandomEdit() {
        return new ResponseForm<>(HttpStatus.OK, null, "Ok");
    }
    @PutMapping("/cuts/contents")
    public ResponseForm imageContentsEdit() {
        return new ResponseForm<>(HttpStatus.OK, null, "Ok");
    }
    @PutMapping("/cuts/detail")
    public ResponseForm imageDetailEdit() {
        return new ResponseForm<>(HttpStatus.OK, null, "Ok");
    }
    @PutMapping("/cuts/sequence")
    public ResponseForm imageSequenceEdit() {
        return new ResponseForm<>(HttpStatus.OK, null, "Ok");
    }
}
