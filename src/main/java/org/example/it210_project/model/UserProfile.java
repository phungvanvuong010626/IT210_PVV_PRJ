package org.example.it210_project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    private Long id; // Sẽ lấy từ User ID thông qua @MapsId

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @EqualsAndHashCode.Exclude
    private User user;

    @Pattern(regexp = "\\d{10}", message = "Số điện thoại phải có đúng 10 chữ số")
    private String phoneNumber;

    private String address;

    private LocalDate birthday;

    private String gender;

    // Bạn có thể thêm trường avatar nếu muốn lưu ảnh đại diện sau này
    private String avatarUrl;
}