package com.krish.service;

import com.krish.domain.BookLoanStatus;
import com.krish.payload.dto.BookLoanDTO;
import com.krish.payload.request.BookLoanSearchRequest;
import com.krish.payload.request.CheckinRequest;
import com.krish.payload.request.CheckoutRequest;
import com.krish.payload.request.RenewalRequest;
import com.krish.payload.response.PageResponse;

public interface BookLoanService {

    BookLoanDTO checkoutBook(CheckoutRequest checkoutRequest) throws Exception;

    BookLoanDTO checkoutBookForUser(Long userId,CheckoutRequest checkoutRequest) throws Exception;

    BookLoanDTO checkinBook(CheckinRequest checkinRequest) throws Exception;

    BookLoanDTO renewCheckout(RenewalRequest renewalRequest) throws Exception;

    PageResponse<BookLoanDTO> getMyBookLoans(BookLoanStatus status,
                                             int page, int size) throws Exception;

    PageResponse<BookLoanDTO> getBookLoans(BookLoanSearchRequest request);

    int updateOverdueBookLoan();

}
