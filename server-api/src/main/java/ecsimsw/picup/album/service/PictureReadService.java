package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PictureReadService {

    private final PictureService pictureService;

    public List<PictureInfoResponse> pictures(Long userId, Long albumId, PictureSearchCursor cursor) {
        var pictures = pictureService.fetchOrderByCursor(userId, albumId, cursor);
        return PictureInfoResponse.listOf(pictures);
    }
}
