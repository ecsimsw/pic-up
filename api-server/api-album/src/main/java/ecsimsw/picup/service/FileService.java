package ecsimsw.picup.service;

import ecsimsw.picup.domain.FileExtension;
import ecsimsw.picup.exception.AlbumException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class FileService {

    private final StorageHttpClient storageClient;

    public FileService(StorageHttpClient storageClient) {
        this.storageClient = storageClient;
    }

    public String upload(MultipartFile file, String tag) {
        validateFileType(file.getOriginalFilename());
        var response = storageClient.requestUpload(file, tag);
        return response.getResourceKey();
    }

    public void delete(String resourceKey) {
        validateFileType(resourceKey);
        deleteAll(List.of(resourceKey));
    }

    public void deleteAll(List<String> resources) {
        resources.forEach(this::validateFileType);
        storageClient.requestDelete(resources);
    }

    private void validateFileType(String fileName) {
        if(Objects.isNull(fileName) || !fileName.contains(".")) {
            throw new AlbumException("Invalid file type");
        }
        FileExtension.fromFileName(fileName);
    }

    @RabbitListener(queues = {"hello.queue1"})
    public void onUserRegistration1(String object) {
        System.out.println(LocalDateTime.now() + " q1 " + object);
    }

    @RabbitListener(queues = {"hello.queue2"})
    public void onUserRegistration2(String object) {
        System.out.println(LocalDateTime.now() + " q2 "+ object);
    }
}
