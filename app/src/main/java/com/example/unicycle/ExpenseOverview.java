package com.example.unicycle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ExpenseOverview extends AppCompatActivity {
    TextView oneTimeHeadline;
    TextView recurringHeadline;
    RecyclerView onetimeRecycler;
    RecyclerView recurringRecycler;

    List<Expense> onetimeExpenses = new ArrayList<>();
    List<Expense> recurringExpenses = new ArrayList<>();

    ExpenseAdapter oneTimeAdapter;
    RecurringExpenseAdapter recurringAdapter;

    FloatingActionButton fab;
    final Context context = this;
    ExpenseDao expenseDao;
    ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_overview);
        initView();
        initDao();
        populateExpenses();




        if(onetimeExpenses.size() <= 0) {
            Date date = new Date(System.currentTimeMillis());
            onetimeExpenses.add(new Expense("Example Expense", 20, "One-Time", date));
        }
        if(recurringExpenses.size() <= 0) {
            Date date = new Date(System.currentTimeMillis());
            recurringExpenses.add(new Expense("Example Expense", 20, "Monthly", date));
        }
        initOneTimeRecycler();
        initRecurringRecycler();
    }

    private void initView(){
        oneTimeHeadline = findViewById(R.id.onetimeHeadline);
        recurringHeadline = findViewById(R.id.recurringHeadline);
        onetimeRecycler = findViewById(R.id.onetimeRecycler);
        recurringRecycler = findViewById(R.id.recurringRecycler);
        fab = findViewById(R.id.fabExpense2);

        fab.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                CreateExpenseDialog dialog = new CreateExpenseDialog(context);
                dialog.show();
            }
        });
    }

    private void initOneTimeRecycler(){
        onetimeRecycler.setLayoutManager(new LinearLayoutManager(this));
        onetimeRecycler.setHasFixedSize(true);
        oneTimeAdapter = new ExpenseAdapter(onetimeExpenses);
        onetimeRecycler.addItemDecoration(new SpaceItemDecoration(10));
        onetimeRecycler.setAdapter(oneTimeAdapter);
    }

    private void initRecurringRecycler(){
        recurringRecycler.setLayoutManager(new LinearLayoutManager(this));
        recurringRecycler.setHasFixedSize(true);
        recurringAdapter = new RecurringExpenseAdapter(recurringExpenses);
        recurringRecycler.addItemDecoration(new SpaceItemDecoration(10));
        recurringRecycler.setAdapter(recurringAdapter);
    }

    private void initDao(){
        AppDatabase db = AppDatabase.getInstance(this);
        expenseDao = db.expenseDao();
    }
    private void populateExpenses(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                recurringExpenses = expenseDao.getRecurringExpenses();
                onetimeExpenses = expenseDao.getRecentOneTimeExpenses();
            }
        });
        try{
            executor.awaitTermination(200, TimeUnit.MILLISECONDS);
            executor.shutdown();
        }catch (InterruptedException e){
            Toast.makeText(context, "Couldn't retrieve Expenses", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}