package org.example.it210_project.controller;

import jakarta.servlet.http.HttpSession;
import org.example.it210_project.model.Showtime;
import org.example.it210_project.model.Ticket;
import org.example.it210_project.model.User;
import org.example.it210_project.repository.ShowtimeRepository;
import org.example.it210_project.repository.TicketDetailRepository;
import org.example.it210_project.repository.TicketRepository;
import org.example.it210_project.service.BookingService;
import org.example.it210_project.service.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class BookingController {
    private static final int TICKETS_PER_PAGE = 5;

    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private TicketDetailRepository ticketDetailRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private BookingService bookingService;
    @Autowired private ShowtimeService showtimeService;

    @GetMapping("/booking/{showtimeId}")
    public String showSeatPlan(@PathVariable Long showtimeId, Model model, RedirectAttributes ra) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay suat chieu"));
        showtimeService.refreshShowtimeStatus(showtime);

        if ("HIDDEN".equals(showtime.getStatus())) {
            ra.addFlashAttribute("error", "Suat chieu da bat dau, khong the dat ve.");
            return "redirect:/user/home";
        }

        List<Long> bookedSeatIds = ticketDetailRepository.findBookedSeatIdsByShowtime(showtimeId);
        model.addAttribute("showtime", showtime);
        model.addAttribute("bookedSeatIds", bookedSeatIds);
        model.addAttribute("soldOut", "SOLD_OUT".equals(showtime.getStatus()));
        return "user/booking";
    }

    @PostMapping("/booking/confirm")
    public String confirmBooking(@RequestParam Long showtimeId,
                                 @RequestParam List<Long> seatIds,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/login";

        try {
            Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow();
            bookingService.createBooking(currentUser, showtime, seatIds);

            ra.addFlashAttribute("success", "Dat ve thanh cong! Ve dang cho thanh toan va nhan vien xac nhan.");
            return "redirect:/user/my-tickets";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/booking/" + showtimeId;
        }
    }

    @GetMapping("/my-tickets")
    public String showMyTickets(@RequestParam(defaultValue = "1") int page,
                                HttpSession session,
                                Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/login";

        List<Ticket> tickets = ticketRepository.findHistoryInvoiceByUserId(currentUser.getId());
        Set<Long> cancelableTicketIds = tickets.stream()
                .filter(bookingService::canCustomerCancel)
                .map(Ticket::getId)
                .collect(Collectors.toSet());

        addPagination(model, tickets, page, TICKETS_PER_PAGE);
        model.addAttribute("cancelableTicketIds", cancelableTicketIds);
        return "user/my-tickets";
    }

    @PostMapping("/tickets/{ticketId}/cancel")
    public String cancelTicket(@PathVariable Long ticketId,
                               HttpSession session,
                               RedirectAttributes ra) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/login";

        try {
            bookingService.cancelBooking(ticketId, currentUser);
            ra.addFlashAttribute("success", "Da huy ve va giai phong ghe thanh cong.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/my-tickets";
    }

    @PostMapping("/tickets/{ticketId}/pay")
    public String payTicket(@PathVariable Long ticketId,
                            HttpSession session,
                            RedirectAttributes ra) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/login";

        try {
            bookingService.markPaid(ticketId, currentUser);
            ra.addFlashAttribute("success", "Thanh toan thanh cong. Ma don hang da duoc gui sang nhan vien xac nhan.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/my-tickets";
    }

    @PostMapping("/tickets/{ticketId}/payment-cancel")
    public String cancelPayment(@PathVariable Long ticketId,
                                HttpSession session,
                                RedirectAttributes ra) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/login";

        try {
            bookingService.cancelBooking(ticketId, currentUser);
            ra.addFlashAttribute("success", "Da huy thanh toan va giai phong ghe.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/my-tickets";
    }

    private void addPagination(Model model, List<Ticket> tickets, int page, int size) {
        int totalPages = Math.max(1, (int) Math.ceil((double) tickets.size() / size));
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = Math.min((currentPage - 1) * size, tickets.size());
        int toIndex = Math.min(fromIndex + size, tickets.size());

        model.addAttribute("tickets", tickets.subList(fromIndex, toIndex));
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
    }
}
