package org.example.it210_project.repository;

import org.example.it210_project.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    // Tìm danh sách ghế theo phòng để hiển thị sơ đồ
    List<Seat> findByRoomId(Long roomId);
}