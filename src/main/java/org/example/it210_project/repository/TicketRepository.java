package org.example.it210_project.repository;

import org.example.it210_project.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT DISTINCT t FROM Ticket t " +
            "JOIN FETCH t.user " +
            "JOIN FETCH t.showtime st " +
            "JOIN FETCH st.movie " +
            "JOIN FETCH st.room " +
            "LEFT JOIN FETCH t.ticketDetails td " +
            "LEFT JOIN FETCH td.seat " +
            "WHERE t.user.id = :userId " +
            "ORDER BY t.bookingTime DESC")
    List<Ticket> findHistoryInvoiceByUserId(@Param("userId") Long userId);

    List<Ticket> findByPaymentStatusOrderByBookingTimeDesc(String paymentStatus);

    @Query("SELECT DISTINCT t FROM Ticket t " +
            "JOIN FETCH t.user " +
            "JOIN FETCH t.showtime st " +
            "JOIN FETCH st.movie " +
            "JOIN FETCH st.room " +
            "LEFT JOIN FETCH t.ticketDetails td " +
            "LEFT JOIN FETCH td.seat " +
            "WHERE t.paymentStatus = :paymentStatus " +
            "ORDER BY t.bookingTime DESC")
    List<Ticket> findByPaymentStatusWithInvoice(@Param("paymentStatus") String paymentStatus);

    @Query("SELECT DISTINCT t FROM Ticket t " +
            "JOIN FETCH t.user " +
            "JOIN FETCH t.showtime st " +
            "JOIN FETCH st.movie " +
            "JOIN FETCH st.room " +
            "LEFT JOIN FETCH t.ticketDetails td " +
            "LEFT JOIN FETCH td.seat " +
            "WHERE t.id = :ticketId")
    Optional<Ticket> findInvoiceById(@Param("ticketId") Long ticketId);

    List<Ticket> findTop10ByOrderByBookingTimeDesc();

    Optional<Ticket> findByOrderCode(String orderCode);

    long countByPaymentStatus(String paymentStatus);

    long countByStatus(String status);

    long countByShowtimeId(Long showtimeId);
}
