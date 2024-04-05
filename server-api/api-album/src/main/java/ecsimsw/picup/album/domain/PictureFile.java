package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.utils.ThumbnailUtils;
import java.util.Objects;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public record PictureFile(
    String resourceKey,
    PictureFileExtension format,
    byte[] file
) {

    public static PictureFile of(MultipartFile file) {
        try {
            var resourceKey = generateResourceKey(file);
            var format = getFileFormat(file);
            return new PictureFile(resourceKey, format, file.getBytes());
        } catch (IOException | NullPointerException e) {
            throw new AlbumException("Invalid multipart file");
        }
    }

    public static PictureFile resizedOf(MultipartFile file, float scale) {
        try {
            var resourceKey = generateResourceKey(file);
            var format = getFileFormat(file);
            var resized = ThumbnailUtils.resize(file.getInputStream(), format.name(), scale);
            return new PictureFile(resourceKey, format, resized);
        } catch (IOException e) {
            throw new AlbumException("Invalid multipart upload request");
        }
    }

    public MultipartFile toMultipartFile() {
        return new MockMultipartFile(resourceKey, resourceKey, format.name(), file);
    }

    private static PictureFileExtension getFileFormat(MultipartFile file) {
        Objects.requireNonNull(file.getOriginalFilename());
        return PictureFileExtension.fromFileName(file.getOriginalFilename());
    }

    private static String generateResourceKey(MultipartFile file) {
        return ResourceKeyStrategy.generate(file);
    }
}
