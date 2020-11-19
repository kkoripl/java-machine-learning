package handwritting.dto;

import ai.djl.ndarray.types.Shape;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BaseMlpModelFeatures {

    private String modelPath;
    private String modelName;

    private int[] hiddenLayersNeurons;
    private List<String> classes;

    private Shape inputShape;

    public Integer getInputSize() {
        return new Long(inputShape.size()).intValue();
    }

    public Integer getOutputSize() {
        return classes.size();
    }
}