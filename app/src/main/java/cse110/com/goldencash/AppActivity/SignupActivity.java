package cse110.com.goldencash.AppActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.Date;
import java.util.List;
import java.util.Random;

import cse110.com.goldencash.R;
import cse110.com.goldencash.SideFunctionFacade;
import cse110.com.goldencash.SideFunctionFacadeImp;
import cse110.com.goldencash.modelAccount.*;
import cse110.com.goldencash.modelAccount.Account;

/**
 * Created by Xin Wen on 10/21/14.
 */
public class SignupActivity extends Activity implements View.OnClickListener {

    Button cancelButton;
    Button confirmButton;

    String username;
    String password1;
    String password2;
    String firstname;
    String lastname;
    String email;

    boolean openDebit;
    boolean openCredit;
    boolean openSaving;

    boolean finishTag = false;

    protected ProgressDialog proDialog;
    protected SideFunctionFacade sff = new SideFunctionFacadeImp();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tell the activity which XML layout is right
        setContentView(R.layout.activity_signup);

        confirmButton = (Button) findViewById(R.id.button_confirm);
        confirmButton.setOnClickListener(this);
        cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        username = ((EditText)findViewById(R.id.signup_username)).getText().toString().toLowerCase();
        password1 = ((EditText) findViewById(R.id.signup_password)).getText().toString();
        password2 = ((EditText) findViewById(R.id.signup_password2)).getText().toString();
        firstname = ((EditText)findViewById(R.id.signup_firstname)).getText().toString();
        lastname = ((EditText)findViewById(R.id.signup_lastname)).getText().toString();
        email = ((EditText) findViewById(R.id.signup_email)).getText().toString();
        openDebit = ((CheckBox)findViewById(R.id.check_debit)).isChecked();
        openCredit = ((CheckBox)findViewById(R.id.check_credit)).isChecked();
        openSaving  = ((CheckBox)findViewById(R.id.check_saving)).isChecked();

