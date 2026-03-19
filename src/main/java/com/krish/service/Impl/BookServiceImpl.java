package com.krish.service.Impl;

import com.krish.exception.BookException;
import com.krish.mapper.BookMapper;
import com.krish.modal.Book;
import com.krish.payload.dto.BookDTO;
import com.krish.payload.request.BookSearchRequest;
import com.krish.payload.response.PageResponse;
import com.krish.repository.BookRepository;
import com.krish.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public BookDTO createBook(BookDTO bookDTO) throws BookException {

        if(bookRepository.existsByIsbn(bookDTO.getIsbn())){
            throw new BookException("Book with the same ISBN "+bookDTO.getIsbn()+ " already exists");
        }
        Book book = bookMapper.toEntity(bookDTO);
        if (!book.isAvailableCopiesValid()) {
            throw new BookException("Available copies cannot exceed total copies");
        }

        Book savedBook = bookRepository.save(book);

        return bookMapper.toDTO(savedBook);
    }

    @Override
    public List<BookDTO> createBooksBulk(List<BookDTO> bookDTOS) {

        List<BookDTO> createdBooks = new ArrayList<>();
        for(BookDTO bookDTO : bookDTOS){
            try {
                BookDTO book = createBook(bookDTO);
                createdBooks.add(book);
            } catch (BookException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return createdBooks;
    }

    @Override
    public BookDTO getBookById(Long bookId) throws BookException {
        Book book= bookRepository.findById(bookId)
                .orElseThrow(()-> new BookException("Book not found"));
        return bookMapper.toDTO(book);
    }

    @Override
    public BookDTO getBookByISBN(String isbn) throws BookException {
        Book book= bookRepository.findByIsbn(isbn)
                .orElseThrow(()-> new BookException("Book not found"));
        return bookMapper.toDTO(book);
    }

    @Override
    public BookDTO updateBook(Long bookId, BookDTO bookDTO) throws BookException {
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException("Book not found"));
        bookMapper.updateEntityFromDTO(bookDTO, existingBook);
        if (!existingBook.isAvailableCopiesValid()) {
            throw new BookException("Available copies cannot exceed total copies");
        }
        Book savedBook = bookRepository.save(existingBook);
        return bookMapper.toDTO(savedBook);
    }

    @Override
    public void deleteBook(Long bookId) throws BookException {
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException("Book not found"));
        existingBook.setActive(false);
        bookRepository.save(existingBook);

    }

    @Override
    public void hardDeleteBook(Long bookId) throws BookException {
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException("Book not found"));
        bookRepository.delete(existingBook);

    }

    @Override
    public PageResponse<BookDTO> searchBooksWithFilters(BookSearchRequest searchRequest) {
        int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
        int size = searchRequest.getSize() != null ? searchRequest.getSize() : 20;

        // Use unsorted pageable since native query has ORDER BY clause
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<Book> bookPage = bookRepository.searchBooksWithFilters(
                searchRequest.getSearchTerm(),
                searchRequest.getGenreId(),
                Boolean.TRUE.equals(searchRequest.getAvailableOnly()),
                pageable
        );
        return convertToPageResponse(bookPage);
    }

    @Override
    public long getTotalActiveBooks() {
        return bookRepository.countByActiveTrue();
    }

    @Override
    public long getTotalAvailableBooks() {
        return bookRepository.countAvailableBooks();
    }

    private Pageable createPageable(int page,int size,String sortBy, String sortDirection){
        page=Math.max(page,0);
        size=Math.min(size,10);
        size=Math.max(size,1);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private PageResponse<BookDTO> convertToPageResponse(Page<Book> books){
        List<BookDTO> bookDTOS = books.getContent()
                .stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());
        return new PageResponse<>(bookDTOS,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isLast(),
                books.isFirst(),
                books.isEmpty());

    }
}
