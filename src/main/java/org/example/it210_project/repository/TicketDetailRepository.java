package org.example.it210_project.repository;

import org.example.it210_project.model.TicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketDetailRepository extends JpaRepository<TicketDetail, Long> {

    @Query("SELECT COUNT(td) > 0 FROM TicketDetail td " +
            "WHERE td.ticket.showtime.id = :showtimeId " +
            "AND td.seat.id = :seatId " +
            "AND td.ticket.status IN ('PENDING', 'BOOKED')")
    boolean isSeatBooked(@Param("showtimeId") Long showtimeId, @Param("seatId") Long seatId);

    @Query("SELECT td.seat.id FROM TicketDetail td " +
            "WHERE td.ticket.showtime.id = :showtimeId " +
            "AND td.ticket.status IN ('PENDING', 'BOOKED')")
    List<Long> findBookedSeatIdsByShowtime(@Param("showtimeId") Long showtimeId);

    @Query("SELECT COUNT(DISTINCT td.seat.id) FROM TicketDetail td " +
            "WHERE td.ticket.showtime.id = :showtimeId " +
            "AND td.ticket.status IN ('PENDING', 'BOOKED')")
    long countBookedSeatsByShowtime(@Param("showtimeId") Long showtimeId);
}