        if(findViewById(R.id.button_cancel).equals(view)) {
            finish();
        }else{
            checkInputFields();
        }
    }

    /**
     * A chain of methods to validate all criteria that user entered
     */
    private void checkInputFields(){
        if(sff.isEmpty(username)) {
            alertMsg("Sign Up Failed", "Please Enter Username");
        }
        else if(sff.isEmpty(password1)||sff.isEmpty(password2)){
            alertMsg("Sign Up Failed", "Please Enter Password");
        }
        else if(!password1.equals(password2)) {
            alertMsg("Sign Up Failed", "The passwords do not match");
        }
        else if (sff.isEmpty(firstname)||sff.isEmpty(lastname)){
            alertMsg("Sign Up Failed", "Please Enter Firstname or Lastname");
        }
        else if (!(firstname.matches("[a-zA-Z]+")) || !(lastname.matches("[a-zA-Z]+"))) {
            alertMsg("Sign Up Failed", "Please only enter letters for names.");
        }
        else if(sff.isEmpty(email)){
            alertMsg("Sign Up Failed", "Please Enter an Email Address");
        }
        else if(!openCredit && !openDebit &&!openSaving) {
            alertMsg("Sign Up Failed", "Please choose to activate at lease one account");
        }
        else {
            processSignup();
        }
    }

    /**
     * Networking with Parse for signup
     */
    private void processSignup() {
        startLoading();
        setupParse();
    }

    /**
     * initialize database object for this user.
     */
    private void setupParse() {
        String accountnumber = new Random().nextInt(99999999) % (99999999 - 00000001 + 1) + 00000001 + "";
        String log = "\n";
        Date current = new Date(System.currentTimeMillis());

        // Set up new Account
        final cse110.com.goldencash.modelAccount.DebitAccount debit = ParseObject.create(cse110.com.goldencash.modelAccount.DebitAccount.class);
        final cse110.com.goldencash.modelAccount.CreditAccount credit = ParseObject.create(cse110.com.goldencash.modelAccount.CreditAccount.class);
        final cse110.com.goldencash.modelAccount.SavingAccount saving = ParseObject.create(cse110.com.goldencash.modelAccount.SavingAccount.class);
        debit.put("accountnumber",accountnumber);
        debit.put("open",openDebit);
        debit.put("Debit", 0);
        debit.put("Log",log);
        debit.put("UpdateTime",current);
        debit.put("DailyTime",current);
        debit.put("DailyAmount",0);

        credit.put("accountnumber", accountnumber);
        credit.put("open", openCredit);
        credit.put("Credit", 0);
        credit.put("Log",log);
        credit.put("UpdateTime",current);

        saving.put("accountnumber", accountnumber);
        saving.put("open", openSaving);
        saving.put("Saving", 0);
        saving.put("Log",log);
        saving.put("UpdateTime",current);
        saving.put("DailyTime",current);
        saving.put("DailyAmount",0);
        debit.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null){
                    if (e.getCode() == 100)
                        alertMsg("Connection Failed", "Please check your Internet connection");
                    else
                        alertMsg("Debit Account Sign up Failed",e.getMessage());
                }
                credit.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e!=null) {
                            debit.deleteInBackground();
                            if (e.getCode() == 100)
                                alertMsg("Connection Failed", "Please check your Internet connection");
                            else
                                alertMsg("Credit Account Sign up Failed", e.getMessage());
                        }
                        saving.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                                if (e != null) {
                                    stopLoading();
                                    debit.deleteInBackground();
                                    credit.deleteInBackground();
                                    if (e.getCode() == 100)
                                        alertMsg("Connection Failed", "Please check your Internet connection");
                                    else
                                        alertMsg("Saving Account Sign Up Failed", e.getMessage());
                                } else {
                                    // Set up a new User
                                    ParseUser user = new ParseUser();
                                    user.setUsername(username);
                                    user.setPassword(password1);
                                    user.put("firstname", firstname);
                                    user.put("lastname", lastname);
                                    user.setEmail(email);
                                    user.put("admin", false);
                                    user.put("Debitaccount", debit);
                                    user.put("Creditaccount", credit);
                                    user.put("Savingaccount", saving);
                                    user.put("Disable",false);
                                    // Call the Parse signup method
                                    user.signUpInBackground(new SignUpCallback() {
                                        public void done(ParseException e) {
                                            stopLoading();
                                            if (e != null) {
                                                debit.deleteInBackground();
                                                credit.deleteInBackground();
                                                saving.deleteInBackground();
                                                if (e.getCode() == 100)
                                                    alertMsg("Connection Failed", "Please check your Internet connection");
                                                else
                                                    alertMsg("User Sign Up Failed", e.getMessage());
                                            } else {
                                                //sign up successful
                                                clearAlltext();
                                                finishTag = true;
                                                alertMsg("Success!", "You have successfully signed up.");
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
                    }
                });
    }

    /**
     * Helper function to clear all text fields in current activity
     */
    //this function will be using when need to clear the text user entered in the textfields
    protected void clearAlltext() {
        ViewGroup textFields = (ViewGroup) findViewById(R.id.signup_textFields);
        for (int i = 0, count = textFields.getChildCount(); i < count; ++i) {
            View view = textFields.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText) view).setText("");
            }
        }
    }

    /**
     * show an alert message on current activity.
     * @param title
     * @param msg
     */
    protected void alertMsg(String title, String msg){
        //build dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //clear msg
                        clearAlltext();
                        if(finishTag){
                            finish();
                        }
                    }
                });
        //create alert dialog
        AlertDialog alert = builder.create();
        //show dialog on screen
        alert.show();
    }

    protected void startLoading() {
        proDialog = new ProgressDialog(this);
        proDialog.setMessage("Signing Up...Please Wait");
        proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        proDialog.setCancelable(false);
        proDialog.show();
    }

    protected void stopLoading() {
        proDialog.dismiss();
        proDialog = null;
    }
}

