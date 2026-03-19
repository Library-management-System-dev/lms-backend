package com.krish.service;

import com.krish.domain.FineStatus;
import com.krish.domain.FineType;
import com.krish.payload.dto.FineDTO;
import com.krish.payload.request.CreateFineRequest;
import com.krish.payload.request.WaiveFineRequest;
import com.krish.payload.response.PageResponse;
import com.krish.payload.response.PaymentInitiateResponse;

import java.util.List;

public interface FineService {

    FineDTO createFine(CreateFineRequest createFineRequest);

    PaymentInitiateResponse payFine(Long fineId, String transactionId) throws Exception;

    void markFineAsPaid(Long fineId,Long amount, String transactionId) throws Exception;

    FineDTO waiveFine(WaiveFineRequest waiveFineRequest) throws Exception;

    List<FineDTO> getMyFines(FineStatus status, FineType type) throws Exception;

    PageResponse<FineDTO> getAllFines(
            FineStatus status,
            FineType type,
            Long userId,
            int page,
            int size
    );
}
