package com.krish.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaiveFineRequest {

    @NotNull(message = "Fine ID is mandatory")
    private Long fineId;

    @NotNull(message = "Waiver reason is mandatory")
    private String reason;
}
