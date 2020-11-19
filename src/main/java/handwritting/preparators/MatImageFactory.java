package handwritting.preparators;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import handwritting.utils.opencv.OpenCVWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Klasa nominalnie przydatna do wczytywania obrazów w danym formacie, tak by były przyjmowane przez DJL**/
public class MatImageFactory extends ImageFactory {
    @Override
    public Image fromFile(Path path) {
        return new MatImageWrapper(OpenCVWrapper.loadImage(path.toString()));
    }

    @Override
    public Image fromUrl(URL url) {
        return null;
    }

    @Override
    public Image fromInputStream(InputStream is) {
        return null;
    }

    @Override
    public Image fromImage(Object image) {
        return null;
    }
}
