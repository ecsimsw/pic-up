package ecsimsw.picup.album.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ThumbnailUtils {

    private static final Color COLOR = new Color(0, 0, 0);

    public static byte[] resize(InputStream input, String format, float scale) throws IOException {
        var originalImage = ImageIO.read(input);
        var width = (int) (originalImage.getWidth() * scale);
        var height = (int) (originalImage.getHeight() * scale);
        var newResizedImage = originalImage.getScaledInstance(
            width,
            height,
            Image.SCALE_SMOOTH
        );

        var imageBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        imageBuff.getGraphics().drawImage(newResizedImage, 0, 0, COLOR, null);
        var buffer = new ByteArrayOutputStream();
        ImageIO.write(imageBuff, format, buffer);
        return buffer.toByteArray();
    }
}
