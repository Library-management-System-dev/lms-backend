package com.krish.service.Impl;

import com.krish.domain.BookLoanStatus;
import com.krish.mapper.BookReviewMapper;
import com.krish.modal.Book;
import com.krish.modal.BookLoan;
import com.krish.modal.BookReview;
import com.krish.modal.User;
import com.krish.payload.dto.BookReviewDTO;
import com.krish.payload.request.CreateReviewRequest;
import com.krish.payload.request.UpdateReviewRequest;
import com.krish.payload.response.PageResponse;
import com.krish.repository.BookLoanRepository;
import com.krish.repository.BookRepository;
import com.krish.repository.BookReviewRepository;
import com.krish.service.BookReviewService;
import com.krish.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookReviewServiceImpl implements BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final UserService userService;
    private final BookRepository bookRepository;
    private final BookReviewMapper bookReviewMapper;
    private final BookLoanRepository bookLoanRepository;


    @Override
    public BookReviewDTO createReview(CreateReviewRequest request) throws Exception {

        //        1.fetch the loggedin user
        User user = userService.getCurrentUser();

//        2.validate book exist
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new Exception("book not found!"));

//        3.check if user has already reviewd the book
        if(bookReviewRepository.existsByUserIdAndBookId(user.getId(), book.getId())){
            throw new Exception("You have already reviewed this book!");
        }

//        4.check user has read the book
        boolean hasReadBook = hasUserReadBook(user.getId(), book.getId());
        if(!hasReadBook){
            throw new Exception("You can only review books you have read!");
        }
//        5.create the review
        BookReview bookReview = new BookReview();
        bookReview.setUser(user);
        bookReview.setBook(book);
        bookReview.setRating(request.getRating());
        bookReview.setReviewText(request.getReviewText());
        bookReview.setTitle(request.getTitle());
        BookReview savedBookReview = bookReviewRepository.save(bookReview);
        return bookReviewMapper.toDTO(savedBookReview);
    }



    @Override
    public BookReviewDTO updateReview(Long reviewId, UpdateReviewRequest request) throws Exception {

        //        1.fetch the loggedin user
        User user = userService.getCurrentUser();

//        2.fin the review
        BookReview bookReview = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception("Review not found!"));

//        3.check if logged user is the owner of the review
        if(!bookReview.getUser().getId().equals(user.getId())){
            throw new Exception("You havenot reviewd this book!");
        }

//        4.update review
        bookReview.setReviewText(request.getReviewText());
        bookReview.setTitle(request.getTitle());
        bookReview.setRating(request.getRating());
        BookReview savedBookReview = bookReviewRepository.save(bookReview);
        return bookReviewMapper.toDTO(savedBookReview);

    }

    @Override
    public BookReviewDTO deleteReview(Long reviewId) throws Exception {

        User currentUser = userService.getCurrentUser();

//        1.find the review
        BookReview bookReview = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception("Review not found with id: " + reviewId));
//        2.check if the current user is the owner of the review
        if(!bookReview.getUser().getId().equals(currentUser.getId())){
            throw new Exception("you can only delete your own review!");
        }

        BookReviewDTO dto = bookReviewMapper.toDTO(bookReview);
        bookReviewRepository.delete(bookReview);
        return dto;
    }


    @Override
    public PageResponse<BookReviewDTO> getReviewsByBookId(Long id, int page, int size) throws Exception {
            Book book = bookRepository.findById(id)
                    .orElseThrow(() -> new Exception("Book not found by id!"));
            Pageable pageable = PageRequest.of(page,size,
                    Sort.by("createdAt").descending());

            Page<BookReview> reviewPage = bookReviewRepository.findByBook(book,pageable);
            return convertToPageResponse(reviewPage);
    }

    private PageResponse<BookReviewDTO> convertToPageResponse(Page<BookReview> reviewPage) {
        return new PageResponse<>(
                reviewPage.getContent().stream()
                        .map(bookReviewMapper::toDTO)
                        .collect(Collectors.toList()),
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isLast(),
                reviewPage.isFirst(),
                reviewPage.isEmpty()
        );
    }

    private boolean hasUserReadBook(Long userId, Long bookId) {
        List<BookLoan> bookLoans=bookLoanRepository.findByBookId(bookId);
        return bookLoans.stream()
                .anyMatch(loan->loan.getUser().getId().equals(userId) &&
                        loan.getStatus()== BookLoanStatus.RETURNED);
    }
}
