package ecsimsw.picup.domain;

import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.service.ThumbnailUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public record ImageFile(
    ImageFileType fileType,
    int size,
    byte[] file
) {

    private static final float THUMBNAIL_RESIZE_SCALE = 0.3f;

    public static ImageFile of(String resourceKey, byte[] binaryValue) {
        return new ImageFile(
            ImageFileType.extensionOf(resourceKey),
            binaryValue.length,
            binaryValue
        );
    }

    public static ImageFile of(MultipartFile file) {
        try {
            var imageFileType = ImageFileType.extensionOf(file.getOriginalFilename());
            var fileValue = file.getBytes();
            return new ImageFile(imageFileType, fileValue.length, fileValue);
        } catch (IOException e) {
            throw new InvalidResourceException("Invalid multipart file");
        }
    }

    public static ImageFile thumbnailFrom(MultipartFile file) {
        try {
            var imageFileType = ImageFileType.extensionOf(file.getOriginalFilename());
            var fileValue = ThumbnailUtils.resize(
                file.getInputStream(),
                imageFileType.name(),
                THUMBNAIL_RESIZE_SCALE
            );
            return new ImageFile(imageFileType, fileValue.length, fileValue);
        } catch (IOException e) {
            throw new InvalidResourceException("Invalid multipart file");
        }
    }
}
