package vn.tnteco.common.data.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

@Data
@Accessors(chain = true)
public class DeleteResponse {

    private int totalRequested;

    private int totalDeleted;

    private Collection<?> failedIds;

}
