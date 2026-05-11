package org.example.it210_project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    @ToString.Exclude // Ngăn chặn vòng lặp vô hạn khi log dữ liệu
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;
}