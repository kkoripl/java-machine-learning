package djl.config;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicmodelzoo.basic.Mlp;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import djl.config.lettersDivider.LettersDividerProperties;
import djl.config.model.BaseMlpModelFeatures;
import djl.service.HandwrittingService;
import djl.service.LettersPreparationService;
import djl.handwritting.service.LettersDivider;
import djl.handwritting.translator.HandwrittingTranslator;
import djl.utils.opencv.OpenCVWrapper;
import org.opencv.core.Mat;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class HandwrittingConfig {

    @Bean
    @ConfigurationProperties("model")
    public BaseMlpModelFeatures baseMlpModelFeatures() {
        return new BaseMlpModelFeatures();
    }

    @Bean
    @ConfigurationProperties("letters-divider")
    public LettersDividerProperties lettersDividerProperties() {
        return new LettersDividerProperties();
    }

    @Bean
    public OpenCVWrapper openCVWrapper() {
        return new OpenCVWrapper();
    }

    @Bean
    public Model handwrittingModel(BaseMlpModelFeatures features) throws IOException, MalformedModelException {
        Model model = Model.newInstance(features.getName());
        model.setBlock(new Mlp(features.getInputSize(), features.getOutputSize(), features.getHiddenLayersNeurons()));
        model.load(Paths.get(features.getPath()));
        return model;
    }

    @Bean
    public Predictor<Mat, Classifications> handwrittingPredictor(Model handwrittingModel, BaseMlpModelFeatures features) {
        return handwrittingModel.newPredictor(new HandwrittingTranslator(features.getClasses()));
    }

    @Bean
    public LettersDivider lettersDivider(OpenCVWrapper openCVWrapper, LettersDividerProperties properties) {
        return LettersDivider.builder()
                .openCvWrapper(openCVWrapper)
                .black1dColor(properties.getColor().getBlack())
                .white1dColor(properties.getColor().getWhite())
                .colorThreshold(properties.getColor().getThreshold())
                .binarizedBlack(properties.getBinarized().getBlack())
                .binarizedWhite(properties.getBinarized().getWhite())
                .joinedIAreaPx(properties.getJoinedIAreaPx())
                .templateFilePath(properties.getTemplateFilePath())
                .templateSimilarityThreshold(properties.getTemplateSimilarityThreshold())
                .imgHeight(properties.getImgHeight())
                .imgWidth(properties.getImgWidth())
                .build();
    }

    @Bean
    public LettersPreparationService lettersPreparationService(LettersDivider lettersDivider) {
        return new LettersPreparationService(lettersDivider);
    }

    @Bean
    public HandwrittingService handwrittingService(LettersPreparationService lettersPreparationService, Predictor<Mat, Classifications> handwrittingPredictor) {
        return new HandwrittingService(lettersPreparationService, handwrittingPredictor);
    }
}
