package demo.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageProperties {
    private int width;
    private int height;
    private int channels;

    public int getPixelsCount() {
        return this.getHeight() * this.getWidth() * this.getChannels();
    }
}
