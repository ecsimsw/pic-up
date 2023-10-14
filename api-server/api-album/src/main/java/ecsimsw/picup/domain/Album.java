package ecsimsw.picup.domain;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Entity
public class Album {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private Long userId;

    private String name;

    private String thumbnailResourceKey;

    private int orderNumber;

    public Album() {
    }

    public Album(Long id, Long userId, String name, String thumbnailResourceKey, int orderNumber) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.thumbnailResourceKey = thumbnailResourceKey;
        this.orderNumber = orderNumber;
    }

    public Album(Long userId, String name, String thumbnailResourceKey, int orderNumber) {
        this(null, userId, name, thumbnailResourceKey, orderNumber);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateThumbnail(String thumbnailImage) {
        this.thumbnailResourceKey = thumbnailImage;
    }

    public void updateOrder(int newOrder) {
        this.orderNumber = newOrder;
    }
}
