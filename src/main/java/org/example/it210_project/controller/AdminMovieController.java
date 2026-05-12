package org.example.it210_project.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.it210_project.model.Movie;
import org.example.it210_project.model.Showtime;
import org.example.it210_project.model.User;
import org.example.it210_project.repository.GenreRepository;
import org.example.it210_project.repository.RoomRepository;
import org.example.it210_project.repository.ShowtimeRepository;
import org.example.it210_project.repository.TicketRepository;
import org.example.it210_project.service.MovieService;
import org.example.it210_project.service.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminMovieController {
    private static final int ADMIN_PAGE_SIZE = 5;

    @Autowired private MovieService movieService;
    @Autowired private GenreRepository genreRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private ShowtimeService showtimeService;
    @Autowired private TicketRepository ticketRepository;

    //kiểm tra quyền Admin nhanh
    private User getAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getRole() == User.Role.ADMIN) return user;
        return null;
    }

    //DASHBOARD THỐNG KÊ
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (getAdmin(session) == null) return "redirect:/login";
        // Tính doanh thu thực tế từ những vé có trạng thái BOOKED
        var tickets = ticketRepository.findAll();
        double totalRevenue = tickets.stream()
                .filter(t -> "PAID".equals(t.getPaymentStatus())
                        || ("BOOKED".equals(t.getStatus()) && t.getPaymentStatus() == null))
                .mapToDouble(t -> t.getTotalAmount() != null ? t.getTotalAmount() : 0.0)
                .sum();
        long paidTicketCount = tickets.stream()
                .filter(t -> "PAID".equals(t.getPaymentStatus())
                        || ("BOOKED".equals(t.getStatus()) && t.getPaymentStatus() == null))
                .count();

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalTickets", paidTicketCount);
        model.addAttribute("pendingTickets", ticketRepository.countByStatus("PENDING"));
        model.addAttribute("totalShowtimes", showtimeRepository.count());
        model.addAttribute("movies", movieService.getAllMovies());

        return "admin/dashboard";
    }

    //QUẢN LÝ DANH SÁCH PHIM
    @GetMapping("/movies")
    public String listMovies(@RequestParam(defaultValue = "1") int moviePage,
                             @RequestParam(defaultValue = "1") int showtimePage,
                             Model model,
                             HttpSession session) {
        if (getAdmin(session) == null) return "redirect:/login";
        //Lấy danh sách phim
        addPagination(model, "movie", movieService.getAllMovies(), moviePage, ADMIN_PAGE_SIZE);
        //Lấy toàn bộ suất chiếu để hiện ở bảng dưới
        addPagination(model, "showtime", showtimeRepository.findAll(), showtimePage, ADMIN_PAGE_SIZE);

        return "admin/movie-list";
    }

    @GetMapping("/movies/add")
    public String addForm(HttpSession session, Model model) {
        if (getAdmin(session) == null) return "redirect:/login";
        model.addAttribute("movie", new Movie());
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("today", LocalDate.now());
        return "admin/movie-add";
    }

    @PostMapping("/movies/add")
    public String handleAdd(@Valid @ModelAttribute Movie movie,
                            BindingResult result,
                            HttpSession session,
                            Model model,
                            RedirectAttributes ra) {
        if (getAdmin(session) == null) return "redirect:/login";

        try {
            validateMovieForm(movie, result);
            if (result.hasErrors()) {
                prepareMovieForm(model);
                return "admin/movie-add";
            }
            if (movie.getGenre() != null && movie.getGenre().getId() != null) {
                movie.setGenre(genreRepository.findById(movie.getGenre().getId()).orElse(null));
            }
            movieService.saveMovie(movie);
            ra.addFlashAttribute("success", "Đã thêm phim mới thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/movies/add";
        }
        return "redirect:/admin/movies";
    }

    //THIẾT LẬP SUẤT CHIẾU
    @GetMapping("/movies/showtimes/add")
    public String addShowtimeForm(HttpSession session, Model model) {
        if (getAdmin(session) == null) return "redirect:/login";
        model.addAttribute("showtime", new Showtime());
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomRepository.findAll());
        return "admin/showtime-add";
    }

    @PostMapping("/movies/showtimes/add")
    public String handleAddShowtime(@ModelAttribute("showtime") Showtime showtime,
                                    BindingResult result,
                                    RedirectAttributes ra,
                                    HttpSession session,
                                    Model model) {
        if (getAdmin(session) == null) return "redirect:/login";

        try {
            // 1. Kiểm tra dữ liệu đầu vào
            if (showtime.getMovie() == null || showtime.getMovie().getId() == null) {
                result.rejectValue("movie", "movie.required", "Vui long chon phim");
            }
            if (showtime.getRoom() == null || showtime.getRoom().getId() == null) {
                result.rejectValue("room", "room.required", "Vui long chon phong chieu");
            }
            if (showtime.getStartTime() == null) {
                result.rejectValue("startTime", "start.required", "Thoi gian bat dau khong duoc de trong");
            } else if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
                result.rejectValue("startTime", "start.past", "Thoi gian bat dau khong duoc nam trong qua khu");
            }
            if (result.hasErrors()) {
                prepareShowtimeForm(model);
                return "admin/showtime-add";
            }

            //Lấy phim từ DB để có Duration để tính endTime
            Movie movie = movieService.getMovieById(showtime.getMovie().getId());

            //Tính toán thời gian
            LocalDateTime start = showtime.getStartTime();
            LocalDateTime end = start.plusMinutes(movie.getDuration());

            //Kiểm tra xung đột
            if (showtimeService.hasConflict(showtime.getRoom().getId(), start, end)) {
                result.rejectValue("startTime", "start.conflict", "Phong nay da co lich chieu hoac chua don xong");
                prepareShowtimeForm(model);
                return "admin/showtime-add";
            }

            //Gán dữ liệu và lưu
            showtime.setMovie(movie);
            showtime.setEndTime(end);
            showtime.setStatus("ACTIVE");
            showtimeRepository.save(showtime);

            ra.addFlashAttribute("success", "Tạo suất chiếu thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/movies/showtimes/add";
        }
        return "redirect:/admin/movies";
    }

    @GetMapping("/movies/delete/{id}")
    public String deleteMovie(@PathVariable Long id, HttpSession session) {
        if (getAdmin(session) == null) return "redirect:/login";
        movieService.deleteMovie(id);
        return "redirect:/admin/movies";
    }

    @PostMapping("/showtimes/delete/{id}")
    public String deleteShowtime(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (getAdmin(session) == null) return "redirect:/login";

        if (ticketRepository.countByShowtimeId(id) > 0) {
            ra.addFlashAttribute("error", "Khong the xoa suat chieu da co ve. Hay an hoac huy ve truoc.");
            return "redirect:/admin/movies";
        }

        showtimeRepository.deleteById(id);
        ra.addFlashAttribute("success", "Da xoa suat chieu thanh cong!");
        return "redirect:/admin/movies";
    }

    @GetMapping("/movies/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (getAdmin(session) == null) return "redirect:/login";

        Movie movie = movieService.getMovieById(id);
        if (movie == null) return "redirect:/admin/movies";

        model.addAttribute("movie", movie);
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("today", LocalDate.now());
        return "admin/movie-edit"; // Bạn cần có file movie-edit.html
    }

    //lưu phim đã sửa
    @PostMapping("/movies/update")
    public String handleUpdate(@Valid @ModelAttribute Movie movie,
                               BindingResult result,
                               HttpSession session,
                               Model model,
                               RedirectAttributes ra) {
        if (getAdmin(session) == null) return "redirect:/login";

        try {
            validateMovieForm(movie, result);
            if (result.hasErrors()) {
                prepareMovieForm(model);
                return "admin/movie-edit";
            }
            //Kiểm tra xem phim có tồn tại trong DB không trước khi lưu
            Movie existingMovie = movieService.getMovieById(movie.getId());
            if (existingMovie == null) {
                ra.addFlashAttribute("error", "Không tìm thấy phim để cập nhật!");
                return "redirect:/admin/movies";
            }

            // Map lại thể loại
            if (movie.getGenre() != null && movie.getGenre().getId() != null) {
                movie.setGenre(genreRepository.findById(movie.getGenre().getId()).orElse(null));
            }

            movieService.saveMovie(movie);
            ra.addFlashAttribute("success", "Cập nhật phim thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/movies";
    }

    private void prepareMovieForm(Model model) {
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("today", LocalDate.now());
    }

    private void prepareShowtimeForm(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomRepository.findAll());
    }

    private void validateMovieForm(Movie movie, BindingResult result) {
        if (movie.getGenre() == null || movie.getGenre().getId() == null) {
            result.rejectValue("genre", "genre.required", "Vui long chon the loai");
        }
        if (movie.getReleaseDate() != null && movie.getReleaseDate().isBefore(LocalDate.now())) {
            result.rejectValue("releaseDate", "releaseDate.past", "Ngay phat hanh khong duoc nam trong qua khu");
        }
    }

    private <T> void addPagination(Model model, String prefix, List<T> items, int page, int size) {
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / size));
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = Math.min((currentPage - 1) * size, items.size());
        int toIndex = Math.min(fromIndex + size, items.size());

        model.addAttribute(prefix + "s", items.subList(fromIndex, toIndex));
        model.addAttribute(prefix + "CurrentPage", currentPage);
        model.addAttribute(prefix + "TotalPages", totalPages);
        model.addAttribute(prefix + "TotalItems", items.size());
    }
}
