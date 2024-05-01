package ecsimsw.picup;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import javax.imageio.ImageIO;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.Logger;

public class ThumbnailMaker implements RequestHandler<S3Event, String> {

    private static final String ORIGINAL_UPLOAD_ROOT_PATH = "storage/";
    private static final String THUMBNAIL_UPLOAD_ROOT_PATH = "thumb/";

    private static final float MINIMUM_SIZE = 300;
    private static final float SCALE_FACTOR = 0.3f;

    private static final String[] ALLOW_FILE_EXTENSION = new String[]{"jpg", "jpeg", "png"};
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
            String originUploadPath = record.getS3().getObject().getUrlDecodedKey();
            String extension = getExtensionFromName(originUploadPath);
            if (!isThumbnailNeeded(extension, originUploadPath)) {
                return "";
            }

            log.info(() -> "In : " + originUploadPath);
            BufferedImage srcImage = getObject(s3Client, bucket, originUploadPath);
            BufferedImage resized = resizeImage(srcImage, SCALE_FACTOR);
            String thumbnailUploadPath = thumbnailUploadPath(originUploadPath);
            putObject(s3Client, resized, bucket, thumbnailUploadPath, extension);
            log.info(() -> "Writing to: " + thumbnailUploadPath);

            return "Ok";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String thumbnailUploadPath(String srcKey) {
        return srcKey.replaceFirst(ORIGINAL_UPLOAD_ROOT_PATH, THUMBNAIL_UPLOAD_ROOT_PATH);
    }

    private boolean isThumbnailNeeded(String extension, String srcKey) {
        return Arrays.stream(ALLOW_FILE_EXTENSION).anyMatch(ae -> ae.equalsIgnoreCase(extension))
            || srcKey.startsWith(ORIGINAL_UPLOAD_ROOT_PATH);
    }

    private BufferedImage getObject(S3Client s3Client, String srcBucket, String srcKey) throws IOException {
        InputStream s3Object = s3Client.getObject(GetObjectRequest.builder()
            .bucket(srcBucket)
            .key(srcKey)
            .build());
        return ImageIO.read(s3Object);
    }

    private void putObject(S3Client s3Client, BufferedImage uploadImage, String bucket, String path, String extension) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(uploadImage, extension, outputStream);
            Map<String, String> metadata = Map.of(
                "Content-Length", String.valueOf(outputStream.size()),
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
        } catch (AwsServiceException | IOException e) {
            throw new IllegalArgumentException("failed to upload : " + e.getMessage());
        }
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
}