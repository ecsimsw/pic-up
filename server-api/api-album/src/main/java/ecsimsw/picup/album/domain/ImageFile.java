package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.utils.ThumbnailUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public record ImageFile(
    String resourceKey,
    ImageFileExtension format,
    byte[] file
) {

    public static ImageFile of(MultipartFile file) {
        try {
            var resourceKey = ResourceKeyStrategy.generate(file);
            var fileName = file.getOriginalFilename();
            var format = ImageFileExtension.fromFileName(fileName);
            return new ImageFile(resourceKey, format, file.getBytes());
        } catch (IOException | NullPointerException e) {
            throw new AlbumException("Invalid multipart file");
        }
    }

    public static ImageFile resizedOf(MultipartFile file, float scale) {
        try {
            var resourceKey = ResourceKeyStrategy.generate(file);
            var fileName = file.getOriginalFilename();
            var format = ImageFileExtension.fromFileName(fileName);
            var inputStream = file.getInputStream();
            var resized = ThumbnailUtils.resize(inputStream, format.name(), scale);
            return new ImageFile(resourceKey, format, resized);
        } catch (IOException | NullPointerException e) {
            throw new AlbumException("Invalid multipart upload request");
        }
    }

    public MultipartFile toMultipartFile() {
        return new MockMultipartFile(resourceKey, resourceKey, format.name(), file);
    }
}
