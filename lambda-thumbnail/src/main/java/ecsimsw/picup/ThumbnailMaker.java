package ecsimsw.picup;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
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
import java.io.*;
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

    private static final String[] IMAGE_EXTENSIONS = new String[]{"jpg", "jpeg", "png"};
    private static final String[] VIDEO_EXTENSIONS = new String[]{"mp4"};

    private static final String DEFAULT_VIDEO_CAPTURE_EXTENSION = "jpg";
    private static final String VIDEO_TEMP_FILE_PATH_PREFIX = "/tmp/";
    private static final int DEFAULT_VIDEO_CAPTURE_FRAME = 1;

    private static final String JPG_MIME = "image/jpeg";
    private static final String PNG_MIME = "image/png";

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
                BufferedImage imageFile = ImageIO.read(s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(originFilePath)
                    .build()));
                BufferedImage resized = resizeImage(imageFile, SCALE_FACTOR);
                fileSize = putObject(s3Client, resized, bucket, thumbnailFilePath, extension);
            }

            if (Arrays.stream(VIDEO_EXTENSIONS).anyMatch(ae -> ae.equalsIgnoreCase(extension))) {
                File videoFile = new File(VIDEO_TEMP_FILE_PATH_PREFIX+"temp." + extension);
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

            URI uri = new URI("https://www.ecismsw.com:8082/api/picture/thumbnail");
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
        if(getExtensionFromName(originFilePath).equals("mp4")) {
            originFilePath = originFilePath.replace(".mp4", "." + DEFAULT_VIDEO_CAPTURE_EXTENSION);
        }
        return originFilePath.replaceFirst(ORIGINAL_UPLOAD_ROOT_PATH, THUMBNAIL_UPLOAD_ROOT_PATH);
    }

    private long putObject(S3Client s3Client, BufferedImage uploadImage, String bucket, String path, String extension) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(uploadImage, extension, outputStream);
            long fileSize = outputStream.size();
            Map<String, String> metadata = Map.of(
                "Content-Length", String.valueOf(fileSize),
                "Content-Type", contentType(extension)
            );
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .metadata(metadata)
                .build();
            s3Client.putObject(
                putObjectRequest,
                RequestBody.fromBytes(outputStream.toByteArray())
            );
            return fileSize;
        } catch (AwsServiceException | IOException e) {
            throw new IllegalArgumentException("failed to upload : " + e.getMessage());
        }
    }

    private BufferedImage resizeImage(BufferedImage srcImage, float scaleFactor) {
        int srcHeight = srcImage.getHeight();
        int srcWidth = srcImage.getWidth();
        int width = (int) (scaleFactor * srcWidth);
        int height = (int) (scaleFactor * srcHeight);
        if (width < MINIMUM_SIZE || height < MINIMUM_SIZE) {
            return srcImage;
        }
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setPaint(Color.white);
        graphics.fillRect(0, 0, width, height);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(srcImage, 0, 0, width, height, null);
        graphics.dispose();
        return resizedImage;
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
}