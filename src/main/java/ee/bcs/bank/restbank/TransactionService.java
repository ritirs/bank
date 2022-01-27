package ee.bcs.bank.restbank;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    public static final String ATM = "ATM";
    public static final char NEW_ACCOUNT = 'n';
    public static final char DEPOSIT = 'd';
    public static final char WITHDRAWAL = 'w';
    public static final char SEND_MONEY = 's';
    public static final char RECEIVE_MONEY = 'r';

    @Resource
    private AccountService accountService;

    @Resource
    private BalanceService balanceService;

//    transactionType
//    n - new account
//    d - deposit
//    w - withdrawal
//    s - send money
//    r - receive money

    public TransactionDto createExampleTransaction() {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(123);
        transactionDto.setBalance(1000);
        transactionDto.setAmount(100);
        transactionDto.setTransactionType(SEND_MONEY);
        transactionDto.setReceiverAccountNumber("EE123");
        transactionDto.setSenderAccountNumber("EE456");
        return transactionDto;
    }

    public RequestResult addNewTransaction(Bank bank, TransactionDto transactionDto) {
        // loon vajalikud objektid ( tühjad)
        RequestResult requestResult = new RequestResult();

        // vajalike andmete lisamine muutujatesse
        List<AccountDto> accounts = bank.getAccounts();
        int accountId = transactionDto.getAccountId();

        //  kontrolli kas konto eksisteerib
        if (!accountService.accountIdExist(accounts, accountId)) {
            requestResult.setAccountId(accountId);
            requestResult.setError("Account ID " + accountId + " does not exist!");
            return requestResult;
        }

        // veel vajalike andmete lisamine muutujatesse
        Character transactionType = transactionDto.getTransactionType();
        Integer amount = transactionDto.getAmount();
        int transactionId = bank.getTransactionIdCount();

        //  päri välja accountID abiga õige konto ja balance
        AccountDto account = accountService.getAccountBy(accounts, accountId);
        Integer balance = account.getBalance();

        int newBalance;
        String receiverAccountNumber;

        switch (transactionType) {
            case NEW_ACCOUNT:

                transactionDto.setSenderAccountNumber(null);
                transactionDto.setReceiverAccountNumber(null);
                transactionDto.setBalance(0);
                transactionDto.setAmount(0);
                transactionDto.setLocalDateTime(LocalDateTime.now());

                transactionDto.setId(transactionId);

                bank.addTransactionToTransactions(transactionDto);
                bank.incrementTransactionID();

                requestResult.setTransactionId(transactionId);
                requestResult.setAccountId(accountId);
                requestResult.setMessage("Successfully added 'new account' transaction");
                return requestResult;

            case DEPOSIT:
                // arvutame uue balance
                newBalance = balance + amount;

                //  täidame ära transactionDTO
                transactionDto.setSenderAccountNumber(ATM);
                transactionDto.setReceiverAccountNumber(account.getAccountNumber());
                transactionDto.setBalance(newBalance);
                transactionDto.setLocalDateTime(LocalDateTime.now());
                transactionDto.setId(transactionId);

                // lisame tehingu transactionite alla (pluss ikcrementeerime)
                bank.addTransactionToTransactions(transactionDto);
                bank.incrementTransactionID();

                // uuendame konto balancet
                account.setBalance(newBalance);

                requestResult.setTransactionId(transactionId);
                requestResult.setAccountId(accountId);
                requestResult.setMessage("Successfully made deposit transaction");
                return requestResult;

            case WITHDRAWAL:
                // arvutame uue balance, withdrawaliga on miinus

                if (!balanceService.enoughMoneyOnAccount(balance, amount)) {
                    requestResult.setAccountId(accountId);
                    requestResult.setError("Not enough money: " + amount + " does not exist!");
                    return requestResult;
                }

                newBalance = balance - amount;

                //  täidame ära transactionDTO
                transactionDto.setSenderAccountNumber(account.getAccountNumber());
                transactionDto.setReceiverAccountNumber(ATM);
                transactionDto.setBalance(newBalance);
                transactionDto.setLocalDateTime(LocalDateTime.now());
                transactionDto.setId(transactionId);

                // lisame tehingu transactionite alla (pluss ikcrementeerime)
                bank.addTransactionToTransactions(transactionDto);
                bank.incrementTransactionID();

                // uuendame konto balancet
                account.setBalance(newBalance);

                requestResult.setTransactionId(transactionId);
                requestResult.setAccountId(accountId);
                requestResult.setMessage("Successfully made withdrawal transaction");
                return requestResult;

            case SEND_MONEY:

                // kas saatjal on piisavalt raha?
                if (!balanceService.enoughMoneyOnAccount(balance, amount)) {
                    requestResult.setAccountId(accountId);
                    requestResult.setError("Not enough money: " + amount + " does not exist!");
                    return requestResult;
                }

                //  arvutame SAATJA uue balace'i
                newBalance = balance - amount;

                //  täidame ära transactionDTO
                transactionDto.setSenderAccountNumber(account.getAccountNumber());
                transactionDto.setBalance(newBalance);
                transactionDto.setLocalDateTime(LocalDateTime.now());
                transactionDto.setId(transactionId);


                // lisame tehingu transactionite alla (pluss inkrementeerime)
                bank.addTransactionToTransactions(transactionDto);
                bank.incrementTransactionID();

                // uuendame konto balancet
                account.setBalance(newBalance);

                requestResult.setTransactionId(transactionId);
                requestResult.setAccountId(accountId);
                requestResult.setMessage("Successfully sent money");

                // teeme SAAJA transaktsiooni
                receiverAccountNumber = transactionDto.getReceiverAccountNumber();

                // kontrollime kas saaja konto nr eksisteerib meie andmebaasis (bank)
                if (accountService.accountNumberExist(accounts, receiverAccountNumber)) {
                    AccountDto receiverAccount = accountService.getAccountByNumber(accounts, receiverAccountNumber);
                    int receiverNewBalance = receiverAccount.getBalance() + amount;

                    // loome uue tühja transaktsiooni objekti
                    TransactionDto receiverTransactionDto = new TransactionDto();

                    //  täidame ära transactionDto
                    receiverTransactionDto.setSenderAccountNumber(account.getAccountNumber());
                    receiverTransactionDto.setReceiverAccountNumber(receiverAccountNumber);
                    receiverTransactionDto.setBalance(receiverNewBalance);
                    receiverTransactionDto.setLocalDateTime(LocalDateTime.now());
                    receiverTransactionDto.setId(bank.getTransactionIdCount());
                    bank.addTransactionToTransactions(receiverTransactionDto);
                    receiverTransactionDto.setAmount(amount);
                    receiverTransactionDto.setTransactionType(RECEIVE_MONEY);
                    bank.incrementTransactionID();
                    receiverAccount.setBalance(receiverNewBalance);


                }

                return requestResult;

            case RECEIVE_MONEY:

                receiverAccountNumber = transactionDto.getReceiverAccountNumber();

                if (!accountService.accountNumberExist(accounts, receiverAccountNumber)) {
                requestResult.setError("No such account number exists: " + receiverAccountNumber);
                return requestResult;
            }
                AccountDto receiverAccount = accountService.getAccountByNumber(accounts, receiverAccountNumber);


                // arvutame uue balance
            newBalance = balance + amount;

            //  täidame ära transactionDTO
            transactionDto.setSenderAccountNumber(ATM);
            transactionDto.setReceiverAccountNumber(account.getAccountNumber());
            transactionDto.setBalance(newBalance);
            transactionDto.setLocalDateTime(LocalDateTime.now());
            transactionDto.setId(transactionId);

            // lisame tehingu transactionite alla (pluss ikcrementeerime)
            bank.addTransactionToTransactions(transactionDto);
            bank.incrementTransactionID();

            // uuendame konto balancet
            account.setBalance(newBalance);

            requestResult.setTransactionId(transactionId);
            requestResult.setAccountId(accountId);
            requestResult.setMessage("Successfully made deposit transaction");
            return requestResult;

            default:
                requestResult.setError("Unknown transaction type: " + transactionType);
                return requestResult;


        }

    }


    // TODO:    createExampleTransaction()
    //  account id 123
    //  balance 1000
    //  amount 100
    //  transactionType 's'
    //  receiver EE123
    //  sender EE456


    // TODO:    createTransactionForNewAccount()
    //  account number
    //  balance 0
    //  amount 0
    //  transactionType 'n'
    //  receiver jääb null
    //  sender jääb null


}
