package org.example.it210_project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "showtimes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Showtime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime; // Tính toán bằng startTime + movie.duration

    private String status; // "ACTIVE", "SOLD_OUT", "HIDDEN"

    // Quan hệ: Một suất chiếu có nhiều vé được bán ra
    @OneToMany(mappedBy = "showtime", cascade = CascadeType.ALL)
    private List<Ticket> tickets;

    // Helper method để tự động tính endTime trước khi lưu vào database
    @PrePersist
    @PreUpdate
    public void calculateEndTime() {
        if (startTime != null && movie != null) {
            // EndTime = StartTime + Thời lượng phim + 15 phút dọn phòng
            this.endTime = startTime.plusMinutes(movie.getDuration() + 15);
        }
    }
}