package org.example.it210_project.controller;

import jakarta.servlet.http.HttpSession;
import org.example.it210_project.model.Ticket;
import org.example.it210_project.model.User;
import org.example.it210_project.repository.TicketRepository;
import org.example.it210_project.service.BookingService;
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

@Controller
@RequestMapping("/staff")
public class StaffController {
    private static final int STAFF_PAGE_SIZE = 6;

    @Autowired private TicketRepository ticketRepository;
    @Autowired private BookingService bookingService;

    @GetMapping("/counter")
    public String staffPage(@RequestParam(required = false) Long ticketId,
                            @RequestParam(defaultValue = "1") int paidPage,
                            @RequestParam(defaultValue = "1") int recentPage,
                            HttpSession session,
                            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.STAFF) {
            return "redirect:/login";
        }

        List<Ticket> paidTickets = ticketRepository.findByPaymentStatusWithInvoice("PAID");
        List<Ticket> recentTickets = ticketRepository.findTop10ByOrderByBookingTimeDesc();
        Ticket selectedTicket = null;

        if (ticketId != null) {
            selectedTicket = ticketRepository.findInvoiceById(ticketId)
                    .filter(t -> "PAID".equals(t.getPaymentStatus()))
                    .orElse(null);
        }
        if (selectedTicket == null) {
            selectedTicket = paidTickets.stream()
                    .filter(t -> "PENDING".equals(t.getStatus()))
                    .findFirst()
                    .orElse(paidTickets.isEmpty() ? null : paidTickets.get(0));
        }

        addPagination(model, "paidTicket", paidTickets, paidPage, STAFF_PAGE_SIZE);
        model.addAttribute("selectedTicket", selectedTicket);
        addPagination(model, "recentTicket", recentTickets, recentPage, STAFF_PAGE_SIZE);
        return "staff/counter";
    }

    @PostMapping("/tickets/{ticketId}/confirm")
    public String confirmTicket(@PathVariable Long ticketId,
                                HttpSession session,
                                RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.STAFF) {
            return "redirect:/login";
        }

        try {
            bookingService.confirmTicket(ticketId);
            ra.addFlashAttribute("success", "Da xac nhan thanh cong. Co the in ve cho khach.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/counter";
    }

    private <T> void addPagination(Model model, String prefix, List<T> items, int page, int size) {
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / size));
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = Math.min((currentPage - 1) * size, items.size());
        int toIndex = Math.min(fromIndex + size, items.size());

        model.addAttribute(prefix + "s", items.subList(fromIndex, toIndex));
        model.addAttribute(prefix + "CurrentPage", currentPage);
        model.addAttribute(prefix + "TotalPages", totalPages);
    }
}
