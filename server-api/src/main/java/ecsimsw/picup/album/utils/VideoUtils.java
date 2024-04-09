package ecsimsw.picup.album.utils;

import ecsimsw.picup.album.exception.StorageException;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class VideoUtils {

    public static BufferedImage getFrame(File file, int frameNumber) {
        try {
            Picture picture = FrameGrab.getFrameFromFile(file, frameNumber);
            return AWTUtil.toBufferedImage(picture);
        } catch (JCodecException | IOException e) {
            throw new StorageException("Failed to get frame of : " + file.getAbsolutePath());
        }
    }
}
