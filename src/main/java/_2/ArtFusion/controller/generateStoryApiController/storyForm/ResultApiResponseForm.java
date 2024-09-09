package _2.ArtFusion.controller.generateStoryApiController.storyForm;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ResultApiResponseForm {
    private List<Integer> failedSeq = new ArrayList<>();
    private boolean singleResult = true;

    public ResultApiResponseForm() {
        failedSeq = new ArrayList<>();
    }

    public void setFailSeq(Integer failNum) {
        failedSeq.add(failNum);
    }

    public void setSingleResult(boolean singleResult) {
        this.singleResult = singleResult;
    }
}
