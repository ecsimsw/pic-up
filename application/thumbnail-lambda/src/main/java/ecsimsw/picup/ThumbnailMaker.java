package ecsimsw.picup;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.ImageFilter;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;

public class ThumbnailMaker implements RequestHandler<S3Event, String> {

    private static final String ORIGINAL_UPLOAD_ROOT_PATH = "storage/";
    private static final String THUMBNAIL_UPLOAD_ROOT_PATH = "thumb/";

    private static final float MINIMUM_SIZE = 300;
    private static final float SCALE_FACTOR = 0.3f;
    private static final Color COLOR = new Color(0, 0, 0);

    private static final String[] IMAGE_EXTENSIONS = new String[]{"jpg", "jpeg", "png"};
    private static final String[] VIDEO_EXTENSIONS = new String[]{"mp4"};

    private static final String DEFAULT_VIDEO_CAPTURE_EXTENSION = "jpg";
    private static final String VIDEO_TEMP_FILE_PATH_PREFIX = "/tmp/";
    private static final int DEFAULT_VIDEO_CAPTURE_FRAME = 1;

    private static final String JPG_MIME = "image/jpeg";
    private static final String PNG_MIME = "image/png";

    private static final String APPLICATION_SERVER_URL = "https://www.ecsimsw.com:8082/api/storage/thumbnail";

    private static final Logger log = Logger.loggerFor(ThumbnailMaker.class);

    @Override
    public String handleRequest(S3Event s3event, Context context) {
        try (
            S3Client s3Client = S3Client.builder().build();
        ) {
            S3EventNotificationRecord record = s3event.getRecords().get(0);
            String bucket = record.getS3().getBucket().getName();
            String originFilePath = record.getS3().getObject().getUrlDecodedKey();
            String thumbnailFilePath = thumbnailPath(originFilePath);
            String extension = getExtensionFromName(originFilePath);

            log.info(() -> "In : " + originFilePath);
            if (!originFilePath.startsWith(ORIGINAL_UPLOAD_ROOT_PATH)) {
                return "";
            }

            long fileSize = 0L;
            if (Arrays.stream(IMAGE_EXTENSIONS).anyMatch(ae -> ae.equalsIgnoreCase(extension))) {
                BufferedImage resized = resize(s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(originFilePath)
                    .build())
                );
                fileSize = putObject(s3Client, resized, bucket, thumbnailFilePath, extension);
            }

            if (Arrays.stream(VIDEO_EXTENSIONS).anyMatch(ae -> ae.equalsIgnoreCase(extension))) {
                File videoFile = new File(VIDEO_TEMP_FILE_PATH_PREFIX + "temp." + extension);
                ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(originFilePath)
                    .build());
                Files.copy(object, videoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Picture picture = FrameGrab.getFrameFromFile(videoFile, DEFAULT_VIDEO_CAPTURE_FRAME);
                BufferedImage captured = AWTUtil.toBufferedImage(picture);
                fileSize = putObject(s3Client, captured, bucket, thumbnailFilePath, "jpg");
            }
            log.info(() -> "Upload : " + thumbnailFilePath);

            URI uri = new URI(APPLICATION_SERVER_URL);
            uri = new URIBuilder(uri)
                .addParameter("resourceKey", originFilePath.replaceFirst(ORIGINAL_UPLOAD_ROOT_PATH, ""))
                .addParameter("fileSize", String.valueOf(fileSize))
                .build();
            HttpClient httpClient = HttpClientBuilder.create().build();
            httpClient.execute(new HttpPost(uri));
            return "Ok";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String thumbnailPath(String originFilePath) {
        String extension = getExtensionFromName(originFilePath);
        if (Arrays.stream(VIDEO_EXTENSIONS).anyMatch(ae -> ae.equalsIgnoreCase(extension))) {
            originFilePath = changeExtensionTo(originFilePath, DEFAULT_VIDEO_CAPTURE_EXTENSION);
        }
        return originFilePath.replaceFirst(ORIGINAL_UPLOAD_ROOT_PATH, THUMBNAIL_UPLOAD_ROOT_PATH);
    }

    private long putObject(S3Client s3Client, BufferedImage uploadImage, String bucket, String path, String extension) {
        try {
            Graphics graphics = uploadImage.getGraphics();
            graphics.drawImage(uploadImage, 0, 0, COLOR, null);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ImageIO.write(uploadImage, extension, buffer);
            int fileSize = buffer.size();
            Map<String, String> metadata = Map.of(
                "Content-Length", String.valueOf(fileSize),
                "Content-Type", contentType(extension)
            );
            s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .metadata(metadata)
                .build(), RequestBody.fromBytes(buffer.toByteArray())
            );
            return fileSize;
        } catch (AwsServiceException | IOException e) {
            throw new IllegalArgumentException("failed to upload : " + e.getMessage());
        }
    }

    public BufferedImage resize(InputStream input) throws IOException {
        float scale = SCALE_FACTOR;
        return Thumbnails.of(input)
            .scale(1)
            .addFilter(image -> {
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
            })
            .useExifOrientation(true)
            .asBufferedImage();
    }

    private String getExtensionFromName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }
        var indexOfExtension = fileName.lastIndexOf(".");
        return fileName.substring(indexOfExtension + 1);
    }

    private String contentType(String extension) {
        if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
            return JPG_MIME;
        }
        if (extension.equalsIgnoreCase("png")) {
            return PNG_MIME;
        }
        throw new IllegalArgumentException("invalid content type");
    }

    public String changeExtensionTo(String originFileName, String newExtension) {
        var extension = getExtensionFromName(originFileName);
        var sb = new StringBuilder(originFileName);
        var extensionIndex = originFileName.lastIndexOf(extension);
        sb.replace(extensionIndex, originFileName.length() + extensionIndex, newExtension);
        return sb.toString();
    }
}