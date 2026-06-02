package pl.mlodawski.bankSystem.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.mlodawski.bankSystem.model.User;
import pl.mlodawski.bankSystem.model.dto.AuthRequest;
import pl.mlodawski.bankSystem.model.dto.AuthResponse;
import pl.mlodawski.bankSystem.model.dto.RegisterRequest;

import java.util.List;

public interface UserService {

    List<User> findAllUsers();
    User findById(Long id);
    User findByUsername(String username);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    void toggleRole(Long userId, String role);
    User registerNewUser(RegisterRequest request);
    boolean loginWith2FA(AuthRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    String generateMfaSecret(Long userId);
    boolean verifyAndEnableMfa(Long userId, String totpCode);
    boolean verifyTotp(Long userId, String totpCode);
    boolean disableMfa(Long userId);
}