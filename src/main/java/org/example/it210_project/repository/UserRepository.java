package org.example.it210_project.repository;
import org.example.it210_project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
//Tìm kiếm người dùng để Đăng nhập (CORE-01)
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // Tìm user để đăng nhập
}