package ecsimsw.picup.domain;

import ecsimsw.picup.utils.AlbumFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PictureTest {

    @DisplayName("소속 앨범없이 Picture 는 생성될 수 없다.")
    @Test
    void pictureWithoutAlbum() {
        assertThatThrownBy(
            () -> new Picture(null, AlbumFixture.RESOURCE_KEY, 1L)
        );
    }

    @DisplayName("리소스 키 없이 Picture 는 생성될 수 없다.")
    @Test
    void pictureWithoutResourceKey() {
        assertThatThrownBy(
            () -> new Picture(AlbumFixture.ALBUM, null, 1L)
        );
    }

    @DisplayName("Picture 의 파일 크기는 음수일 수 없다.")
    @Test
    void pictureWithInvalidFileSize() {
        assertThatThrownBy(
            () -> new Picture(AlbumFixture.ALBUM, AlbumFixture.RESOURCE_KEY, -1L)
        );
    }

    @DisplayName("사진의 소유자가 맞는지 확인한다.")
    @Test
    void checkSameUser() {
        var userId = 1L;
        var album = new Album(userId, AlbumFixture.ALBUM_NAME, AlbumFixture.THUMBNAIL_RESOURCE_KEY);
        var picture = new Picture(album, AlbumFixture.RESOURCE_KEY, AlbumFixture.FILE_SIZE);
        assertThatThrownBy(() -> {
            var wrongUserId = userId + 1;
            picture.checkSameUser(wrongUserId);
        });
    }
}