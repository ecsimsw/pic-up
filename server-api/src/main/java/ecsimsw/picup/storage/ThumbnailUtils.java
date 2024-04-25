package ecsimsw.picup.storage;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.ImageFilter;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

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
            .addFilter(getImageFilter(scale))
            .useExifOrientation(true)
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

    public static byte[] capture(String videoFilePath, int frameNumber, String format) {
        try {
            Picture picture = FrameGrab.getFrameFromFile(new File(videoFilePath), frameNumber);
            BufferedImage image = AWTUtil.toBufferedImage(picture);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);
            return baos.toByteArray();
        } catch (JCodecException | IOException e) {
            throw new IllegalArgumentException("Failed to capture from video : " + videoFilePath);
        }
    }
}
