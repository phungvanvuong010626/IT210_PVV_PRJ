package org.example.it210_project.service;

import org.example.it210_project.model.Seat;
import org.example.it210_project.model.Showtime;
import org.example.it210_project.model.Ticket;
import org.example.it210_project.model.TicketDetail;
import org.example.it210_project.model.User;
import org.example.it210_project.repository.SeatRepository;
import org.example.it210_project.repository.ShowtimeRepository;
import org.example.it210_project.repository.TicketDetailRepository;
import org.example.it210_project.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookingService {
    private static final long CUSTOMER_CANCEL_DEADLINE_HOURS = 24;

    @Autowired private TicketRepository ticketRepository;
    @Autowired private TicketDetailRepository detailRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private ShowtimeService showtimeService;

    public boolean canCustomerCancel(Ticket ticket) {
        if (ticket == null || ticket.getShowtime() == null || ticket.getShowtime().getStartTime() == null) {
            return false;
        }
        boolean activeTicket = "PENDING".equals(ticket.getStatus()) || "BOOKED".equals(ticket.getStatus());
        boolean notCancelledPayment = !"CANCELLED".equals(ticket.getPaymentStatus());
        LocalDateTime deadline = ticket.getShowtime().getStartTime().minusHours(CUSTOMER_CANCEL_DEADLINE_HOURS);
        return activeTicket && notCancelledPayment && LocalDateTime.now().isBefore(deadline);
    }

    @Transactional
    public Ticket createBooking(User user, Showtime selectedShowtime, List<Long> seatIds) {
        if (user == null) {
            throw new RuntimeException("Ban can dang nhap de dat ve!");
        }
        if (selectedShowtime == null || selectedShowtime.getId() == null) {
            throw new RuntimeException("Suat chieu khong hop le!");
        }

        Set<Long> uniqueSeatIds = new LinkedHashSet<>(seatIds == null ? List.of() : seatIds);
        if (uniqueSeatIds.isEmpty()) {
            throw new RuntimeException("Vui long chon ghe!");
        }

        Showtime showtime = showtimeRepository.findByIdForUpdate(selectedShowtime.getId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay suat chieu!"));
        showtimeService.refreshShowtimeStatus(showtime);

        if (!showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Suat chieu da bat dau, khong the dat ve!");
        }
        if ("SOLD_OUT".equals(showtime.getStatus())) {
            throw new RuntimeException("Suat chieu da het ve!");
        }

        List<Seat> seats = new ArrayList<>();
        for (Long seatId : uniqueSeatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Ghe khong ton tai!"));
            if (seat.getRoom() == null || !seat.getRoom().getId().equals(showtime.getRoom().getId())) {
                throw new RuntimeException("Ghe khong thuoc phong cua suat chieu nay!");
            }
            if (detailRepository.isSeatBooked(showtime.getId(), seatId)) {
                throw new RuntimeException("Ghe " + seat.getSeatNumber() + " da co nguoi dat!");
            }
            seats.add(seat);
        }

        Ticket ticket = Ticket.builder()
                .user(user)
                .showtime(showtime)
                .bookingTime(LocalDateTime.now())
                .status("PENDING")
                .paymentStatus("UNPAID")
                .orderCode(generateOrderCode())
                .totalAmount(showtime.getMovie().getPrice() * seats.size())
                .build();
        Ticket savedTicket = ticketRepository.save(ticket);

        List<TicketDetail> details = new ArrayList<>();
        for (Seat seat : seats) {
            TicketDetail detail = new TicketDetail();
            detail.setTicket(savedTicket);
            detail.setSeat(seat);
            details.add(detailRepository.save(detail));
        }
        savedTicket.setTicketDetails(details);

        showtimeService.refreshShowtimeStatus(showtime);
        return savedTicket;
    }

    @Transactional
    public void cancelBooking(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();

        if (!canCustomerCancel(ticket)) {
            throw new RuntimeException("Chi duoc huy ve truoc gio chieu it nhat 24 gio!");
        }

        ticket.setStatus("CANCELLED");
        ticket.setPaymentStatus("CANCELLED");
        ticketRepository.save(ticket);

        // Keep TicketDetail rows for invoice history. Seat availability queries ignore CANCELLED tickets.
        showtimeService.refreshShowtimeStatus(ticket.getShowtime());
    }

    @Transactional
    public void cancelBooking(Long ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (currentUser == null || ticket.getUser() == null || !ticket.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Ban khong co quyen huy ve nay!");
        }
        cancelBooking(ticketId);
    }

    @Transactional
    public void markPaid(Long ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (currentUser == null || ticket.getUser() == null || !ticket.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Ban khong co quyen thanh toan ve nay!");
        }
        if (!"PENDING".equals(ticket.getStatus()) || !"UNPAID".equals(ticket.getPaymentStatus())) {
            throw new RuntimeException("Ve nay khong the cap nhat thanh toan!");
        }
        ticket.setPaymentStatus("PAID");
        ticket.setPaidAt(LocalDateTime.now());
        ticketRepository.save(ticket);
    }

    @Transactional
    public void confirmTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (!"PAID".equals(ticket.getPaymentStatus())) {
            throw new RuntimeException("Ve chua thanh toan nen chua the xac nhan!");
        }
        ticket.setStatus("BOOKED");
        ticketRepository.save(ticket);
    }

    private String generateOrderCode() {
        String code;
        do {
            code = "TP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        } while (ticketRepository.findByOrderCode(code).isPresent());
        return code;
    }
}
