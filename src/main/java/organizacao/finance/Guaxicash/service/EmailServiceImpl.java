package organizacao.finance.Guaxicash.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {   // 👈 implements, não extends

    private JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {  // 👈 será injetado pelo Spring
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }
}
