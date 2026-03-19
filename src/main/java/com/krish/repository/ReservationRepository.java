package com.krish.repository;

import com.krish.domain.ReservationStatus;
import com.krish.modal.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {




    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reservation r " +
           "WHERE r.user.id = :userId AND r.book.id = :bookId " +
           "AND r.status in :activeStatuses")
    boolean hasActiveReservation(
            @Param("userId") Long userId,
            @Param("bookId") Long bookId,
            @Param("activeStatuses") Collection<ReservationStatus> activeStatuses
    );



    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = :userId " +
           "AND r.status in :activeStatuses")
    long countActiveReservationsByUser(
            @Param("userId") Long userId,
            @Param("activeStatuses") Collection<ReservationStatus> activeStatuses
    );



    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.book.id = :bookId " +
           "AND r.status = :status")
    long countPendingReservationsByBook(
            @Param("bookId") Long bookId,
            @Param("status") ReservationStatus status
    );








    @Query("SELECT r FROM Reservation r WHERE " +
            "(:userId IS NULL OR r.user.id = :userId) AND " +
            "(:bookId IS NULL OR r.book.id = :bookId) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:activeOnly = false OR r.status in :activeStatuses)")
    Page<Reservation> searchReservationWithFilters(
            @Param("userId") Long userId,
            @Param("bookId") Long bookId,
            @Param("status") ReservationStatus status,
            @Param("activeOnly") boolean activeOnly,
            @Param("activeStatuses") Collection<ReservationStatus> activeStatuses,
            Pageable pageable
    );


}
