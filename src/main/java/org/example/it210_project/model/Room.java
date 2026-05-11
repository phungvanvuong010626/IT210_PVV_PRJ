package org.example.it210_project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên phòng không được để trống")
    @Column(unique = true)
    private String name;

    @NotNull(message = "Tổng số ghế không được để trống")
    private Integer totalSeats;

    // Quan hệ: Một phòng có nhiều suất chiếu
    @OneToMany(mappedBy = "room")
    private List<Showtime> showtimes;

    // Quan hệ: Một phòng có danh sách ghế cố định
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Seat> seats;
}