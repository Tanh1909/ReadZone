package vn.tnteco.repository.data.audit;

import java.time.LocalDateTime;

public interface SoftDeletable {

    LocalDateTime getDeletedAt();

    default boolean isDeleted() {
        return getDeletedAt() != null;
    }

}
