package djl.objectDetection;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.tensorflow.zoo.cv.objectdetction.TfSsdTranslator;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslatorContext;
import lombok.Builder;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Builder
public class ExtDetectionModelPreparator {

    private final float threshold;
    private final String modelUrl;
    private final String synsetUrl;
    private final String boundingBoxOutputName;
    private final String classLabelOutputName;
    private final String scoresOutputName;
    private final String engine;
    private final String engineTags;
    private final String engineBackbone;
    private final String engineSignatureDefKey;

    public ZooModel<Image, DetectedObjects> buildModel() throws MalformedModelException, ModelNotFoundException, IOException {
        TfSsdTranslator.Builder tfssdBuilder = TfSsdTranslator.builder()
                .optThreshold(threshold)
                .setPipeline(new Pipeline())
                .optSynsetUrl("file:///" + Paths.get(synsetUrl).toAbsolutePath().toString())
                .optBoundingBoxOutputName(boundingBoxOutputName)
                .optClassLabelOutputName(classLabelOutputName)
                .optScoresOutputName(scoresOutputName);

        TfSsdTranslator translator = new TfSsdTranslator(tfssdBuilder) {
            @Override
            public DetectedObjects processOutput(TranslatorContext ctx, NDList list) {
                int len = (int) list.get(0).getShape().get(0);
                float[] scores = new float[len];
                float[] classIds = new float[len];

                NDArray boundingBoxes = list.get(0);
                for (NDArray array : list) {
                    if (scoresOutputName.equals(array.getName())) {
                        scores = array.toFloatArray();
                    } else if (boundingBoxOutputName.equals(array.getName())) {
                        boundingBoxes = array;
                    } else if (classLabelOutputName.equals(array.getName())) {
                        classIds = array.toFloatArray();
                    }
                }
                List<String> retNames = new ArrayList<>();
                List<Double> retProbs = new ArrayList<>();
                List<BoundingBox> retBB = new ArrayList<>();

                // results are already sorted according to scores
                for (int i = 0; i < Math.min(classIds.length, 100); ++i) {
                    float classId = classIds[i];
                    double score = scores[i];
                    // classId starts from 0, -1 means background
                    if (classId >= 0 && score > threshold) {
                        if (classId >= classes.size()) {
                            throw new AssertionError("Unexpected index: " + classId);
                        }
                        String className = classes.get((int) classId - 1);
                        float[] box = boundingBoxes.get(0).get(i).toFloatArray();
                        float yMin = box[0];
                        float xMin = box[1];
                        float yMax = box[2];
                        float xMax = box[3];
                        double w = xMax - xMin;
                        double h = yMax - yMin;
                        Rectangle rect = new Rectangle(xMin, yMin, w, h);
                        retNames.add(className);
                        retProbs.add(score);
                        retBB.add(rect);
                    }
                }

                return new DetectedObjects(retNames, retProbs, retBB);
            }
        };

        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .optApplication(Application.CV.OBJECT_DETECTION)
                        .setTypes(Image.class, DetectedObjects.class)
                        .optEngine(engine)
                        .optTranslator(translator)
                        .optOption("Tags", engineTags) // Tags dla Tensorflow 1.x => "", dla 2.x => "serve", ten drugi to default
//              .optOption("SignatureDefKey", "default") // Tensorflow 1.x
                        .optFilter("backbone", engineBackbone) //ResNet-v2-50
                        .optModelUrls(Paths.get(modelUrl).toAbsolutePath().toString())
                        .build();


        return ModelZoo.loadModel(criteria);
    }



}
