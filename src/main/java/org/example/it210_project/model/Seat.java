package org.example.it210_project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Số ghế không được để trống")
    private String seatNumber; // Ví dụ: A1, A2, B10...

    private String type; // "NORMAL", "VIP", "COUPLE"

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
}