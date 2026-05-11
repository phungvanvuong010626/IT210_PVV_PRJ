package org.example.it210_project.repository;

import jakarta.persistence.LockModeType;
import org.example.it210_project.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    @Query("SELECT s FROM Showtime s WHERE s.room.id = :roomId " +
            "AND (:newStart < s.endTime AND :newEnd > s.startTime)")
    List<Showtime> findOverlappingShowtimes(@Param("roomId") Long roomId,
                                            @Param("newStart") LocalDateTime newStart,
                                            @Param("newEnd") LocalDateTime newEnd);

    List<Showtime> findByStartTimeAfterAndStatusOrderByStartTimeAsc(LocalDateTime now, String status);

    List<Showtime> findByStartTimeAfterAndStatusInOrderByStartTimeAsc(LocalDateTime now, List<String> statuses);

    List<Showtime> findByRoomId(Long roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Showtime s JOIN FETCH s.movie JOIN FETCH s.room WHERE s.id = :id")
    Optional<Showtime> findByIdForUpdate(@Param("id") Long id);
}
