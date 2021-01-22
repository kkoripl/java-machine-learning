package djl.config;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.Model;
//import ai.djl.basicmodelzoo.basic.Mlp;
import ai.djl.basicmodelzoo.basic.Mlp;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.BufferedImageFactory;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.modality.cv.translator.ObjectDetectionTranslator;
import ai.djl.modality.cv.translator.SingleShotDetectionTranslator;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.tensorflow.zoo.TfModelZoo;
import ai.djl.tensorflow.zoo.cv.objectdetction.TfSsdModelLoader;
import ai.djl.tensorflow.zoo.cv.objectdetction.TfSsdTranslator;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.*;
import djl.config.lettersDivider.LettersDividerProperties;
import djl.config.model.BaseMlpModelFeatures;
import djl.objectDetection.ExtDetectionModelPreparator;
import djl.objectDetection.ObjectDetectionService;
import djl.service.HandwrittingService;
import djl.service.LettersPreparationService;
import djl.handwritting.service.LettersDivider;
import djl.handwritting.translator.HandwrittingTranslator;
import djl.utils.opencv.OpenCVWrapper;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ai.djl.modality.cv.Image.Interpolation.NEAREST;

@Configuration
public class DjlConfig {

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
        Model model = Model.newInstance(features.getName(), "MXNet");
        // Mlp działa na podstawie silnika, na którym został utworzony - trzeba pamiętać, jaki był załączony przy jego uczeniu
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
    public ObjectDetectionService objectDetectionService(
            @Value("${object-detection.external.model.url}") String modelUrl,
            @Value("${object-detection.external.model.engine}") String engine,
            @Value("${object-detection.external.model.engine.backbone}") String engineBackbone,
            @Value("${object-detection.external.model.engine.tags}") String engineTags,
            @Value("${object-detection.external.model.boundingBoxOutputName}") String modelBbOutputName,
            @Value("${object-detection.external.model.classLabelOutputName}") String classOutputName,
            @Value("${object-detection.external.model.scoresOutputName}") String scoresOutputName,
            @Value("${object-detection.external.model.synset.url}") String synsetUrl,
            @Value("${object-detection.external.threshold}") float threshold) throws IOException, ModelNotFoundException, MalformedModelException {

        ExtDetectionModelPreparator modelPreparator = ExtDetectionModelPreparator.builder()
                .modelUrl(modelUrl)
                .engine(engine)
                .engineBackbone(engineBackbone)
                .engineTags(engineTags)
                .boundingBoxOutputName(modelBbOutputName)
                .classLabelOutputName(classOutputName)
                .scoresOutputName(scoresOutputName)
                .synsetUrl(synsetUrl)
                .threshold(threshold)
                .build();

        return new ObjectDetectionService(modelPreparator.buildModel().newPredictor());
    }

    @Bean
    public HandwrittingService handwrittingService(LettersPreparationService lettersPreparationService, Predictor<Mat, Classifications> handwrittingPredictor) {
        return new HandwrittingService(lettersPreparationService, handwrittingPredictor);
    }
}