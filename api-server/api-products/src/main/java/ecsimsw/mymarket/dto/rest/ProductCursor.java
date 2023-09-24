package ecsimsw.mymarket.dto.rest;

public class ProductCursor {

    private final Long id;
    private final String name;
    private final Integer price;

    public ProductCursor(Long id, String name, Integer price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Integer price() {
        return price;
    }
}
