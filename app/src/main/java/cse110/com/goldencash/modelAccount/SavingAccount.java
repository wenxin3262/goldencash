package cse110.com.goldencash.modelAccount;

import com.parse.ParseClassName;

import java.text.SimpleDateFormat;
import java.util.Date;

import cse110.com.goldencash.User;

@ParseClassName("SavingAccount")
public class SavingAccount extends Account{
    private int dailyAmount;

    public SavingAccount(){
        this.accountType = "Saving";
    }

    public int getInterestRate() {
        if(isOver30days()) {
            double balance = getAmount();
            int rate;
            if (balance >= 3000)
                rate = 4;
            else if (balance >= 2000 && balance < 3000)
                rate = 3;
            else if (balance >= 1000 && balance < 2000)
                rate = 2;
            else
                rate = 0;

            put("InterestRate",rate);
            saveInBackground();
            return rate;
        }
        else
            return getInt("InterestRate");
    }

    public double getMonthInterest() {
        return getAmount()>100? getAmount() * getInterestRate()/100:-25;
    }
}


