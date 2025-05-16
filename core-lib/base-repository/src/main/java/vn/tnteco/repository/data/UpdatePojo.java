package vn.tnteco.repository.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import vn.tnteco.repository.builder.UpdateField;

@Getter
@Setter
@Accessors(chain = true)
public class UpdatePojo<ID> {

    private ID id;

    private UpdateField updateField;

}
