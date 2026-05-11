package org.example.it210_project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên phim không được để trống")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Thời lượng không được để trống")
    @Min(value = 1, message = "Thời lượng phải lớn hơn 0")
    private Integer duration; // Phút

    @NotNull(message = "Giá vé không được để trống")
    @Min(value = 0, message = "Giá vé không được âm")
    private Double price;

    @NotBlank(message = "URL ảnh poster không được để trống")
    private String posterUrl;

    @NotNull(message = "Ngày chiếu không được để trống")
    private LocalDate releaseDate;

    @NotBlank(message = "Giới hạn độ tuổi không được để trống")
    private String ageRating; // P, T13, T16, T18

    private String author; // Đạo diễn/Tác giả

    @NotBlank(message = "Trạng thái không được để trống")
    private String status; // "ĐANG_CHIEU", "SAP_CHIEU", "TAM_DUNG"

    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;

    // Quan hệ: Một bộ phim có nhiều suất chiếu (Phục vụ CORE-05)
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Showtime> showtimes;
}