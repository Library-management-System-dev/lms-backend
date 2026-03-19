package com.krish.service.Impl;

import com.krish.mapper.WishlistMapper;
import com.krish.modal.Book;
import com.krish.modal.User;
import com.krish.modal.Wishlist;
import com.krish.payload.dto.WishListDTO;
import com.krish.payload.response.PageResponse;
import com.krish.repository.BookRepository;
import com.krish.repository.WishlistRepository;
import com.krish.service.UserService;
import com.krish.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishListService {

    private final WishlistRepository wishlistRepository;
    private final UserService userService;
    private final BookRepository bookRepository;
    private final WishlistMapper wishlistmapper;

    @Override
    public WishListDTO addToWishlist(Long bookId, String notes) throws Exception {
        User user = userService.getCurrentUser();

//        1.validate book exist
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new Exception("Book not found"));
//        2.check if book is already in wishlist
        if(wishlistRepository.existsByUserIdAndBookId(user.getId(), bookId)){
            throw new Exception("Book is already in your wishlist");
        }
//        3. create the wishlist
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setBook(book);
        wishlist.setNotes(notes);
        Wishlist saved=wishlistRepository.save(wishlist);
        return wishlistmapper.toDTO(saved);
    }

    @Override
    public void removeFromWishlist(Long BookId) throws Exception {
        User user = userService.getCurrentUser();

        Wishlist wishlist = wishlistRepository.findByUserIdAndBookId(user.getId(), BookId);

        if(wishlist==null){
            throw new Exception("Book is not in your wishlist");
        }
        wishlistRepository.delete(wishlist);

    }

    @Override
    public PageResponse<WishListDTO> getMyWishlist(int page, int size) throws Exception {
        Long userId = userService.getCurrentUser().getId();
        Pageable pageable = PageRequest.of(page,
                size, Sort.by("addedAt").descending());

        Page<Wishlist> wishlistPage = wishlistRepository.findByUserId(userId, pageable);
        return convertToPageResponse(wishlistPage);
    }

    private PageResponse<WishListDTO> convertToPageResponse(Page<Wishlist> wishlistPage){
        List<WishListDTO> wishListDTOs = wishlistPage.getContent()
                .stream()
                .map(wishlistmapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                wishListDTOs,
                wishlistPage.getNumber(),
                wishlistPage.getSize(),
                wishlistPage.getTotalElements(),
                wishlistPage.getTotalPages(),
                wishlistPage.isLast(),
                wishlistPage.isFirst(),
                wishlistPage.isEmpty()
        );
    }
}
