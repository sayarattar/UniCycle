package com.example.unicycle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Properties extends AppCompatActivity {
    EditText incomeValue;
    EditText cppValue;
    EditText eiValue;
    EditText rrspValue;
    EditText totalIncomeValue;
    EditText disposableIncomeValue;
    Button updateButton;

    PersonalDao personalDao;
    ExpenseDao expenseDao;
    Personal personal = new Personal(1,0);
    ExecutorService executor = Executors.newFixedThreadPool(5);
    final Context context = this;

    List<Personal> tempPersonalList = new ArrayList<>();
    List<Expense> recurrExp = new ArrayList<>();

    SharedPreferences sharedPref;
    boolean show;
    int paychequeNum;
    double fedTax, provTax, cppContribution, eiContribution, rrspContribution, afterContributionPay, disposablePay;

    final int BASIC_PERSONAL_AMOUNT_EXEMPTION = 13229;
    final double RRSP_PERCENTAGE = .03;
    final double EI_PERCENTAGE = .0158;
    final double CPP_PERCENTAGE = .0545;
    final int CPP_EXEMPTION = 3500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_properties);

        initDao();
        initPersonal();
        initView();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        show = sharedPref.getBoolean(SettingsActivity.KEY_PREF_HIDE_SWITCH, false);
        paychequeNum =  12; //sharedPref.getInt(SettingsActivity.KEY_PREF_PAY_FREQ, 30);

        if(show) {
            if(personal.income > 0)
                calculateIncome();
            populateView();
        }
    }

    public void updateValues(View view) {

        if(!empty(incomeValue) && validate(incomeValue))
            personal.income = Double.parseDouble(incomeValue.getText().toString());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                personalDao.updatePersonal(personal);
            }
        });

        try {
            executor.awaitTermination(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Toast.makeText(context, "Couldn't Update Properties", Toast.LENGTH_SHORT).show();
            return;
        }
        calculateIncome();
        populateView();
        Toast.makeText(this, "Properties Updated", Toast.LENGTH_SHORT).show();
    }

    private boolean validate(EditText value){
        double temp;

        try{
           temp = Double.parseDouble(value.getText().toString());
        }catch (NumberFormatException e){
           Toast.makeText(this, "Invalid Values", Toast.LENGTH_SHORT).show();
           return false;
        }
        if(temp < 0){
            Toast.makeText(this, "Invalid Values", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean empty(EditText value){ return value.getText().toString().matches("");}

    private void initView(){
        incomeValue = findViewById(R.id.incomeValue);
        cppValue = findViewById(R.id.cppValue);
        eiValue = findViewById(R.id.eiValue);
        rrspValue = findViewById(R.id.rrspValue);
        totalIncomeValue = findViewById(R.id.totIncomeValue);
        disposableIncomeValue = findViewById(R.id.disposableIncomeValue);
        updateButton = findViewById(R.id.updateButton);
    }

    private void initDao(){
        AppDatabase db = AppDatabase.getInstance(this);
        personalDao = db.personalDao();
        expenseDao = db.expenseDao();
    }

    private void initPersonal(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                tempPersonalList = personalDao.getValues();
            }
        });

        try {
            executor.awaitTermination(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Toast.makeText(context, "Couldn't Initiate Personal", Toast.LENGTH_SHORT).show();
            return;
        }

        if(tempPersonalList.size() <=0 ) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    personalDao.addPersonal(personal);
                }
            });

            try {
                executor.awaitTermination(200, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Toast.makeText(context, "Couldn't Initiate Personal", Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            personal = tempPersonalList.get(0);
        }
    }

    private void populateView(){
        incomeValue.setText(String.format("%.2f",personal.income));
        cppValue.setText(String.format("%.2f", cppContribution));
        eiValue.setText(String.format("%.2f", eiContribution));
        rrspValue.setText(String.format("%.2f", rrspContribution / paychequeNum));
        totalIncomeValue.setText(String.format("%.2f", afterContributionPay));
        disposableIncomeValue.setText(String.format("%.2f", disposablePay));
    }

    private void calculateIncome(){
        double recurringExpenses, totIncome = personal.income;

        rrspContribution = totIncome * RRSP_PERCENTAGE;
        fedTax = calculateFederalTax();
        provTax = calculateOntarioTax();
        recurringExpenses = addRecurringExpenses();
        cppContribution = ((totIncome - (totIncome * RRSP_PERCENTAGE) - CPP_EXEMPTION)* CPP_PERCENTAGE)/paychequeNum;
        eiContribution = ((totIncome - (totIncome * RRSP_PERCENTAGE) )* EI_PERCENTAGE)/paychequeNum;
        afterContributionPay = (totIncome - fedTax - provTax - rrspContribution)/paychequeNum - cppContribution - eiContribution ;
        disposablePay = (totIncome - fedTax - provTax - rrspContribution)/paychequeNum - cppContribution - eiContribution - recurringExpenses;

        eiValue.setText(String.format("%.2f", eiContribution));
        cppValue.setText(String.format("%.2f", cppContribution));
        rrspValue.setText(String.format("%.2f", rrspContribution/paychequeNum));
        disposableIncomeValue.setText(String.format("%.2f", disposablePay));
        totalIncomeValue.setText(String.format("%.2f", afterContributionPay));

    }

    private double calculateFederalTax(){
        double totIncome = personal.income - (personal.income * RRSP_PERCENTAGE );
        double bracket1 = 7144.5;
        double bracket2  = (totIncome - 47630 - BASIC_PERSONAL_AMOUNT_EXEMPTION) * .205;
        double bracket3 = (totIncome - 95259 - BASIC_PERSONAL_AMOUNT_EXEMPTION) * .26;
        double bracket4 = (totIncome - 147667 - BASIC_PERSONAL_AMOUNT_EXEMPTION) * .29;
        double bracket5 = (totIncome - 210371 - BASIC_PERSONAL_AMOUNT_EXEMPTION) * .33;
        //Bracket is 0 - 47630
        if(totIncome <= 47630){
            return totIncome * .15;
        }
        //Bracket is 47630 - 95259
        if(totIncome <= 95259){
            return bracket1 + bracket2;
        }
        //Bracket is 95259 -147667
        if(totIncome <= 147667){
            return bracket1 + bracket2 + bracket3;
        }
        //Bracket is 147667 - 210371
        if(totIncome <= 210371){
            return bracket1 + bracket2 + bracket3 + bracket4;
        }
        //Bracket 210371+
        return bracket1 + bracket2 + bracket3 + bracket4 + bracket5;
    }

    private double calculateOntarioTax(){
        double totIncome = personal.income - (personal.income * RRSP_PERCENTAGE );
        double bracket1 = 43790 * .0505;
        double bracket2 = (totIncome - 43790 - BASIC_PERSONAL_AMOUNT_EXEMPTION) * .0915;
        double bracket3 = (totIncome - 87813 - BASIC_PERSONAL_AMOUNT_EXEMPTION) * .1116;
        double bracket4 = (totIncome - 150000 - BASIC_PERSONAL_AMOUNT_EXEMPTION) * .1216;
        double bracket5 = (totIncome - 220000 - BASIC_PERSONAL_AMOUNT_EXEMPTION) * .1316;
        if(totIncome <= 43906){
            return bracket1;
        }
        if(totIncome <= 87813){
            return bracket1 + bracket2;
        }

        if(totIncome <= 150000){
            return bracket1 + bracket2 + bracket3;
        }
        if(totIncome <= 220000){
            return bracket1 + bracket2 + bracket3 + bracket4;
        }
        return bracket1 + bracket2 + bracket3 + bracket4 + bracket5;
    }

    private double addRecurringExpenses(){
        double totalAmount = 0;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                recurrExp = expenseDao.getThisMonthRecurring();
            }
        });
        try {
            executor.awaitTermination(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Toast.makeText(context, "Couldn't Retrieve Expenses", Toast.LENGTH_SHORT).show();
            return 0;
        }

        for(Expense exp : recurrExp)
            totalAmount += exp.amount;

        return totalAmount;
    }


}