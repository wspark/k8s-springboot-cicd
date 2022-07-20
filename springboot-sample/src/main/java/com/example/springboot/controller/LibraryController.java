package com.example.springboot.controller;

import io.micrometer.core.annotation.Timed;
import com.example.springboot.model.Author;
import com.example.springboot.model.Book;
import com.example.springboot.model.Member;
import com.example.springboot.model.request.AuthorCreationRequest;
import com.example.springboot.model.request.BookCreationRequest;
import com.example.springboot.model.request.BookLendRequest;
import com.example.springboot.model.request.MemberCreationRequest;
import com.example.springboot.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/library")
@RequiredArgsConstructor
@CrossOrigin("*")
public class LibraryController {

    private final LibraryService libraryService;

    @Timed(value = "get-book")
    @GetMapping("/book")
    public ResponseEntity readBooks(@RequestParam(required = false) String isbn) {
        if (isbn == null) {
            return ResponseEntity.ok(libraryService.readBooks());
        }
        return ResponseEntity.ok(libraryService.readBook(isbn));
    }
    @Timed(value = "get-bookId")
    @GetMapping("/book/{bookId}")
    public ResponseEntity<Book> readBook (@PathVariable Long bookId) {
        return ResponseEntity.ok(libraryService.readBook(bookId));
    }
    @Timed(value = "post-book")
    @PostMapping("/book")
    public ResponseEntity<Book> createBook (@RequestBody BookCreationRequest request) {
        return ResponseEntity.ok(libraryService.createBook(request));
    }

    @Timed(value = "patch-bookId")
    @PatchMapping("/book/{bookId}")
    public ResponseEntity<Book> updateBook (@PathVariable("bookId") Long bookId, @RequestBody BookCreationRequest request) {
        return ResponseEntity.ok(libraryService.updateBook(bookId, request));
    }

    @Timed(value = "post-author")
    @PostMapping("/author")
    public ResponseEntity<Author> createAuthor (@RequestBody AuthorCreationRequest request) {
        return ResponseEntity.ok(libraryService.createAuthor(request));
    }

    @Timed(value = "get-author")
    @GetMapping("/author")
    public ResponseEntity<List<Author>> readAuthors () {
        return ResponseEntity.ok(libraryService.readAuthors());
    }

    @Timed(value = "delete-book")
    @DeleteMapping("/book/{bookId}")
    public ResponseEntity<Void> deleteBook (@PathVariable Long bookId) {
        libraryService.deleteBook(bookId);
        return ResponseEntity.ok().build();
    }

    @Timed(value = "post-member")
    @PostMapping("/member")
    public ResponseEntity<Member> createMember (@RequestBody MemberCreationRequest request) {
        return ResponseEntity.ok(libraryService.createMember(request));
    }

    @Timed(value = "get-member")
    @GetMapping("/member")
    public ResponseEntity<List<Member>> readMembers () {
        return ResponseEntity.ok(libraryService.readMembers());
    }

    @Timed(value = "patch-member")
    @PatchMapping("/member/{memberId}")
    public ResponseEntity<Member> updateMember (@RequestBody MemberCreationRequest request, @PathVariable Long memberId) {
        return ResponseEntity.ok(libraryService.updateMember(memberId, request));
    }

    @Timed(value = "post-book-lend")
    @PostMapping("/book/lend")
    public ResponseEntity<List<String>> lendABook(@RequestBody BookLendRequest bookLendRequests) {
        return ResponseEntity.ok(libraryService.lendABook(bookLendRequests));
    }
}

