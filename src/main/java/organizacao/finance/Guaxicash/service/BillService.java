package organizacao.finance.Guaxicash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.Bill;
import organizacao.finance.Guaxicash.repositories.BankRepository;
import organizacao.finance.Guaxicash.repositories.BillRepository;

import java.util.List;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    public List<Bill> findAll() {return billRepository.findAll();
    }

    public Bill insert(Bill bill) {
        return billRepository.save(bill);
    }
}
