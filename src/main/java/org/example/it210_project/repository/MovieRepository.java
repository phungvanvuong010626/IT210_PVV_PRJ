package org.example.it210_project.repository;

import org.example.it210_project.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    // Lấy phim theo trạng thái (Đang chiếu, Sắp chiếu...)
    List<Movie> findByStatus(String status);
}