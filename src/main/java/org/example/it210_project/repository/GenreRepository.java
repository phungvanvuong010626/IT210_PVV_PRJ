package org.example.it210_project.repository;
import org.example.it210_project.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Lấy danh sách thể loại để đổ vào form thêm phim
@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {}