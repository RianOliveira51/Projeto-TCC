package organizacao.finance.Guaxicash.service;


public interface EmailService {

    void send(String to, String subject, String body);
}
