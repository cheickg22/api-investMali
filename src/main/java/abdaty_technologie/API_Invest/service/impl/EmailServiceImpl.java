package abdaty_technologie.API_Invest.service.impl;

import java.util.Collection;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import abdaty_technologie.API_Invest.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:}")
    private String from;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendTo(String to, String subject, String text) {
        if (to == null || to.isBlank()) return;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            if (from != null && !from.isBlank()) {
                msg.setFrom(from);
            }
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
            log.info("[EmailService] Mail envoyé à {} | subject='{}'", to, subject);
        } catch (Exception ex) {
            log.warn("[EmailService] Échec envoi mail à {} | subject='{}' | cause={}", to, subject, ex.getMessage());
        }
    }

    @Override
    public void sendToMany(Collection<String> tos, String subject, String text) {
        if (tos == null || tos.isEmpty()) return;
        tos.stream().filter(Objects::nonNull).filter(s -> !s.isBlank()).distinct()
            .forEach(to -> sendTo(to, subject, text));
    }
}
