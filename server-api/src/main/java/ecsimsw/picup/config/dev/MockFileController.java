//package ecsimsw.picup.config.dev;
//
//import com.amazonaws.services.s3.AmazonS3;
//import ecsimsw.picup.album.service.PictureService;
//import ecsimsw.picup.album.service.ThumbnailService;
//import ecsimsw.picup.storage.S3Utils;
//import java.io.IOException;
//import javax.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Profile;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import static ecsimsw.picup.album.service.FileResourceService.BUCKET;
//import static ecsimsw.picup.album.service.FileResourceService.ROOT_PATH;
//import static ecsimsw.picup.album.service.FileResourceService.THUMBNAIL_PATH;
//
//@RequiredArgsConstructor
//@Profile("dev")
//@RestController
//public class MockFileController {
//
//    private final AmazonS3 amazonS3;
//    private final PictureService pictureService;
//    private final ThumbnailService thumbnailService;
//
//    @PutMapping(ROOT_PATH + "{resourceKey}")
//    public void upload(@PathVariable String resourceKey, MultipartFile file) {
//        System.out.println("UPLOAD : " + ROOT_PATH + resourceKey + " " + file.getSize());
//        S3Utils.store(amazonS3, BUCKET, ROOT_PATH + resourceKey, file);
//        var thumbnailFile = thumbnailService.captureVideo(file);
//        S3Utils.store(amazonS3, BUCKET, THUMBNAIL_PATH + resourceKey, thumbnailFile);
//        pictureService.saveThumbnailResource(resourceKey, thumbnailFile.getSize());
//    }
//
//    @GetMapping(
//        value = ROOT_PATH + "{resourceKey}",
//        produces = MediaType.ALL_VALUE
//    )
//    public ResponseEntity<Void> resource(
//        @PathVariable String resourceKey,
//        HttpServletResponse response
//    ) throws IOException {
//        System.out.println("DOWNLOAD : " + ROOT_PATH + resourceKey);
//        S3Utils.getResource(amazonS3, BUCKET, ROOT_PATH + resourceKey, response.getOutputStream());
//        return ResponseEntity.ok().build();
//    }
//}
