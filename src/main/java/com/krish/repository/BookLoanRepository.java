package com.krish.repository;

import com.krish.domain.BookLoanStatus;
import com.krish.modal.Book;
import com.krish.modal.BookLoan;
import com.krish.modal.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookLoanRepository extends JpaRepository<BookLoan,Long> {

    Page<BookLoan> findByUserId(Long userId, Pageable pageable);
    Page<BookLoan> findByStatusAndUser(BookLoanStatus status, User user, Pageable pageable);
    Page<BookLoan> findByStatus(BookLoanStatus status, Pageable pageable);
    Page<BookLoan> findByBookId(Long bookId, Pageable pageable);

    List<BookLoan> findByBookId(Long bookId);


    @Query(" select case when count(bl) > 0 then true else false end from BookLoan bl " +
            " where bl.user.id =:userId and bl.book.id =:bookId "+
            " and bl.status in :activeStatuses"
    )
    boolean hasActiveCheckout(
            @Param("userId") Long userId,
            @Param("bookId") Long bookId,
            @Param("activeStatuses") Collection<BookLoanStatus> activeStatuses
    );

    @Query("SELECT COUNT(bl) FROM BookLoan bl WHERE bl.user.id = :userId " +
            "AND bl.status in :activeStatuses")
    long countActiveBookLoansByUser(
            @Param("userId") Long userId,
            @Param("activeStatuses") Collection<BookLoanStatus> activeStatuses
    );

    @Query("SELECT COUNT(bl) FROM BookLoan bl WHERE bl.user.id = :userId " +
            "AND bl.status = :status ")
    long countOverdueBookLoansByUser(
            @Param("userId") Long userId,
            @Param("status") BookLoanStatus status
    );

    @Query("SELECT bl FROM BookLoan bl WHERE bl.dueDate< :currentDate " +
           "AND bl.status in :activeStatuses ")
    Page<BookLoan> findOverdueBookLoans(
            @Param("currentDate") LocalDate currentDate,
            @Param("activeStatuses") Collection<BookLoanStatus> activeStatuses,
            Pageable pageable
    );

    @Query("SELECT bl FROM BookLoan bl WHERE bl.checkoutDate BETWEEN :startDate AND :endDate")
    Page<BookLoan> findBookLoansByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, BookLoanStatus status);

}
