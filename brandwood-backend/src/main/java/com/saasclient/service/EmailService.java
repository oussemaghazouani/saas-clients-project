package com.saasclient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.dev-mode:true}")
    private boolean devMode;

    @Value("${app.mail.from:}")
    private String from;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendVerificationCode(String to, String firstName, String code) {
        String subject = "SaaS — Votre code de vérification";
        String body = String.format("""
            Bonjour %s,

            Voici votre code de vérification pour activer votre compte :

            ╔══════════════╗
              %s
            ╚══════════════╝

            Ce code est valable pendant 15 minutes.
            Si vous n'avez pas créé de compte, ignorez cet email.

            — L'équipe SaaS
            """, firstName, code);

        send(to, subject, body);
    }

    public void sendPasswordResetLink(String to, String firstName, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String subject = "SaaS — Réinitialisation de mot de passe";
        String body = String.format("""
            Bonjour %s,

            Vous avez demandé la réinitialisation de votre mot de passe.
            Cliquez sur le lien ci-dessous (valable 30 minutes) :

            %s

            Si vous n'avez pas fait cette demande, ignorez cet email.

            — L'équipe SaaS
            """, firstName, resetUrl);

        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        if (devMode) {
            log.info("""

                ==================== EMAIL ====================
                To      : {}
                Subject : {}
                Body    :
                {}
                ===============================================
                """, to, subject, body);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
