package org.example.it210_project.service;

import org.example.it210_project.model.Showtime;
import org.example.it210_project.repository.ShowtimeRepository;
import org.example.it210_project.repository.TicketDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShowtimeService {
    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private TicketDetailRepository ticketDetailRepository;

    public boolean hasConflict(Long roomId, LocalDateTime newStart, LocalDateTime newEnd) {
        LocalDateTime newEndWithCleanup = newEnd.plusMinutes(15);
        List<Showtime> existingShowtimes = showtimeRepository.findByRoomId(roomId);

        for (Showtime ex : existingShowtimes) {
            LocalDateTime exEndWithCleanup = ex.getEndTime().plusMinutes(15);
            if (newStart.isBefore(exEndWithCleanup) && newEndWithCleanup.isAfter(ex.getStartTime())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void refreshShowtimeStatus(Showtime showtime) {
        if (showtime == null || showtime.getId() == null) {
            return;
        }

        String nextStatus;
        if (showtime.getStartTime() == null || !showtime.getStartTime().isAfter(LocalDateTime.now())) {
            nextStatus = "HIDDEN";
        } else if (isSoldOut(showtime)) {
            nextStatus = "SOLD_OUT";
        } else {
            nextStatus = "ACTIVE";
        }

        if (!nextStatus.equals(showtime.getStatus())) {
            showtime.setStatus(nextStatus);
            showtimeRepository.save(showtime);
        }
    }

    @Transactional
    public void refreshAllShowtimeStatuses() {
        for (Showtime showtime : showtimeRepository.findAll()) {
            refreshShowtimeStatus(showtime);
        }
    }

    @Transactional(readOnly = true)
    public List<Showtime> findVisibleShowtimes() {
        return showtimeRepository.findByStartTimeAfterAndStatusInOrderByStartTimeAsc(
                LocalDateTime.now(),
                List.of("ACTIVE", "SOLD_OUT")
        );
    }

    public boolean isSoldOut(Showtime showtime) {
        int totalSeats = 0;
        if (showtime.getRoom() != null) {
            if (showtime.getRoom().getSeats() != null && !showtime.getRoom().getSeats().isEmpty()) {
                totalSeats = showtime.getRoom().getSeats().size();
            } else if (showtime.getRoom().getTotalSeats() != null) {
                totalSeats = showtime.getRoom().getTotalSeats();
            }
        }
        return totalSeats > 0 && ticketDetailRepository.countBookedSeatsByShowtime(showtime.getId()) >= totalSeats;
    }
}
