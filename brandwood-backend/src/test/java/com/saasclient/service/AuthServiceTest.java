package com.saasclient.service;

import com.saasclient.dto.*;
import com.saasclient.entity.*;
import com.saasclient.exception.BusinessException;
import com.saasclient.repository.*;
import com.saasclient.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock VerificationTokenRepository verificationTokenRepository;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock EmailService emailService;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    private Role clientRole;
    private Role prestataireRole;
    private User enabledUser;
    private User disabledUser;

    @BeforeEach
    void setUp() {
        clientRole      = Role.builder().id(1L).name("ROLE_CLIENT").build();
        prestataireRole = Role.builder().id(2L).name("ROLE_PRESTATAIRE").build();

        enabledUser = User.builder()
            .id(1L).firstName("Alice").lastName("Dupont")
            .email("alice@test.com").password("encoded")
            .userType(UserType.CLIENT).enabled(true)
            .roles(new HashSet<>(Set.of(clientRole)))
            .build();

        disabledUser = User.builder()
            .id(2L).firstName("Bob").lastName("Martin")
            .email("bob@test.com").password("encoded")
            .userType(UserType.CLIENT).enabled(false)
            .roles(new HashSet<>(Set.of(clientRole)))
            .build();
    }

    // ── registerClient ────────────────────────────────────────────────────────────

    @Test
    void registerClient_success() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        RegisterClientRequest req = RegisterClientRequest.builder()
            .firstName("Paul").lastName("Géry").email("new@test.com")
            .password("Test@123").companyName("ACME").build();

        ApiResponse resp = authService.registerClient(req);

        assertThat(resp.isSuccess()).isTrue();
        verify(emailService).sendVerificationCode(eq("new@test.com"), eq("Paul"), anyString());
    }

    @Test
    void registerClient_emailDuplicate_throwsConflict() {
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        RegisterClientRequest req = RegisterClientRequest.builder()
            .firstName("X").lastName("Y").email("alice@test.com")
            .password("Test@123").companyName("Co").build();

        assertThatThrownBy(() -> authService.registerClient(req))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getStatus())
            .isEqualTo(HttpStatus.CONFLICT);
    }

    // ── registerPrestataire ───────────────────────────────────────────────────────

    @Test
    void registerPrestataire_success() {
        when(userRepository.existsByEmail("prest@test.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_PRESTATAIRE")).thenReturn(Optional.of(prestataireRole));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        RegisterPrestataireRequest req = RegisterPrestataireRequest.builder()
            .firstName("Bob").lastName("Martin").email("prest@test.com")
            .password("Test@123").jobTitle("Développeur Web").build();

        ApiResponse resp = authService.registerPrestataire(req);

        assertThat(resp.isSuccess()).isTrue();
        verify(emailService).sendVerificationCode(eq("prest@test.com"), eq("Bob"), anyString());
    }

    // ── verifyEmail ───────────────────────────────────────────────────────────────

    @Test
    void verifyEmail_success() {
        VerificationToken token = VerificationToken.builder()
            .id(1L).code("123456").user(disabledUser)
            .expiryDate(LocalDateTime.now().plusMinutes(10)).used(false).build();

        when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.of(disabledUser));
        when(verificationTokenRepository.findTopByCodeAndUsedFalseOrderByExpiryDateDesc("123456"))
            .thenReturn(Optional.of(token));

        ApiResponse resp = authService.verifyEmail(new VerifyCodeRequest("bob@test.com", "123456"));

        assertThat(resp.isSuccess()).isTrue();
        assertThat(disabledUser.isEnabled()).isTrue();
        assertThat(token.isUsed()).isTrue();
    }

    @Test
    void verifyEmail_expiredToken_throwsBadRequest() {
        VerificationToken token = VerificationToken.builder()
            .id(1L).code("123456").user(disabledUser)
            .expiryDate(LocalDateTime.now().minusMinutes(1)).used(false).build();

        when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.of(disabledUser));
        when(verificationTokenRepository.findTopByCodeAndUsedFalseOrderByExpiryDateDesc("123456"))
            .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyEmail(new VerifyCodeRequest("bob@test.com", "123456")))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getStatus())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void verifyEmail_invalidCode_throwsBadRequest() {
        when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.of(disabledUser));
        when(verificationTokenRepository.findTopByCodeAndUsedFalseOrderByExpiryDateDesc("999999"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail(new VerifyCodeRequest("bob@test.com", "999999")))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getStatus())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void verifyEmail_alreadyVerified_throwsBadRequest() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(enabledUser));

        assertThatThrownBy(() -> authService.verifyEmail(new VerifyCodeRequest("alice@test.com", "123456")))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getStatus())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── login ─────────────────────────────────────────────────────────────────────

    @Test
    void login_success() {
        when(authenticationManager.authenticate(any())).thenReturn(
            new UsernamePasswordAuthenticationToken("alice@test.com", "password"));
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(enabledUser));
        when(jwtService.generateToken(any(), any(), any())).thenReturn("jwt-token");

        AuthResponse resp = authService.login(new LoginRequest("alice@test.com", "Test@123"));

        assertThat(resp.getToken()).isEqualTo("jwt-token");
        assertThat(resp.getUserType()).isEqualTo("CLIENT");
    }

    @Test
    void login_accountDisabled_throwsDisabledException() {
        when(authenticationManager.authenticate(any()))
            .thenThrow(new DisabledException("Account disabled"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("bob@test.com", "Test@123")))
            .isInstanceOf(DisabledException.class);
    }

    @Test
    void login_unknownUser_throwsBadCredentials() {
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown@test.com", "wrong")))
            .isInstanceOf(BadCredentialsException.class);
    }

    // ── forgotPassword ────────────────────────────────────────────────────────────

    @Test
    void forgotPassword_knownEmail_sendsLink() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(enabledUser));

        ApiResponse resp = authService.forgotPassword(new ForgotPasswordRequest("alice@test.com"));

        assertThat(resp.isSuccess()).isTrue();
        verify(emailService).sendPasswordResetLink(eq("alice@test.com"), eq("Alice"), anyString());
    }

    @Test
    void forgotPassword_unknownEmail_returnsGenericMessage() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        ApiResponse resp = authService.forgotPassword(new ForgotPasswordRequest("ghost@test.com"));

        assertThat(resp.isSuccess()).isTrue();
        verify(emailService, never()).sendPasswordResetLink(any(), any(), any());
    }

    // ── resetPassword ─────────────────────────────────────────────────────────────

    @Test
    void resetPassword_success() {
        PasswordResetToken token = PasswordResetToken.builder()
            .id(1L).token("uuid-token").user(enabledUser)
            .expiryDate(LocalDateTime.now().plusMinutes(20)).used(false).build();

        when(passwordResetTokenRepository.findByTokenAndUsedFalse("uuid-token"))
            .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPass@1")).thenReturn("encoded-new");

        ApiResponse resp = authService.resetPassword(new ResetPasswordRequest("uuid-token", "NewPass@1"));

        assertThat(resp.isSuccess()).isTrue();
        assertThat(enabledUser.getPassword()).isEqualTo("encoded-new");
        assertThat(token.isUsed()).isTrue();
    }

    @Test
    void resetPassword_expiredToken_throwsBadRequest() {
        PasswordResetToken token = PasswordResetToken.builder()
            .id(1L).token("expired-token").user(enabledUser)
            .expiryDate(LocalDateTime.now().minusMinutes(1)).used(false).build();

        when(passwordResetTokenRepository.findByTokenAndUsedFalse("expired-token"))
            .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("expired-token", "NewPass@1")))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getStatus())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void resetPassword_invalidToken_throwsBadRequest() {
        when(passwordResetTokenRepository.findByTokenAndUsedFalse("bad-token"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("bad-token", "NewPass@1")))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getStatus())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
