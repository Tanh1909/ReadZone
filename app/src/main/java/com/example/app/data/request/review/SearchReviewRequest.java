package com.example.app.data.request.review;

import lombok.Getter;
import lombok.Setter;
import vn.tnteco.common.core.model.paging.SearchRequest;

import java.util.Set;

@Getter
@Setter
public class SearchReviewRequest extends SearchRequest {

    private Integer bookId;

    private Set<Integer> starOptions;



}
