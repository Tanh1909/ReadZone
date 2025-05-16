package vn.tnteco.repository.data.audit;

import java.time.LocalDateTime;

public interface Auditable {

    LocalDateTime getCreatedAt();

    Integer getCreatedBy();

    LocalDateTime getUpdatedAt();

    Integer getUpdatedBy();

}
