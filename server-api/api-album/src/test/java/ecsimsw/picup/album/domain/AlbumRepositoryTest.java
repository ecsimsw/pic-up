//package ecsimsw.picup.album.domain;
//
//import static ecsimsw.picup.env.AlbumFixture.ALBUM;
//import static ecsimsw.picup.env.AlbumFixture.ALBUM_NAME;
//import static ecsimsw.picup.env.AlbumFixture.RESOURCE_KEY;
//import static ecsimsw.picup.env.AlbumFixture.THUMBNAIL_RESOURCE_KEY;
//import static ecsimsw.picup.env.MemberFixture.MEMBER_ID;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.assertAll;
//
//import ecsimsw.picup.album.domain.Album;
//import ecsimsw.picup.album.domain.AlbumRepository;
//import ecsimsw.picup.album.domain.Picture;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.context.TestPropertySource;
//
//@TestPropertySource(locations = "/databaseConfig.properties")
//@DataJpaTest
//public class AlbumRepositoryTest {
//
//    @Autowired
//    private AlbumRepository albumRepository;
//
//    @DisplayName("Album 정보를 저장한다.")
//    @Test
//    public void save() {
//        var album = albumRepository.save(new Album(MEMBER_ID, ALBUM_NAME, RESOURCE_KEY, 0L);
//        assertAll(
//            () -> assertThat(album.getId()).isNotNull(),
//            () -> assertThat(album.getName()).isEqualTo(ALBUM_NAME),
//            () -> assertThat(album.getResourceKey()).isEqualTo(RESOURCE_KEY),
//            () -> assertThat(album.getCreatedAt()).isNotNull()
//        );
//    }
//
//    @DisplayName("유효하지 않은 Album 정보 저장시 예외를 반환한다.")
//    @Test
//    public void saveInvalid() {
//        assertThatThrownBy(
//            () -> albumRepository.save(new Album(null, ALBUM_NAME, RESOURCE_KEY, 0L))
//        );
//        assertThatThrownBy(
//            () -> albumRepository.save(new Album(MEMBER_ID, ALBUM_NAME, RESOURCE_KEY, -1L))
//        );
//        assertThatThrownBy(
//            () -> albumRepository.save(new Album(MEMBER_ID, ALBUM_NAME, null, 0L))
//        );
//    }
//
//    @DisplayName("앨범내에서 생성 일자를 기준으로 조건 검색할 수 있다.")
//    @Test
//    public void cursorBasedFetch() {
//        var picture1 = albumRepository.save(ALBUM());
//        var picture2 = albumRepository.save(ALBUM());
//        var picture3 = albumRepository.save(ALBUM());
//
//        var result1 = albumRepository.findAllByAlbumOrderThan(
//            savedAlbum.getId(),
//            picture2.getId(),
//            picture2.getCreatedAt(),
//            PageRequest.of(0,10)
//        );
//        assertThat(result1).contains(picture1);
//
//        var result2 = pictureRepository.findAllByAlbumOrderThan(
//            savedAlbum.getId(),
//            picture3.getId(),
//            picture3.getCreatedAt(),
//            PageRequest.of(0,10, Direction.DESC, Picture_.CREATED_AT)
//        );
//        assertThat(result2).contains(picture2, picture1);
//
//        var result3 = pictureRepository.findAllByAlbumOrderThan(
//            savedAlbum.getId(),
//            picture3.getId(),
//            picture3.getCreatedAt(),
//            PageRequest.of(0,1, Direction.DESC, Picture_.CREATED_AT)
//        );
//        assertThat(result3).contains(picture2);
//    }
//}
