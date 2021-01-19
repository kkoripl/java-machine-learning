package djl.config.model;

import ai.djl.ndarray.types.Shape;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Configuration;

import java.util.List;

//@Configuration
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseMlpModelFeatures {

    private String path;
    private String name;

    private int[] hiddenLayersNeurons;
    private List<String> classes;

    private Shape inputShape;

    public void setInputShape(int[] shape) {
        this.inputShape = new Shape(shape[0], shape[1], shape[2]);
    }

    public Integer getInputSize() {
        return new Long(inputShape.size()).intValue();
    }

    public Integer getOutputSize() {
        return classes.size();
    }
}