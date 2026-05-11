package org.example.it210_project.repository;
import org.example.it210_project.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Quản lý thông tin chi tiết người dùng (CORE-03)
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {}