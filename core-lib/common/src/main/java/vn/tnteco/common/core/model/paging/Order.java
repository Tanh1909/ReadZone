package vn.tnteco.common.core.model.paging;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jooq.Field;
import vn.tnteco.common.utils.StringUtils;

@Data
@Accessors(chain = true)
public class Order {

    @Schema(title = "tên trường cần sort")
    private String property;

    @Schema(title = "asc: tăng dần, desc: giảm dần")
    private String direction;

    public Order() {
    }

    public Order(String property, String direction) {
        this.property = StringUtils.toSnakeCase(property);
        this.direction = direction;
    }

    public <T> Order(Field<T> field, Direction direction) {
        this.property = field.getName();
        this.direction = direction.name();
    }

    public Order setProperty(String property) {
        this.property = StringUtils.toSnakeCase(property);
        return this;
    }

    public enum Direction {
        ASC, DESC;
    }
}
