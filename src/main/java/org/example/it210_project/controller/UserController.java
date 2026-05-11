package org.example.it210_project.controller;

import jakarta.servlet.http.HttpSession;
import org.example.it210_project.model.Movie;
import org.example.it210_project.model.Showtime;
import org.example.it210_project.model.User;
import org.example.it210_project.repository.GenreRepository;
import org.example.it210_project.repository.MovieRepository;
import org.example.it210_project.service.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private static final int MOVIES_PER_PAGE = 8;

    @Autowired private MovieRepository movieRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private ShowtimeService showtimeService;

    @GetMapping("/user/home")
    public String userHome(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Long genreId,
                           @RequestParam(defaultValue = "1") int page,
                           HttpSession session,
                           Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        showtimeService.refreshAllShowtimeStatuses();
        List<Showtime> activeShowtimes = showtimeService.findVisibleShowtimes();
        Map<Long, Showtime> nextShowtimeByMovieId = new HashMap<>();
        for (Showtime showtime : activeShowtimes) {
            nextShowtimeByMovieId.putIfAbsent(showtime.getMovie().getId(), showtime);
        }

        List<Movie> filteredMovies = movieRepository.findAll().stream()
                .filter(movie -> matchesKeyword(movie, keyword))
                .filter(movie -> genreId == null || genreId == 0
                        || (movie.getGenre() != null && genreId.equals(movie.getGenre().getId())))
                .collect(Collectors.toList());

        List<Movie> showingMovies = filteredMovies.stream()
                .filter(movie -> "SHOWING".equals(movie.getStatus()))
                .collect(Collectors.toList());

        addPagination(model, "showing", showingMovies, page, MOVIES_PER_PAGE);
        model.addAttribute("comingSoonMovies", filteredMovies.stream()
                .filter(movie -> "COMING_SOON".equals(movie.getStatus()))
                .collect(Collectors.toList()));
        model.addAttribute("endedMovies", filteredMovies.stream()
                .filter(movie -> "ENDED".equals(movie.getStatus()))
                .collect(Collectors.toList()));
        model.addAttribute("nextShowtimeByMovieId", nextShowtimeByMovieId);
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedGenreId", genreId);
        return "user/home";
    }

    @GetMapping("/user/tickets")
    public String myTickets(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        return "redirect:/user/my-tickets";
    }

    private boolean matchesKeyword(Movie movie, String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        String value = keyword.toLowerCase(Locale.ROOT);
        return contains(movie.getTitle(), value)
                || (movie.getGenre() != null && contains(movie.getGenre().getName(), value));
    }

    private boolean contains(String text, String keyword) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private <T> void addPagination(Model model, String prefix, List<T> items, int page, int size) {
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / size));
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = Math.min((currentPage - 1) * size, items.size());
        int toIndex = Math.min(fromIndex + size, items.size());

        model.addAttribute(prefix + "Movies", items.subList(fromIndex, toIndex));
        model.addAttribute(prefix + "CurrentPage", currentPage);
        model.addAttribute(prefix + "TotalPages", totalPages);
    }
}
