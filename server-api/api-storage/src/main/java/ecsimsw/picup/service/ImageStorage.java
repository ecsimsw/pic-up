package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;

public interface ImageStorage {

    CompletableFuture<Resource> storeAsync(Resource resource, ImageFile imageFile);

    ImageFile read(Resource resource) throws FileNotFoundException;

    void deleteIfExists(Resource resource);
}
