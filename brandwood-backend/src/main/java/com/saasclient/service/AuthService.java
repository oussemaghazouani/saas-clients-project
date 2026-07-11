package com.saasclient.service;

import com.saasclient.dto.*;
import com.saasclient.entity.*;
import com.saasclient.exception.BusinessException;
import com.saasclient.repository.*;
import com.saasclient.security.JwtService;
import com.saasclient.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.mail.dev-mode:true}")
    private boolean devMode;

    // ── Inscription Client ────────────────────────────────────────────────────────

    @Transactional
    public ApiResponse registerClient(RegisterClientRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Un compte existe déjà avec cet email.", HttpStatus.CONFLICT);
        }

        Role role = roleRepository.findByName("ROLE_CLIENT")
            .orElseThrow(() -> new BusinessException("Rôle client introuvable.", HttpStatus.INTERNAL_SERVER_ERROR));

        User user = User.builder()
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .phone(req.getPhone())
            .companyName(req.getCompanyName())
            .userType(UserType.CLIENT)
            .enabled(true)
            .build();
        user.getRoles().add(role);
        userRepository.save(user);

        String code = sendOtp(user);
        return buildRegisterResponse(code);
    }

    // ── Inscription Prestataire ──────────────────────────────────────────────────

    @Transactional
    public ApiResponse registerPrestataire(RegisterPrestataireRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Un compte existe déjà avec cet email.", HttpStatus.CONFLICT);
        }

        Role role = roleRepository.findByName("ROLE_PRESTATAIRE")
            .orElseThrow(() -> new BusinessException("Rôle prestataire introuvable.", HttpStatus.INTERNAL_SERVER_ERROR));

        User user = User.builder()
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .phone(req.getPhone())
            .jobTitle(req.getJobTitle())
            .companyName(req.getCompanyName())
            .userType(UserType.PRESTATAIRE)
            .enabled(true)
            .build();
        user.getRoles().add(role);
        userRepository.save(user);

        String code = sendOtp(user);
        return buildRegisterResponse(code);
    }

    // ── Vérification email ────────────────────────────────────────────────────────

    @Transactional
    public ApiResponse verifyEmail(VerifyCodeRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new BusinessException("Compte introuvable.", HttpStatus.NOT_FOUND));

        if (user.isEnabled()) {
            throw new BusinessException("Ce compte est déjà vérifié.", HttpStatus.BAD_REQUEST);
        }

        VerificationToken token = verificationTokenRepository
            .findTopByCodeAndUsedFalseOrderByExpiryDateDesc(req.getCode())
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new BusinessException("Code de vérification invalide.", HttpStatus.BAD_REQUEST));

        if (token.isExpired()) {
            throw new BusinessException("Ce code a expiré. Demandez un nouveau code.", HttpStatus.BAD_REQUEST);
        }

        token.setUsed(true);
        verificationTokenRepository.save(token);

        user.setEnabled(true);
        userRepository.save(user);

        return ApiResponse.ok("Email vérifié avec succès. Vous pouvez maintenant vous connecter.");
    }

    // ── Renvoi code ───────────────────────────────────────────────────────────────

    @Transactional
    public ApiResponse resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("Compte introuvable.", HttpStatus.NOT_FOUND));

        if (user.isEnabled()) {
            throw new BusinessException("Ce compte est déjà vérifié.", HttpStatus.BAD_REQUEST);
        }

        verificationTokenRepository.invalidateAllByUser(user);
        String code = sendOtp(user);

        return devMode
            ? ApiResponse.builder().success(true)
                .message("Un nouveau code a été envoyé à votre adresse email.")
                .devCode(code).build()
            : ApiResponse.ok("Un nouveau code a été envoyé à votre adresse email.");
    }

    // ── Connexion ─────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new BusinessException("Compte introuvable.", HttpStatus.NOT_FOUND));

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal, user.getId(), user.getUserType().name());

        List<String> roles = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList());

        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .userId(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .userType(user.getUserType().name())
            .roles(roles)
            .build();
    }

    // ── Mot de passe oublié ───────────────────────────────────────────────────────

    @Transactional
    public ApiResponse forgotPassword(ForgotPasswordRequest req) {
        userRepository.findByEmail(req.getEmail()).ifPresent(user -> {
            passwordResetTokenRepository.invalidateAllByUser(user);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetLink(user.getEmail(), user.getFirstName(), resetToken.getToken());
        });

        return ApiResponse.ok("Si un compte est associé à cet email, vous recevrez un lien de réinitialisation.");
    }

    // ── Réinitialisation mot de passe ─────────────────────────────────────────────

    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest req) {
        PasswordResetToken resetToken = passwordResetTokenRepository
            .findByTokenAndUsedFalse(req.getToken())
            .orElseThrow(() -> new BusinessException("Lien de réinitialisation invalide ou déjà utilisé.", HttpStatus.BAD_REQUEST));

        if (resetToken.isExpired()) {
            throw new BusinessException("Ce lien a expiré. Veuillez faire une nouvelle demande.", HttpStatus.BAD_REQUEST);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return ApiResponse.ok("Mot de passe réinitialisé avec succès.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private String sendOtp(User user) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        VerificationToken vt = VerificationToken.builder()
            .code(code)
            .user(user)
            .expiryDate(LocalDateTime.now().plusMinutes(15))
            .used(false)
            .build();
        verificationTokenRepository.save(vt);
        emailService.sendVerificationCode(user.getEmail(), user.getFirstName(), code);
        return code;
    }

    private ApiResponse buildRegisterResponse(String code) {
        String msg = "Inscription réussie. Vérifiez votre email pour activer votre compte.";
        return devMode
            ? ApiResponse.builder().success(true).message(msg).devCode(code).build()
            : ApiResponse.ok(msg);
    }
}
