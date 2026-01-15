package org.example.repository;

import org.example.model.core.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    // Здесь уже есть методы save(), findById(), findAll() и т.д.
}