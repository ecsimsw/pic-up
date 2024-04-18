package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.utils.ResourceKeyStrategy;
import ecsimsw.picup.album.utils.ThumbnailUtils;
import java.io.IOException;
import java.util.Objects;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public record FileUploadRequest(
    String resourceKey,
    PictureFileExtension format,
    byte[] file
) {

    public static FileUploadRequest of(byte[] file, String resourceKey) {
        var format = PictureFileExtension.fromFileName(resourceKey);
        return new FileUploadRequest(resourceKey, format, file);
    }

    public static FileUploadRequest of(MultipartFile file) {
        try {
            var resourceKey = ResourceKeyStrategy.generate(file);
            var format = getFileFormat(file);
            return new FileUploadRequest(resourceKey, format, file.getBytes());
        } catch (IOException | NullPointerException e) {
            throw new AlbumException("Invalid multipart file");
        }
    }

    public static FileUploadRequest resizedOf(MultipartFile file, float scale) {
        try {
            var resourceKey = ResourceKeyStrategy.generate(file);
            var format = getFileFormat(file);
            var resized = ThumbnailUtils.resize(file.getInputStream(), format.name(), scale);
            return new FileUploadRequest(resourceKey, format, resized);
        } catch (IOException e) {
            throw new AlbumException("Invalid multipart upload request");
        }
    }

    private static PictureFileExtension getFileFormat(MultipartFile file) {
        Objects.requireNonNull(file.getOriginalFilename());
        return PictureFileExtension.fromFileName(file.getOriginalFilename());
    }

    public MultipartFile toMultipartFile() {
        return new MockMultipartFile(resourceKey, resourceKey, format.name(), file);
    }
}
