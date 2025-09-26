package organizacao.finance.Guaxicash.service.scheduller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.Enums.BillPay;
import organizacao.finance.Guaxicash.repositories.BillRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;


@Component
public class BillCloseScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BillCloseScheduler.class);

    private final BillRepository billRepository;
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    public BillCloseScheduler(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Sao_Paulo")
    public void closeBillsWhoseCloseDateArrived() {
        LocalDate today = LocalDate.now(ZONE);

        // 1) Fechar: OPEN/FUTURE_BILLS com closeDate <= hoje → CLOSE
        List<BillPay> eligible = Arrays.asList(BillPay.OPEN, BillPay.FUTURE_BILLS);
        int closed = billRepository.markBillsClosed(today, BillPay.CLOSE, eligible);

        // 2) Abrir: FUTURE_BILLS cujo período inclui hoje → OPEN
        int opened = billRepository.markBillsOpenForToday(today, BillPay.OPEN, BillPay.FUTURE_BILLS);

        if (closed > 0 || opened > 0) {
            logger.info("Scheduler Bills: {} fechadas, {} abertas em {}.", closed, opened, today);
        }
    }
}