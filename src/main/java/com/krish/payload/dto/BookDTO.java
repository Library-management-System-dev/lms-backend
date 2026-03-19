package com.krish.payload.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookDTO {

    private Long id;

    @NotBlank(message = "Title is mandatory")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    @NotBlank(message = "Author is mandatory")
    @Size(min = 1, max = 255, message = "Author name must be between 1 and 255 characters")
    private String author;

    @NotBlank(message = "ISBN is mandatory")
    private String isbn;

    @NotNull(message = "Genre is mandatory")
    private Long genreId;

    private String genreName;

    @Size(max = 100, message = "Publisher name cannot exceed 100 characters")
    private String publisher;

    @Size(max = 20, message = "Description cannot exceed 20 characters")
    private String language;

    private String genreCode;

    private LocalDateTime publicationDate;

    @Min(value = 1, message = "Pages must be at least 1")
    @Max(value = 50000, message = "Pages cannot exceed 50000")
    private Integer pages;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Min(value = 0, message = "Total copies cannot be negative")
    @NotNull(message = "Total copies is mandatory")
    private Integer totalCopies;

    @Min(value = 0, message = "Available copies cannot be negative")
    @NotNull(message = "Available copies is mandatory")
    private Integer availableCopies;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Price must be a valid monetary amount")
    private BigDecimal price;

    @Size(max = 500, message = "Cover image URL cannot exceed 500 characters")
    private String coverImageUrl;

    private Boolean alreadyHaveLoan;

    private Boolean alreadyHaveReservation;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
