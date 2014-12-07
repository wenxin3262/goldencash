package cse110.com.goldencash.modelAccount;

import com.parse.ParseClassName;

import java.text.SimpleDateFormat;
import java.util.Date;

import cse110.com.goldencash.User;

@ParseClassName("DebitAccount")
public class DebitAccount extends Account {
    private int dailyAmount;

    public DebitAccount(){
        this.accountType = "Debit";
    }

    public void setInterestRate() {
        if(isOver30days()) {
            double balance = getAmount();
            int rate;
            if (balance >= 3000)
                rate = 3;
            else if(balance >=2000 && balance < 3000)
                rate = 2;
            else if(balance >=1000 && balance < 2000)
                rate = 1;
            else
                rate = 0;

            put("InterestRate",rate);
            saveInBackground();
        }
    }

    public double getMonthInterest() {
        return getAmount()>100? getAmount() * getInt("InterestRate")/100:-25;
    }



}
