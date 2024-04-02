package ecsimsw.picup.album.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.ImageFilter;

public class ThumbnailUtils {

    private static final int MINIMUM_SIZE = 300;
    private static final Color COLOR = new Color(0, 0, 0);

    public static byte[] resize(InputStream input, String format, float scale) throws IOException {
        var imageBuff = Thumbnails.of(input)
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
                        .scale(scale)
                        .asBufferedImage();
                }
                return image;
            } catch (IOException e) {
                return image;
            }
        };
    }
}
