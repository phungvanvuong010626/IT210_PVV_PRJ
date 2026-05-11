package org.example.it210_project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Họ tên không được để trống")
    @Column(nullable = false)
    private String fullName;

    // --- CHỖ SỬA 1: XÓA phoneNumber VÌ ĐÃ CHUYỂN SANG USER_PROFILE ---

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // --- CHỖ SỬA 2: THÊM QUAN HỆ 1-1 VỚI USER_PROFILE ---
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private UserProfile userProfile;

    // QUAN HỆ: Một User có thể có nhiều vé (Phục vụ CORE-07 tra cứu lịch sử)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    public enum Role {
        ADMIN, STAFF, CUSTOMER
    }
}