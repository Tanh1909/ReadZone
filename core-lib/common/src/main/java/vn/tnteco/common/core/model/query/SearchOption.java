package vn.tnteco.common.core.model.query;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SearchOption {

    EQUAL,
    LIKE,
    LIKE_REGEX,
    LIKE_IGNORE_CASE,
    LIKE_IGNORE_ACCENT,
    LIKE_IGNORE_CASE_AND_ACCENT;

}
