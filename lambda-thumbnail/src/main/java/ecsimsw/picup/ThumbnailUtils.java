package ecsimsw.picup;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.ImageFilter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class ThumbnailUtils {

    private static final int MINIMUM_SIZE = 300;
    private static final Color COLOR = new Color(0, 0, 0);

    public static Path resize(InputStream input, String fileName, float scale) throws IOException {
        BufferedImage imageBuff = Thumbnails.of(input)
            .scale(1)
            .addFilter(getImageFilter(scale))
            .useExifOrientation(true)
            .asBufferedImage();
        Graphics graphics = imageBuff.getGraphics();
        graphics.drawImage(imageBuff, 0, 0, COLOR, null);

        String extension = getExtensionFromName(fileName);
        File outputfile = new File(fileName);
        ImageIO.write(imageBuff, extension, outputfile);
        return outputfile.toPath();
    }

    public static String getExtensionFromName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }
        var indexOfExtension = fileName.lastIndexOf(".");
        return fileName.substring(indexOfExtension + 1);
    }

    private static ImageFilter getImageFilter(float scale) {
        return image -> {
            try {
                if (image.getWidth() * scale > MINIMUM_SIZE && image.getHeight() * scale > MINIMUM_SIZE) {
                    return Thumbnails.of(image)
                        .size(
                            (int) (image.getWidth() * scale),
                            (int) (image.getHeight() * scale)
                        ).asBufferedImage();
                }
                return image;
            } catch (IOException e) {
                return image;
            }
        };
    }
}
