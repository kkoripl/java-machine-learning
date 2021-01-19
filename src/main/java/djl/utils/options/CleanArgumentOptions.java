package djl.utils.options;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CleanArgumentOptions {
    IMAGE_PATH("imgs","image-path", "IMAGE-PATH", "Path to image dataset to be cleaned"),
    CSV_PATH("csv", "csv-path", "CSV-PATH", "Path to csv dataset"),
    FINAL_PATH("final", "final-path", "FINAL_PATH", "Path to destination of finally prepared dataset"),
    CLEANING_TEMPLATE_PATH("cleanTempl", "clean-template-path", "CLEAN_TEMPLATE_PATH", "Path to cleaning template image"),
    UNWANTED_PART_PATH("unwimg", "unwanted-img-path", "UNWANTED-IMG", "Path to image needed to remove from images"),
    UNWANTED_SIMILARITY("unwsim", "unwanted-similarity", "UNWANTED-SIMILARITY", "Unwanted image similarity needed for removal"),
    IMAGE_COLUMN("imgcol", "image-column", "IMAGE-COLUMN", "Csv dataset image column"),
    LABEL_COLUMN("labcol", "label-column", "LABEL-COLUMN", "Csv dataset label column"),
    IMAGE_WIDTH("iw", "image-width", "IMAGE-WIDTH", "Image width in pixels"),
    IMAGE_HEIGHT("ih", "image-height", "IMAGE-HEIGHT", "Image height in pixels"),
    JOINED_I_AREA_PX("joinedIArea", "joined-i-area", "JOINED-I-AREA", "Joined dot of I area in pixels"),
    COLOR_THRESHOLD("cthres", "color-threshold", "COLOR-THRESHOLD", "Color threshold");

    private String opt;
    private String longOpt;
    private String argName;
    private String desc;
}
