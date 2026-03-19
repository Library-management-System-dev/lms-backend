package com.krish.service;

import com.krish.payload.dto.WishListDTO;
import com.krish.payload.response.PageResponse;

public interface WishListService {

    WishListDTO addToWishlist(Long bookId, String notes) throws Exception;

    void removeFromWishlist(Long BookId) throws Exception;

    PageResponse<WishListDTO> getMyWishlist(int page, int size) throws Exception;
}
