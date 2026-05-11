package org.example.it210_project.service;

import org.example.it210_project.model.*;
import org.example.it210_project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired private GenreRepository genreRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private SeatRepository seatRepository;

    @Override
    public void run(String... args) {
        // 1. Khởi tạo Thể loại phim
        if (genreRepository.count() == 0) {
            genreRepository.save(new Genre(null, "Hành động", null));
            genreRepository.save(new Genre(null, "Kinh dị", null));
            genreRepository.save(new Genre(null, "Tình cảm", null));
            genreRepository.save(new Genre(null, "Hài hước", null));
            genreRepository.save(new Genre(null, "Khoa học Viễn Tưởng", null));
            genreRepository.save(new Genre(null, "Thế giới mở", null));
        }

        // 2. Khởi tạo các tài khoản nhân viên và Admin (Phục vụ phân quyền)
        if (userRepository.count() == 0) {
            // Admin
            userRepository.save(User.builder()
                    .username("admin").password("123").email("admin@cinema.com")
                    .fullName("Quản trị viên").role(User.Role.ADMIN).build());

            // Nhân viên 01
            userRepository.save(User.builder()
                    .username("staff01").password("123").email("staff1@cinema.com")
                    .fullName("NV Bán Vé 01").role(User.Role.STAFF).build());

            // Nhân viên 02
            userRepository.save(User.builder()
                    .username("staff02").password("123").email("staff2@cinema.com")
                    .fullName("NV Bán Vé 02").role(User.Role.STAFF).build());

            // Khách hàng mẫu
            userRepository.save(User.builder()
                    .username("customer").password("123").email("user@gmail.com")
                    .fullName("Thịnh Phùng").role(User.Role.CUSTOMER).build());
        }

        // 3. Khởi tạo Phòng và tự động tạo 16 Ghế cho mỗi phòng
        if (roomRepository.count() == 0) {
            createRoomWithSeats("Phòng 01");
            createRoomWithSeats("Phòng 02");
            createRoomWithSeats("Phòng 03");
            createRoomWithSeats("Phòng 04");
            createRoomWithSeats("Phòng 05");
        }
    }

    // Hàm bổ trợ để tạo phòng kèm ghế cho đỡ trùng lặp code
    private void createRoomWithSeats(String roomName) {
        Room room = roomRepository.save(new Room(null, roomName, 16, null, null));
        List<Seat> seats = new ArrayList<>();
        String[] rows = {"A", "B", "C", "D"};
        for (String row : rows) {
            for (int i = 1; i <= 4; i++) {
                seats.add(new Seat(null, row + i, "NORMAL", room));
            }
        }
        seatRepository.saveAll(seats);
    }
}