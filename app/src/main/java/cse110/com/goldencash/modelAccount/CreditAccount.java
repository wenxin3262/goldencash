package cse110.com.goldencash.modelAccount;
import com.parse.ParseClassName;

import cse110.com.goldencash.User;

@ParseClassName("CreditAccount")
public class CreditAccount extends Account {

    public CreditAccount(){
        this.accountType = "Credit";
    }
}