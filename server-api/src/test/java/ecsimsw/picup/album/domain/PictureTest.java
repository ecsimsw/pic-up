package ecsimsw.picup.album.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static ecsimsw.picup.env.AlbumFixture.ALBUM;
import static ecsimsw.picup.env.AlbumFixture.RESOURCE_KEY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PictureTest {

    @DisplayName("소속 앨범없이 Picture 는 생성될 수 없다.")
    @Test
    void pictureWithoutAlbum() {
        assertThatThrownBy(
            () -> new Picture(null, RESOURCE_KEY, 1L)
        );
    }

    @DisplayName("리소스 키 없이 Picture 는 생성될 수 없다.")
    @Test
    void pictureWithoutResourceKey() {
        assertThatThrownBy(
            () -> new Picture(ALBUM(), null, 1L)
        );
    }

    @DisplayName("Picture 의 파일 크기는 음수일 수 없다.")
    @Test
    void pictureWithInvalidFileSize() {
        assertThatThrownBy(
            () -> new Picture(ALBUM(), RESOURCE_KEY, -1L)
        );
    }

    @DisplayName("사진의 소유자가 아니라면 예외를 발생시킨다.")
    @Test
    void checkSameUser() {
        var userId = 1L;
        var picture = new Picture(ALBUM(userId), RESOURCE_KEY, 1L);
        assertThatThrownBy(() -> {
            var wrongUserId = userId + 1;
            picture.checkSameUser(wrongUserId);
        });
    }
}