package com.example.springboot.repository;

import com.example.springboot.model.Book;
import com.example.springboot.model.Lend;
import com.example.springboot.model.LendStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LendRepository extends JpaRepository<Lend, Long> {
    Optional<Lend> findByBookAndStatus(Book book, LendStatus status);
}