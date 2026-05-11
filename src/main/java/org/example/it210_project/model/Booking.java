//package org.example.it210_project.model;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Entity
//@Table(name = "bookings")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class Booking {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    private LocalDateTime bookingDate;
//    private Double totalAmount;
//
//    @Enumerated(EnumType.STRING)
//    private BookingStatus status; // PENDING, CONFIRMED, CANCELLED
//
//    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
//    private List<Ticket> tickets;
//
//    public enum BookingStatus {
//        PENDING, CONFIRMED, CANCELLED
//    }
//}
//
