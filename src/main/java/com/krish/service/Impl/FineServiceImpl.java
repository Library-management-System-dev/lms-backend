package com.krish.service.Impl;

import com.krish.domain.FineStatus;
import com.krish.domain.FineType;
import com.krish.domain.PaymentGateway;
import com.krish.domain.PaymentType;
import com.krish.mapper.FineMapper;
import com.krish.modal.BookLoan;
import com.krish.modal.Fine;
import com.krish.modal.User;
import com.krish.payload.dto.FineDTO;
import com.krish.payload.request.CreateFineRequest;
import com.krish.payload.request.PaymentInitiateRequest;
import com.krish.payload.request.WaiveFineRequest;
import com.krish.payload.response.PageResponse;
import com.krish.payload.response.PaymentInitiateResponse;
import com.krish.repository.BookLoanRepository;
import com.krish.repository.FineRepository;
import com.krish.service.FineService;
import com.krish.service.PaymentService;
import com.krish.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {
    private final BookLoanRepository bookLoanRepository;
    private final FineRepository fineRepository;
    private final FineMapper fineMapper;
    private final UserService userService;
    private final PaymentService paymentService;

    @Override
    public FineDTO createFine(CreateFineRequest createFineRequest) {

        //1.validate book loan exist
        BookLoan bookLoan = bookLoanRepository.findById(createFineRequest.getBookLoanId())
                .orElseThrow(() -> new RuntimeException("Book loan doesn't exist"));

        //2.create fine

        Fine fine = Fine.builder()
                .bookLoan(bookLoan)
                .user(bookLoan.getUser())
                .type(createFineRequest.getFineType())
                .amount(createFineRequest.getAmount())
                .status(FineStatus.PENDING)
                .reason(createFineRequest.getReason())
                .notes(createFineRequest.getNotes())
                .build();
        Fine savedFine = fineRepository.save(fine);
        return fineMapper.toDTO(savedFine);
    }

    @Override
    public PaymentInitiateResponse payFine(Long fineId, String transactionId) throws Exception {

        //1.validate fine exist
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(()-> new Exception("Fine doesn't exist"));

        //2.check already paid
        if(fine.getStatus().equals(FineStatus.PAID)) {
            throw new Exception("Fine is already paid");
        }
        if(fine.getStatus().equals(FineStatus.WAIVED)){
            throw new Exception("Fine is waived");
        }

        //3.initiate the payment
        User user = userService.getCurrentUser();

        PaymentInitiateRequest request= PaymentInitiateRequest
                .builder()
                .userId(user.getId())
                .fineId(fine.getId())
                .paymentType(PaymentType.FINE)
                .gateway(PaymentGateway.RAZORPAY)
                .amount(fine.getAmount())
                .description("library fine payment")
                .build();
        return paymentService.initiatePayment(request);
    }

    @Override
    public void markFineAsPaid(Long fineId, Long amount, String transactionId) throws Exception {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(()-> new Exception("Fine not found with id: " + fineId));

        log.info("Marking fine as paid - Fine ID: {}, Amount: {}", fineId, amount);
        
        fine.applyPayment(amount);
        fine.setTransactionId(transactionId);
        fine.setStatus(FineStatus.PAID);
        fine.setUpdatedAt(LocalDateTime.now());

        fineRepository.save(fine);
    }

    @Override
    public FineDTO waiveFine(WaiveFineRequest waiveFineRequest) throws Exception {
        Fine fine = fineRepository.findById(waiveFineRequest.getFineId())
                .orElseThrow(()-> new Exception("Fine doesn't exist"));

        if(fine.getStatus() == FineStatus.WAIVED){
            throw new Exception("Fine is already waived");
        }
        if(fine.getStatus() == FineStatus.PAID){
            throw new Exception("Fine is already paid");
        }

        User currentAdmin = userService.getCurrentUser();
        fine.waive(currentAdmin, waiveFineRequest.getReason());

        Fine savedFine = fineRepository.save(fine);
        return fineMapper.toDTO(savedFine);
    }

    @Override
    public List<FineDTO> getMyFines(FineStatus status, FineType type) throws Exception {

        User currentUser = userService.getCurrentUser();
        List<Fine> fines;

        if(status != null && type != null) {
            fines = fineRepository.findByUserId(currentUser.getId()).stream()
                    .filter(f -> f.getStatus() == status && f.getType() == type)
                    .collect(Collectors.toList());
        } else if (status != null) {
            fines = fineRepository.findByUserId(currentUser.getId()).stream()
                    .filter(f -> f.getStatus() == status)
                    .collect(Collectors.toList());
        } else if (type != null) {
            fines = fineRepository.findByUserIdAndType(currentUser.getId(), type);
        }else {
            fines = fineRepository.findByUserId(currentUser.getId());
        }

        return fines.stream().map(
                fineMapper::toDTO
        ).collect(Collectors.toList());

    }

    @Override
    public PageResponse<FineDTO> getAllFines(FineStatus status, FineType type, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()

        );
        Page<Fine> finePage = fineRepository.findAllWithFilters(
                userId,
                status,
                type,
                pageable
        );
        return convertToPageResponse(finePage);
    }

    private PageResponse<FineDTO> convertToPageResponse(Page<Fine> finePage) {
        List<FineDTO> fineDTOs = finePage.getContent()
                .stream()
                .map(fineMapper::toDTO)
                .collect(Collectors.toList());
        return new PageResponse<>(
                fineDTOs,
                finePage.getNumber(),
                finePage.getSize(),
                finePage.getTotalElements(),
                finePage.getTotalPages(),
                finePage.isLast(),
                finePage.isFirst(),
                finePage.isEmpty()

        );
    }
}
