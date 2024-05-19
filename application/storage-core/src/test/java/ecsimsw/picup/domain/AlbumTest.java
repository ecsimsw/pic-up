package ecsimsw.picup.domain;

import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.utils.AlbumFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlbumTest {

    @DisplayName("앨범의 사용자를 확인한다.")
    @Test
    void authorize() {
        var userId = 1L;
        var album = new Album(userId, AlbumFixture.ALBUM_NAME, AlbumFixture.RESOURCE_KEY);
        album.authorize(userId);
    }

    @DisplayName("앨범의 사용자가 아닌 경우, 사용자 확인에 예외를 반환한다.")
    @Test
    void authorizeInvalidUser() {
        var userId = 1L;
        var album = new Album(userId, AlbumFixture.ALBUM_NAME, AlbumFixture.RESOURCE_KEY);
        var invalidUserId = userId + 1;
        assertThatThrownBy(
            () -> album.authorize(invalidUserId)
        ).isInstanceOf(AlbumException.class);
    }
}