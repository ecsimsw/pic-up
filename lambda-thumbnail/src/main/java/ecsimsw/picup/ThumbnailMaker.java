package ecsimsw.picup;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.utils.Logger;

import java.io.InputStream;
import java.nio.file.Path;

public class ThumbnailMaker implements RequestHandler<S3Event, String> {

    private static final Logger log = Logger.loggerFor(ThumbnailMaker.class);

    @Override
    public String handleRequest(S3Event s3event, Context context) {
        try {
            S3EventNotification.S3EventNotificationRecord record = s3event.getRecords().get(0);
            String srcBucket = record.getS3().getBucket().getName();
            String srcKey = record.getS3().getObject().getKey();

            log.info(() -> srcBucket + " " + srcKey);

            S3Client s3Client = S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
            InputStream objectData = s3Client.getObject(
                GetObjectRequest.builder()
                    .bucket(srcBucket)
                    .key(srcKey)
                    .build()
            );
            Path thumbnailPath = ThumbnailUtils.resize(objectData, srcKey + "thumbnail.jpg", 0.3f);
            s3Client.putObject(PutObjectRequest.builder()
                .bucket(srcBucket)
                .key(srcKey)
                .build(),
                thumbnailPath
            );
            return "Ok";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}