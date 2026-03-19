package com.krish.mapper;

import com.krish.exception.BookException;
import com.krish.modal.Book;
import com.krish.modal.Genre;
import com.krish.payload.dto.BookDTO;
import com.krish.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class BookMapper {

    private final GenreRepository genreRepository;
    public BookDTO toDTO(Book book){
        if(book == null){
            return null;
        }

        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .genreId(book.getGenre() != null ? book.getGenre().getId() : null)
                .genreName(book.getGenre() != null ? book.getGenre().getName() : null)
                .genreCode(book.getGenre() != null ? book.getGenre().getCode() : null)
                .publisher(book.getPublisher())
                .publicationDate(book.getPublishedDate() != null ? book.getPublishedDate().atStartOfDay() : null)
                .language(book.getLanguage())
                .description(book.getDescription())
                .availableCopies(book.getAvailableCopies())
                .totalCopies(book.getTotalCopies())
                .price(book.getPrice())
                .coverImageUrl(book.getCoverImageUrl())
                .active(book.getActive())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    public Book toEntity(BookDTO dto) throws BookException {
        if(dto == null){
            return null;
        }

        Book book = new Book();
        book.setId(dto.getId());
        book.setIsbn(dto.getIsbn());
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());

        if(dto.getGenreId()!=null){
            Genre genre = genreRepository.findById(dto.getGenreId())
                    .orElseThrow(() -> new BookException("Genre with id: " + dto.getGenreId() + " not found"));
            book.setGenre(genre);
        }

        book.setPublisher(dto.getPublisher());
        if (dto.getPublicationDate() != null) {
            book.setPublishedDate(dto.getPublicationDate().toLocalDate());
        }
        book.setLanguage(dto.getLanguage());
        book.setPages(dto.getPages());
        book.setDescription(dto.getDescription());
        book.setTotalCopies(dto.getTotalCopies());
        book.setAvailableCopies(dto.getAvailableCopies());
        book.setPrice(dto.getPrice());
        book.setCoverImageUrl(dto.getCoverImageUrl());
        if (dto.getActive() != null) {
            book.setActive(dto.getActive());
        }

        return book;
    }

    public void updateEntityFromDTO(BookDTO dto, Book book) throws BookException {
        if(dto == null || book == null){
            return;
        }

        //ISBN should not be updated as it is unique and immutable
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());

        if(dto.getGenreId()!=null){
            Genre genre = genreRepository.findById(dto.getGenreId())
                    .orElseThrow(() -> new BookException("Genre with id: " + dto.getGenreId() + " not found"));
            book.setGenre(genre);
        }

        book.setPublisher(dto.getPublisher());
        if (dto.getPublicationDate() != null) {
            book.setPublishedDate(dto.getPublicationDate().toLocalDate());
        }
        book.setLanguage(dto.getLanguage());
        book.setPages(dto.getPages());
        book.setDescription(dto.getDescription());
        book.setTotalCopies(dto.getTotalCopies());
        book.setAvailableCopies(dto.getAvailableCopies());
        book.setPrice(dto.getPrice());
        book.setCoverImageUrl(dto.getCoverImageUrl());

        if(dto.getActive() != null){
            book.setActive(dto.getActive());
        }
    }
}
