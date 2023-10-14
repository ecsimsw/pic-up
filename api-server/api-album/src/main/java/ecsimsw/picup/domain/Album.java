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

    private String resourceKey;

    private int orderNumber;

    public Album() {
    }

    public Album(Long id, Long userId, String name, String resourceKey, int orderNumber) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.resourceKey = resourceKey;
        this.orderNumber = orderNumber;
    }

    public Album(Long userId, String name, String resourceKey, int orderNumber) {
        this(null, userId, name, resourceKey, orderNumber);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateThumbnail(String thumbnailImage) {
        this.resourceKey = thumbnailImage;
    }

    public void updateOrder(int newOrder) {
        this.orderNumber = newOrder;
    }
}
