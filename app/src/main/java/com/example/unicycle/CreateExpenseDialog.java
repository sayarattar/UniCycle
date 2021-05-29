package com.example.unicycle;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateExpenseDialog extends Dialog implements DatePickerDialog.OnDateSetListener {
    EditText nameInput;
    EditText amountInput;
    TextView dateInput;
    Spinner frequencyInput;
    Spinner entityInput;
    Button saveExpenseButton;
    Button cancelExpenseButton;

    ExpenseDao expenseDao;
    ExecutorService executor;

    String name;
    String frequency;
    Double amount;
    Date date = new Date(System.currentTimeMillis());

    public CreateExpenseDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_expense_dialog);
        executor = Executors.newSingleThreadExecutor();

        initiateView();
        initiateListeners();

        loadFreqSpinnerData();
        //loadEntitySpinnerData();

        initiateDao();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater li = LayoutInflater.from(getContext());
        View dialogView = li.inflate(R.layout.create_expense_dialog, null);
        dialogBuilder.setView(dialogView);
    }

    private void initiateView(){
        nameInput = findViewById(R.id.expenseNameInput);
        amountInput = findViewById(R.id.expenseAmountInput);
        dateInput = findViewById(R.id.expenseDateInput);
        frequencyInput = findViewById(R.id.expenseFrequencySpinner);
        saveExpenseButton = findViewById(R.id.saveButton);
        cancelExpenseButton = findViewById(R.id.cancelButton);
    }

    private void initiateListeners(){
        dateInput.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        saveExpenseButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                saveExpense();
            }
        });

        cancelExpenseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        frequencyInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                frequency = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                frequency = "One-Time";
            }
        });
    }

    private void loadFreqSpinnerData(){
        ArrayAdapter<CharSequence> freqAdapter = ArrayAdapter.createFromResource(getContext(), R.array.frequency_options, R.layout.support_simple_spinner_dropdown_item);
        freqAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        frequencyInput.setAdapter(freqAdapter);
    }

    public void saveExpense(){
        if(!validateInputs()){
            return;
        }else{
            executor.execute(new Runnable() {
                public void run(){
                    expenseDao.insert(new Expense(name, amount, frequency, date));
                }
            });
            dismiss();
        }
    }

    private boolean validateInputs(){
        if(nameInput.getText().toString().matches("")){
            Toast.makeText(getContext(), "Enter Expense Name", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            name = nameInput.getText().toString();
        }
        //Check amount input
        try{
            amount = Double.parseDouble(amountInput.getText().toString());
            if(amount < 0) {
                Toast.makeText(getContext(), "Amount must be positive", Toast.LENGTH_SHORT).show();
                return false;
            }
        }catch(NumberFormatException e){
            Toast.makeText(getContext(), "Invalid Amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loadEntitySpinnerData(){

    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        String parseDate = String.format(Locale.CANADA, "%d%d%d", i, i1+1, i2);
        String displayDate = String.format(Locale.CANADA, "%d/%d/%d", i2, i1+1, i);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.CANADA);
        try {
            date = new Date(format.parse(parseDate).getTime());
            dateInput.setHint(displayDate);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Invalid Date", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog(){
        DatePickerDialog datePD = new DatePickerDialog(
                getContext(),
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePD.show();
    }

    private void initiateDao(){
        AppDatabase db = AppDatabase.getInstance(getContext());
        expenseDao = db.expenseDao();
    }
}
