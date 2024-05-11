package ecsimsw.picup.domain;

import static ecsimsw.picup.utils.AlbumFixture.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.auth.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AlbumTest {

    @DisplayName("앨범의 사용자를 확인한다.")
    @Test
    void authorize() {
        var userId = 1L;
        var album = new Album(userId, ALBUM_NAME, RESOURCE_KEY);
        album.authorize(userId);
    }

    @DisplayName("앨범의 사용자가 아닌 경우, 사용자 확인에 예외를 반환한다.")
    @Test
    void authorizeInvalidUser() {
        var userId = 1L;
        var album = new Album(userId, ALBUM_NAME, RESOURCE_KEY);
        var invalidUserId = userId + 1;
        assertThatThrownBy(
            () -> album.authorize(invalidUserId)
        ).isInstanceOf(UnauthorizedException.class);
    }
}