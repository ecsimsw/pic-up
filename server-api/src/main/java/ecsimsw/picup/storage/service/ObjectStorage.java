package ecsimsw.picup.storage.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.exception.StorageException;
import ecsimsw.picup.storage.utils.AwsS3Utils;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component
public class ObjectStorage implements ImageStorage {

    private static final String BUCKET_NAME = "picup-ecsimsw";

    private AmazonS3 s3Client;

    public ObjectStorage(
        @Value("${object.storage.credential.accessKey}") String accessKey,
        @Value("${object.storage.credential.secretKey}") String secretKey
    ) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(
            accessKey,
            secretKey
        );
        this.s3Client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
            .withRegion(Regions.AP_NORTHEAST_2)
            .build();

    }

    //    @Async
    @Override
    public CompletableFuture<String> storeAsync(String resourceKey, FileUploadResponse fileUploadResponse) {
        try {
            AwsS3Utils.upload(s3Client, BUCKET_NAME, resourceKey, fileUploadResponse);
            return new AsyncResult<>(resourceKey).completable();
        } catch (Exception e) {
            throw new StorageException("Object storage server exception while uploading", e);
        }
    }

    @Override
    public FileUploadResponse read(String resourceKey) {
        var file = AwsS3Utils.read(s3Client, BUCKET_NAME, resourceKey);
        return FileUploadResponse.of(resourceKey, file);
    }

    @Override
    public void deleteIfExists(String resourceKey) {
        AwsS3Utils.deleteIfExists(s3Client, BUCKET_NAME, resourceKey);
    }
}

