package com.krish.service.Impl;

import com.krish.domain.BookLoanStatus;
import com.krish.domain.BookLoanType;
import com.krish.exception.BookException;
import com.krish.mapper.BookLoanMapper;
import com.krish.modal.Book;
import com.krish.modal.BookLoan;
import com.krish.modal.Subscription;
import com.krish.modal.User;
import com.krish.payload.dto.BookDTO;
import com.krish.payload.dto.BookLoanDTO;
import com.krish.payload.dto.SubscriptionDTO;
import com.krish.payload.request.BookLoanSearchRequest;
import com.krish.payload.request.CheckinRequest;
import com.krish.payload.request.CheckoutRequest;
import com.krish.payload.request.RenewalRequest;
import com.krish.payload.response.PageResponse;
import com.krish.repository.BookLoanRepository;
import com.krish.repository.BookRepository;
import com.krish.service.BookLoanService;
import com.krish.service.SubscriptionService;
import com.krish.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookLoanServiceImpl implements BookLoanService {

    private final BookLoanRepository bookLoanRepository;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final BookRepository bookRepository;
    private final BookLoanMapper bookLoanMapper;
    private static final EnumSet<BookLoanStatus> ACTIVE_BOOK_LOAN_STATUSES =
            EnumSet.of(BookLoanStatus.CHECKED_OUT, BookLoanStatus.OVERDUE);


    @Override
    public BookLoanDTO checkoutBook(CheckoutRequest checkoutRequest) throws Exception {

        User user = userService.getCurrentUser();


        return checkoutBookForUser(user.getId(), checkoutRequest);
    }

    @Override
    public BookLoanDTO checkoutBookForUser(Long userId, CheckoutRequest checkoutRequest) throws Exception {
        //1.validate user exist
        User user = userService.findById(userId);

        //2. validate user has active subscription or not
        SubscriptionDTO subscription = subscriptionService
                .getUsersActiveSubscription(user.getId());

        //3. validate book exists and is available for checkout
        Book book = bookRepository.findById(checkoutRequest.getBookId())
                .orElseThrow(() -> new BookException("Book not found with id : "+checkoutRequest.getBookId()));

        if(!book.getActive()){
            throw new BookException("Book is not active");
        }
        if(book.getAvailableCopies()<=0){
            throw new BookException("Book is not available");
        }

        //4.check if user already has this book checkout
        if(bookLoanRepository.hasActiveCheckout(userId, book.getId(), ACTIVE_BOOK_LOAN_STATUSES)){
            throw new BookException("book already has active checkout");
        }
        //5. check users active checkout limit
        long activeCheckouts=bookLoanRepository.countActiveBookLoansByUser(userId, ACTIVE_BOOK_LOAN_STATUSES);
        int maxBookAllowed = subscription.getMaxBooksAllowed();;

        if(activeCheckouts>=maxBookAllowed){
            throw new Exception("User has reached your maximum number of books allowed");
        }

        //6.check for overdue books
        long overdueCount = bookLoanRepository.countOverdueBookLoansByUser(userId, BookLoanStatus.OVERDUE);
        if(overdueCount>0){
            throw new Exception("first return old overdue book!");
        }
        // 7. create book loan
        BookLoan bookLoan= BookLoan
                .builder()
                .user(user)
                .book(book)
                .type(BookLoanType.CHECKOUT)
                .status(BookLoanStatus.CHECKED_OUT)
                .checkoutDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(checkoutRequest.getCheckoutDays()))
                .renewalCount(0)
                .maxRenewals(2)
                .notes(checkoutRequest.getNotes())
                .isOverdue(false)
                .overdueDays(0)
                .build();

        //8.update book available copy
        book.setAvailableCopies(book.getAvailableCopies()-1);
        bookRepository.save(book);

        //9. save book loan
        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);

        return bookLoanMapper.toDTO(savedBookLoan);
    }

    @Override
    public BookLoanDTO checkinBook(CheckinRequest checkinRequest) throws Exception {

        //1.validate book loan exist
        BookLoan bookLoan = bookLoanRepository.findById(checkinRequest.getBookLoanId())
                .orElseThrow(() -> new Exception("Book loan not found!"));

        //2.check if already returned
        if(!bookLoan.isActive()){
            throw new BookException("book loan is not active");
        }

        //3.set return date
        bookLoan.setReturnDate(LocalDate.now());

        //4.
        BookLoanStatus condition=checkinRequest.getCondition();
        if(condition==null){
            condition=BookLoanStatus.RETURNED;
        }
        bookLoan.setStatus(condition);

        //5.fine todo
        bookLoan.setOverdueDays(0);
        bookLoan.setIsOverdue(false);
        //6.
        bookLoan.setNotes("book returned by user");
        //7.update book availablity

        if(condition!=BookLoanStatus.LOST){
            Book book = bookLoan.getBook();
            book.setAvailableCopies(book.getAvailableCopies()+1);
            bookRepository.save(book);
        }

        //process next reservation todo

        //8.save book loan
        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);
        return bookLoanMapper.toDTO(savedBookLoan);
    }

    @Override
    public BookLoanDTO renewCheckout(RenewalRequest renewalRequest) throws Exception {

        //1.validate book loan exist
        BookLoan bookLoan = bookLoanRepository.findById(renewalRequest.getBookLoanId())
                .orElseThrow(() -> new Exception("Book loan not found!"));

        //2.check if can be renewed or not
        if(!bookLoan.canRenew()){
            throw new BookException("Book cannot be renewed!");
        }
        //3.update due date
        bookLoan.setDueDate(bookLoan.getDueDate().plusDays(renewalRequest.getExtensionDays()));
        bookLoan.setRenewalCount(bookLoan.getRenewalCount()+1);

        bookLoan.setNotes("book renewed by user");

        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);
        return bookLoanMapper.toDTO(savedBookLoan);
    }

    @Override
    public PageResponse<BookLoanDTO> getMyBookLoans(BookLoanStatus status, int page, int size) throws Exception {
        User currentUser=userService.getCurrentUser();
        Page<BookLoan> bookLoanPage;

        if(status!=null){
            Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
            bookLoanPage = bookLoanRepository.findByStatusAndUser(status,currentUser, pageable);
        }else {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            bookLoanPage = bookLoanRepository.findByUserId(currentUser.getId(), pageable);
        }

        return convertToPageResponse(bookLoanPage);
    }

    @Override
    public PageResponse<BookLoanDTO> getBookLoans(BookLoanSearchRequest searchRequest) {
        int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
        int size = searchRequest.getSize() != null ? searchRequest.getSize() : 10;
        String sortBy = searchRequest.getSortBy() != null ? searchRequest.getSortBy() : "createdAt";
        String sortDirection = searchRequest.getSortDirection() != null ? searchRequest.getSortDirection() : "DESC";

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        Page<BookLoan> bookLoanPage = findBookLoans(searchRequest, pageable);
        return convertToPageResponse(bookLoanPage);
    }

    private Page<BookLoan> findBookLoans(BookLoanSearchRequest searchRequest, Pageable pageable) {
        if(Boolean.TRUE.equals(searchRequest.getOverDueOnly())) {
            return bookLoanRepository.findOverdueBookLoans(LocalDate.now(), ACTIVE_BOOK_LOAN_STATUSES, pageable);
        } else if (searchRequest.getUserId()!=null) {
            return bookLoanRepository.findByUserId(searchRequest.getUserId(), pageable);
        } else if (searchRequest.getBookId()!=null) {
            return bookLoanRepository.findByBookId(searchRequest.getBookId(), pageable);
        } else if (searchRequest.getStatus()!=null) {
            return bookLoanRepository.findByStatus(searchRequest.getStatus(), pageable);
        } else if (searchRequest.getStartDate()!=null && searchRequest.getEndDate()!=null) {
            return bookLoanRepository.findBookLoansByDateRange(
                    searchRequest.getStartDate(),
                    searchRequest.getEndDate(),
                    pageable
            );
        }
        return bookLoanRepository.findAll(pageable);
    }

    @Override
    public int updateOverdueBookLoan() {
        Pageable pageable = PageRequest.of(0,1000);
        Page<BookLoan> overduePage = bookLoanRepository
                .findOverdueBookLoans(LocalDate.now(), ACTIVE_BOOK_LOAN_STATUSES, pageable);

        int updatedCount=0;
        for(BookLoan bookLoan: overduePage.getContent()){
            if(bookLoan.getStatus() == BookLoanStatus.CHECKED_OUT){
                bookLoan.setStatus(BookLoanStatus.OVERDUE);
                bookLoan.setIsOverdue(true);

               int overdueDays = calculateOverdueDate(bookLoan.getDueDate(), LocalDate.now());

//                BigDecimal fine = fineCalculationService.calculateOverdueFine(bookLoan);

                bookLoanRepository.save(bookLoan);
                updatedCount++;
            }
        }

        return  updatedCount;
    }

    private Pageable createPageable(int page,int size,String sortBy,String sortDirection){
        size = Math.min(size,100);
        size = Math.max(size,1);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return PageRequest.of(page,size,sort);
    }

    private PageResponse<BookLoanDTO> convertToPageResponse(Page<BookLoan> bookLoanPage){
        List<BookLoanDTO> bookLoanDTOs = bookLoanPage.getContent()
                .stream()
                .map(bookLoanMapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                bookLoanDTOs,
                bookLoanPage.getNumber(),
                bookLoanPage.getSize(),
                bookLoanPage.getTotalElements(),
                bookLoanPage.getTotalPages(),
                bookLoanPage.isLast(),
                bookLoanPage.isFirst(),
                bookLoanPage.isEmpty()
        );
    }

    public  int calculateOverdueDate(LocalDate dueDate, LocalDate today){
        if(today.isBefore(dueDate) || today.isEqual(dueDate)){
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate,today);
    }
}


