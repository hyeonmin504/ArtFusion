package _2.ArtFusion.controller.generateStoryApiController;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class FailApiResponseForm {
    private List<Integer> failSeq;
    private boolean singleResult;

    public FailApiResponseForm() {
        failSeq = new ArrayList<>();
    }

    public void setFailSeq(int failNum) {
        failSeq.add(failNum);
    }

    public void setSingleResult(boolean singleResult) {
        this.singleResult = singleResult;
    }
}
