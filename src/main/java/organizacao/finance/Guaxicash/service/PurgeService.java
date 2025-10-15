package organizacao.finance.Guaxicash.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.*;

import java.util.UUID;

@Service
public class PurgeService {

    private final UserRepository userRepository;
    private final AccountsRepository accountsRepository;
    private final CreditCardRepository creditCardRepository;
    private final BillRepository billRepository;
    private final CreditCardBillRepository creditCardBillRepository;
    private final ExpenseRepository expenseRepository;
    private final RecipheRepository recipheRepository;
    private final TransactionsRepository transactionsRepository;
    private final MissionsCompletedRepository missionsCompletedRepository;
    private final ArchivementCompletedRepository archivementCompletedRepository;

    public PurgeService(UserRepository userRepository,
                        AccountsRepository accountsRepository,
                        CreditCardRepository creditCardRepository,
                        BillRepository billRepository,
                        CreditCardBillRepository creditCardBillRepository,
                        ExpenseRepository expenseRepository,
                        RecipheRepository recipheRepository,
                        TransactionsRepository transactionsRepository,
                        MissionsCompletedRepository missionsCompletedRepository,
                        ArchivementCompletedRepository archivementCompletedRepository) {
        this.userRepository = userRepository;
        this.accountsRepository = accountsRepository;
        this.creditCardRepository = creditCardRepository;
        this.billRepository = billRepository;
        this.creditCardBillRepository = creditCardBillRepository;
        this.expenseRepository = expenseRepository;
        this.recipheRepository = recipheRepository;
        this.transactionsRepository = transactionsRepository;
        this.missionsCompletedRepository = missionsCompletedRepository;
        this.archivementCompletedRepository = archivementCompletedRepository;
    }

    /** Hard delete de TUDO relacionado ao usuário (ordem importa!) */
    @Transactional
    public void purgeUserData(UUID userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // filhos
        creditCardBillRepository.deleteByCreditCard_Accounts_User_Uuid(userId);
        expenseRepository.deleteByAccounts_User_Uuid(userId);
        recipheRepository.deleteByAccounts_User_Uuid(userId);
        transactionsRepository.deleteByAccounts_User_UuidOrForaccounts_User_Uuid(userId, userId);
        missionsCompletedRepository.deleteByUser_Uuid(userId);
        archivementCompletedRepository.deleteByUser_Uuid(userId);

        // intermediários
        billRepository.deleteByCreditCard_Accounts_User_Uuid(userId);
        creditCardRepository.deleteByAccounts_User_Uuid(userId);

        // contas
        accountsRepository.deleteByUser_Uuid(userId);

        // usuário
        userRepository.delete(u);
    }
}
