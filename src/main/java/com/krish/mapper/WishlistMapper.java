package com.krish.mapper;

import com.krish.modal.Wishlist;
import com.krish.payload.dto.WishListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WishlistMapper {

    private final BookMapper bookMapper;

    public WishListDTO toDTO(Wishlist wishlist){
        if(wishlist==null){
            return null;
        }

        WishListDTO dto = new WishListDTO();
        dto.setId(wishlist.getId());

        if(wishlist.getUser()!=null){
            dto.setUserId(wishlist.getUser().getId());
            dto.setUserFullName(wishlist.getUser().getFullName());
        }

        if(wishlist.getBook()!=null){
            dto.setBook(bookMapper.toDTO(wishlist.getBook()));
        }

        dto.setAddedAt(wishlist.getAddedAt());
        dto.setNotes(wishlist.getNotes());

        return dto;
    }
}
