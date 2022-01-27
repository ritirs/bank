package ee.bcs.bank.restbank;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    // TODO: loo teenus createExampleAccount() mis loob uue AccountDto objekti:
    //  account number = random account number
    //  firstName "John"
    //  lastName "Smith"
    //  balance 0
    //  locked false


    public AccountDto createExampleAccount() {
        AccountDto accountDto = new AccountDto();
        accountDto.setAccountNumber(createRandomAccountNumber());
        accountDto.setFirstName("John");
        accountDto.setLastName("Smith");
        accountDto.setBalance(0);
        accountDto.setLocked(false);
        return accountDto;
    }


    private String createRandomAccountNumber() {
        //  Creates random account number between EE1000 -  EE9999
        Random random = new Random();
        return "EE" + (random.nextInt(9999) + 1000);
    }

    public boolean accountIdExist(List<AccountDto> accounts, int accountId) {
        for (AccountDto account : accounts) {
            if (account.getId() == accountId) {
                return true;
            }
        }
        return false;
    }

    public AccountDto getAccountBy(List<AccountDto> accounts, int accountId) {
        //          käime läbi kõik objektid=kontod accounts listis,
        //         ja paneme iga konto muutujasse 'account'
        for (AccountDto account : accounts) {
            //      kui leiame konto mille id on võrdne accountIDga,
            //        siis tagastame selle konto
            if (account.getId() == accountId) {
                return account;
            }
        }
        return null;
    }

    public boolean accountNumberExist(List<AccountDto> accounts, String receiverAccountNumber) {
        for (AccountDto account : accounts) {
            if (account.getAccountNumber().equals(receiverAccountNumber)) {
                return true;
            }
        }
        return false;
    }

    public AccountDto getAccountByNumber(List<AccountDto> accounts, String receiverAccountNumber) {
        for (AccountDto account : accounts) {
            if (account.getAccountNumber().equals(receiverAccountNumber)) {
                return account;
            }
        }
        return null;
    }
}
