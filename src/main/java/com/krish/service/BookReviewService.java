package com.krish.service;

import com.krish.payload.dto.BookReviewDTO;
import com.krish.payload.request.CreateReviewRequest;
import com.krish.payload.request.UpdateReviewRequest;
import com.krish.payload.response.PageResponse;

public interface BookReviewService {

    BookReviewDTO createReview(CreateReviewRequest request) throws Exception;

    BookReviewDTO updateReview(Long reviewId, UpdateReviewRequest request) throws Exception;

    BookReviewDTO deleteReview(Long reviewId) throws Exception;

    PageResponse<BookReviewDTO> getReviewsByBookId(Long id, int page, int size) throws Exception;
}
