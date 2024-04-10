package ecsimsw.picup.album.utils;

import ecsimsw.picup.album.utils.VideoUtils;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.ImageFilter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ThumbnailUtils {

    private static final int MINIMUM_SIZE = 300;
    private static final Color COLOR = new Color(0, 0, 0);

    public static byte[] resize(InputStream input, String format, float scale) throws IOException {
        BufferedImage imageBuff = Thumbnails.of(input)
            .scale(1)
            .useExifOrientation(true)
            .addFilter(getImageFilter(scale))
            .asBufferedImage();
        Graphics graphics = imageBuff.getGraphics();
        graphics.drawImage(imageBuff, 0, 0, COLOR, null);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ImageIO.write(imageBuff, format, buffer);
        return buffer.toByteArray();
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
