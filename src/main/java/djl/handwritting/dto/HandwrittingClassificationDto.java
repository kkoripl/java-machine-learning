package djl.handwritting.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class HandwrittingClassificationDto {
    private String text;
    private List<BigDecimal> probabilities = new ArrayList<>();
    private List<String> classes = new ArrayList<>();

    public void addProbability(double probability) {
        this.probabilities.add(BigDecimal.valueOf(100*probability).setScale(2, RoundingMode.HALF_UP));
    }
    public void addClass(String clazz) {
        this.classes.add(clazz);
    }
}
