package com.krish.controller;

import com.krish.modal.Wishlist;
import com.krish.payload.dto.WishListDTO;
import com.krish.payload.response.ApiResponse;
import com.krish.payload.response.PageResponse;
import com.krish.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishListService wishListService;

    @PostMapping("/add/{bookId}")
    public ResponseEntity<?> addToWishList(
            @PathVariable Long bookId,
            @RequestParam(required = false) String notes
    ) throws Exception {
        WishListDTO wishListDTO = wishListService.addToWishlist(bookId, notes);
        return ResponseEntity.ok(wishListDTO);
    }

    @DeleteMapping("/remove/{bookId}")
    public ResponseEntity<ApiResponse> removeFromWishList(
            @PathVariable Long bookId
    ) throws Exception {
        wishListService.removeFromWishlist(bookId);
        return ResponseEntity.ok(new ApiResponse("Book removed from wishlist", true));
    }

    @GetMapping("/my-wishlist")
    public ResponseEntity<?> getMyWishlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws Exception {
        PageResponse<WishListDTO> wishlist = wishListService.getMyWishlist(page, size);
        return ResponseEntity.ok(wishlist);
    }
}
