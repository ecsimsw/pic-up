package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.utils.ThumbnailUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class Thumbnail {

    public byte[] resize(MultipartFile file, float scale) {
        try {
            var fileName = file.getOriginalFilename();
            var format = fileName.substring(fileName.lastIndexOf(".") + 1);
            var inputStream = file.getInputStream();
            return ThumbnailUtils.resize(inputStream, format, scale);
        } catch (IOException e) {
            throw new AlbumException("Invalid multipart upload request");
        }
    }
}
