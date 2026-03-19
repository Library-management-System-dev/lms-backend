package com.krish.payload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanDTO {

    private Long id;

    @NotBlank(message = "plan code is mandatory")
    private String planCode;

    @NotBlank(message = "plan name is mandatory")
    private String name;

    private String description;

    @NotNull(message = "duration days is mandatory")
    @Positive(message = "duration days must be a positive integer")
    private Integer durationDays;

    @NotNull(message = "price is mandatory")
    @Positive(message = "price must be a positive integer")
    private Long price;

    private String currency;

    @NotNull(message = "max books allowed is mandatory")
    @Positive(message = "max books allowed must be a positive integer")
    private Integer maxBooksAllowed;

    @NotNull(message = "max days per book is mandatory")
    @Positive(message = "max days per book must be a positive integer")
    private Integer maxDaysPerBook;

    private Integer displayOrder;
    private Boolean isActive;
    private Boolean isFeatured;
    private String badgeText;
    private String adminNotes;



    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;


}
