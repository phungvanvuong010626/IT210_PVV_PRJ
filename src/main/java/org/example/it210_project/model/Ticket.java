package org.example.it210_project.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    private LocalDateTime bookingTime;

    private Double totalAmount; // Tổng tiền = giá vé * số lượng ghế

    private String status; // "PENDING" (chua duyet/chua thanh toan), "BOOKED", "CANCELLED"

    @Column(unique = true)
    private String orderCode;

    private String paymentStatus; // "UNPAID", "PAID", "CANCELLED"

    private LocalDateTime paidAt;

    // Quan hệ: Một hóa đơn có nhiều chi tiết ghế (CORE-07)
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<TicketDetail> ticketDetails;
}
