package com.krish.service.Impl;

import com.krish.domain.BookLoanStatus;
import com.krish.domain.ReservationStatus;
import com.krish.domain.UserRole;
import com.krish.mapper.ReservationMapper;
import com.krish.modal.Book;
import com.krish.modal.Reservation;
import com.krish.modal.User;
import com.krish.payload.dto.ReservationDTO;
import com.krish.payload.request.CheckoutRequest;
import com.krish.payload.request.ReservationRequest;
import com.krish.payload.request.ReservationSearchRequest;
import com.krish.payload.response.PageResponse;
import com.krish.repository.BookLoanRepository;
import com.krish.repository.BookRepository;
import com.krish.repository.ReservationRepository;
import com.krish.service.BookLoanService;
import com.krish.service.BookService;
import com.krish.service.ReservationService;
import com.krish.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final BookLoanRepository bookLoanRepository;
    private final UserService userService;
    private final BookRepository bookRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final BookLoanService bookLoanService;

    int MAX_RESERVATIONS = 5;
    private static final EnumSet<ReservationStatus> ACTIVE_RESERVATION_STATUSES =
            EnumSet.of(ReservationStatus.PENDING, ReservationStatus.AVAILABLE);

    @Override
    public ReservationDTO createReservation(ReservationRequest reservationRequest) throws Exception {
        User user = userService.getCurrentUser();
        return createReservationForUser(reservationRequest, user.getId());
    }

    @Override
    public ReservationDTO createReservationForUser(ReservationRequest reservationRequest, Long userId) throws Exception {
        boolean alreadyHasLoan = bookLoanRepository.existsByUserIdAndBookIdAndStatus(
                userId, reservationRequest.getBookId(), BookLoanStatus.CHECKED_OUT
        );
        if(alreadyHasLoan){
            throw new Exception("you already have an active loan for this book");
        }
//        1.validate user exists
        User user = userService.findById(userId);

//        2.validate book exists
        Book book = bookRepository.findById(reservationRequest.getBookId())
                .orElseThrow(() -> new Exception("Book not found"));

//        3.
        if(reservationRepository.hasActiveReservation(userId, book.getId(), ACTIVE_RESERVATION_STATUSES)){
            throw new Exception("you already have reservation on this book");
        }
//        4.check if book is already available
        if(book.getAvailableCopies() > 0){
            throw new Exception("book is already available");
        }
//        5.check users active reservation limit
        long activeReservations = reservationRepository.countActiveReservationsByUser(userId, ACTIVE_RESERVATION_STATUSES);
        if(activeReservations >= MAX_RESERVATIONS){
            throw new Exception("you have reserved "+MAX_RESERVATIONS+" times");
        }

//        6.create reservation
        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setUser(user);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setNotificationSent(false);
        reservation.setNotes(reservationRequest.getNotes());

        long pendingCount = reservationRepository.countPendingReservationsByBook(book.getId(), ReservationStatus.PENDING);
        reservation.setQueuePosition((int) pendingCount + 1);

        Reservation savedReservation = reservationRepository.save(reservation);

        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public ReservationDTO cancelReservation(Long reservationId) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new Exception("Reservation not found with ID: "+reservationId));

        User currentUser = userService.getCurrentUser();
        if(!reservation.getUser().getId().equals(currentUser.getId())
                 && currentUser.getRole()!= UserRole.ROLE_ADMIN
        ){
            throw new Exception("you can only only cancel your own reservations");
        }
        if(!reservation.canBeCancelled()){
            throw new Exception("this reservation cannot be cancelled(current status: "+reservation.getStatus()+")");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());

        Reservation savedReservation = reservationRepository.save(reservation);

//        updateQueuePositions(reservation.getBook().getId());

        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public ReservationDTO fulfillReservation(Long reservationId) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(()-> new Exception("Reservation not found with ID: "+reservationId));

        if(reservation.getBook().getAvailableCopies()<=0){
            throw new Exception("book is not available for pickup (current available copies: "+reservation.getBook().getAvailableCopies()+")");
        }
        reservation.setStatus(ReservationStatus.AVAILABLE);
        reservation.setFulfilledAt(LocalDateTime.now());

        Reservation savedReservation = reservationRepository.save(reservation);

        CheckoutRequest request = new CheckoutRequest();
        request.setBookId(reservation.getBook().getId());
        request.setNotes("Assign booked by Admin");

        bookLoanService.checkoutBookForUser(reservation.getUser().getId(),request);
        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public PageResponse<ReservationDTO> getMyReservations(ReservationSearchRequest searchRequest) throws Exception {
        User user = userService.getCurrentUser();
        searchRequest.setUserId(user.getId());
        return searchReservations(searchRequest);
    }

    @Override
    public PageResponse<ReservationDTO> searchReservations(ReservationSearchRequest searchRequest) {
        Pageable pageable = createPageable(searchRequest);

        Page<Reservation> reservationPage = reservationRepository.searchReservationWithFilters(
                searchRequest.getUserId(),
                searchRequest.getBookId(),
                searchRequest.getStatus(),
                searchRequest.getActiveOnly()!=null? searchRequest.getActiveOnly():false,
                ACTIVE_RESERVATION_STATUSES,
                pageable
        );

        return buildPageResponse(reservationPage);
    }

    private PageResponse<ReservationDTO> buildPageResponse(Page<Reservation> reservationPage){
        List<ReservationDTO> dtos = reservationPage.getContent().stream()
                .map(reservationMapper::toDTO)
                .toList();
        PageResponse<ReservationDTO> response = new PageResponse<>();
        response.setContent(dtos);
        response.setPageNumber(reservationPage.getNumber());
        response.setPageSize(reservationPage.getSize());
        response.setTotalElements(reservationPage.getTotalElements());
        response.setTotalPages(reservationPage.getTotalPages());
        response.setLast(reservationPage.isLast());
        response.setFirst(reservationPage.isFirst());
        response.setEmpty(reservationPage.isEmpty());

        return response;
    }

    private Pageable createPageable(ReservationSearchRequest searchRequest){
        Sort sort = "ASC".equalsIgnoreCase(searchRequest.getSortDirection())
                ? Sort.by(searchRequest.getSortBy()).ascending()
                : Sort.by(searchRequest.getSortBy()).descending();

        return PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
    }
}
