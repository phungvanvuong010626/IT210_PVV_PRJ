package org.example.it210_project.service;

import org.example.it210_project.model.User;
import org.example.it210_project.model.UserProfile;
import org.example.it210_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // SỬA ĐỔI: Thêm @Transactional để đảm bảo lưu cả User và Profile cùng lúc
    @Transactional
    public User register(User user, String phoneNumber) {
        // 1. Khởi tạo đối tượng Profile
        UserProfile profile = new UserProfile();
        profile.setPhoneNumber(phoneNumber);
        profile.setUser(user); // Thiết lập quan hệ 1-1

        // 2. Gán profile vào user (Nhờ CascadeType.ALL nên lưu User sẽ tự lưu Profile)
        user.setUserProfile(profile);

        // 3. Mặc định role là CUSTOMER nếu chưa set
        if (user.getRole() == null) {
            user.setRole(User.Role.CUSTOMER);
        }

        return userRepository.save(user);
    }

    // GIỮ NGUYÊN: Kiểm tra đăng nhập
    public Optional<User> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password));
    }

    // SỬA ĐỔI: Cập nhật hồ sơ (bao gồm cả số điện thoại từ Profile)
    @Transactional
    public User updateProfile(Long userId, String fullName, String phoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setFullName(fullName);

        // Cập nhật số điện thoại ở bảng Profile
        if (user.getUserProfile() != null) {
            user.getUserProfile().setPhoneNumber(phoneNumber);
        }

        return userRepository.save(user);
    }
}