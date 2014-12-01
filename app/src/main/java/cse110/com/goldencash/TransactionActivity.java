package cse110.com.goldencash;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xin Wen on 11/20/14.
 */
public class TransactionActivity extends Activity{

    protected Spinner spinnerFrom, spinnerTo;
    protected Button button;
    protected Button transferButton;
    protected ProgressDialog proDialog;

    //false for within same account transfer
    private boolean transactionMode = false;
    private Account toAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 11. Add a spinning progress bar (and make sure it's off)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);

        //set title
        setTitle("New Transaction");

        setContentView(R.layout.activity_transaction);

        addItemsOnSpinners();

        addListenerOnButton();

        addListenerOnSpinnerItemSelection();


    }
    private void addItemsOnSpinners(){
        spinnerFrom = (Spinner) findViewById(R.id.spinner_transaction_from);
        spinnerTo = (Spinner) findViewById(R.id.spinner_transaction_to);

        //set Spinner From
        ArrayList<String> list = new ArrayList<String>();
        list.add("Debit");
        list.add("Saving");
        //list.add("Credit");

        spinnerFrom.setAdapter(createAdapter(list));
        //spinnerTo.setAdapter(adapter);
    }
    private void addListenerOnButton(){
        button = (Button)findViewById(R.id.button_confirm_transaction);
        transferButton = (Button)findViewById(R.id.button_transfer_to_account);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(getString(R.string.debugInfo_text),""+spinnerFrom.getSelectedItem()+spinnerTo.getSelectedItem());
                editbox();
            }
        });
        transferButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(getString(R.string.debugInfo_text),"transfer button clicked");
                showTransferAccountDialog();
            }
        });

    }
    private void addListenerOnSpinnerItemSelection(){

        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position == 0){
                    ArrayList<String> list = new ArrayList<String>();
                    list.add("Credit");
                    list.add("Saving");
                    spinnerTo.setAdapter(createAdapter(list));
                }else{
                    ArrayList<String> list = new ArrayList<String>();
                    list.add("Credit");
                    list.add("Debit");
                    spinnerTo.setAdapter(createAdapter(list));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        //spinnerTo.setOnItemSelectedListener(this);
    }

    private ArrayAdapter createAdapter(ArrayList list){
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected void editbox() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Amount").setIcon(android.R.drawable.ic_dialog_info).setView(input).setNegativeButton("Cancel",null);
        builder.setPositiveButton("Confirm",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                startLoading();
                String amount = input.getText().toString();
                if(transactionMode){
                    makeTransactionToOther(Integer.parseInt(amount),toAccount);
                }else {
                    makeTransaction(Integer.parseInt(amount), spinnerFrom.getSelectedItem().toString(),
                            spinnerTo.getSelectedItem().toString());
                }
                stopLoading();
                }

        });
        builder.show();
    }
    private void makeTransactionToOther(int amount, Account targetAccount){
        User user = new User();
        Account currentAccount = user.getAccount();
        currentAccount.withdrawDebit(amount);
        targetAccount.depositDebit(amount);
        currentAccount.saveAccount();
        targetAccount.saveAccount();
        alertMsg("Successful",
                "You have transferred $" + amount +
                        " from your checking account to " + toAccount.getAccountNumber());

    }
    private void makeTransaction(int amount,String from, String to){
        User user = new User();
        Account account = user.getAccount();
        if(from.equals("Debit")) {
            account.transferFromDebit(to,amount);
        }else{
            account.transferFromSaving(to,amount);
        }
        account.saveAccount();
        alertMsg("Successful", "You have transferred $" + amount + " from " + from + " to " + to);
    }

    protected void startLoading() {
        proDialog = new ProgressDialog(this);
        proDialog.setMessage("Transferring...Please Wait");
        proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        proDialog.setCancelable(false);
        proDialog.show();
    }

    protected void stopLoading() {
        proDialog.dismiss();
        proDialog = null;
    }

    private void alertMsg(String title, String msg){
        //build dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        gotoCustomerMainPage();

                    }
                });
        builder.show();
    }

    private void gotoCustomerMainPage(){
        Intent intent = new Intent(this,CustomerMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showTransferAccountDialog(){
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a account number").setIcon(android.R.drawable.ic_dialog_info).setView(input).setNegativeButton("Cancel",null);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                setProgressBarIndeterminateVisibility(true);
                transactionMode = true;
                checkAccountNumber(input.getText().toString());
            }

        });
        builder.show();
    }

    private void checkAccountNumber(String an){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Account");
        query.whereEqualTo("number", an);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    Log.d("accountNumber", parseObjects.size() + " accounts found");
                    if (!parseObjects.isEmpty()) {
                        toAccount = (Account) parseObjects.get(0);
                        editbox();
                    } else {
                        alertMsg("Account Not Found",
                                "There is no account associate to the number you entered.");
                    }
                } else {
                    //TODO:Newwork Error Msg
                }
            }
        });
    }
}