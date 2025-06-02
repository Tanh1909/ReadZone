package com.example.app.data.mapper;

import com.example.app.data.message.CreateReviewMessage;
import com.example.app.data.request.review.ReviewRequest;
import com.example.app.data.response.review.ReviewBookResponse;
import com.example.app.data.response.review.ReviewOrderItemResponse;
import com.example.app.data.tables.pojos.Review;
import com.fasterxml.jackson.core.type.TypeReference;
import org.jooq.JSONB;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import vn.tnteco.common.core.json.Json;
import vn.tnteco.common.data.mapper.TimeMapper;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ReviewMapper implements TimeMapper {

    public abstract List<ReviewOrderItemResponse> toReviewOrderItemResponses(List<Review> reviews);

    public abstract Review toPojo(CreateReviewMessage createReviewMessage);

    public abstract void updateToPojo(ReviewRequest reviewRequest, @MappingTarget Review review);

    public abstract List<ReviewBookResponse> toReviewBookResponse(List<Review> reviews);

    public List<String> mapJSONBToList(JSONB jsonb) {
        if (jsonb == null) return Collections.emptyList();
        return Json.decodeValue(jsonb.data(), new TypeReference<List<String>>() {
        });
    }

    public JSONB mapListToJSONB(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return JSONB.valueOf("[]");
        return JSONB.valueOf(Json.encode(imageUrls));
    }

}
