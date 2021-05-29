package com.example.unicycle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    ExpenseAdapter adapter;
    ExpenseDao expenseDao;
    RecyclerView recyclerView;
    List<Expense> recurringExpenses = new ArrayList<>();
    List<Expense> temp = new ArrayList<>();
    ExecutorService executor;
    FloatingActionButton fab;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        executor = Executors.newSingleThreadExecutor();
        fab = findViewById(R.id.fabExpense);

        fab.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                CreateExpenseDialog dialog = new CreateExpenseDialog(context);
                dialog.show();
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);

        initiateDao();
        updateRecurringExpensesDates();
        populateExpenses();

        if(recurringExpenses.size() <= 0) {
            Date date = new Date(System.currentTimeMillis());
            recurringExpenses.add(new Expense("Example Expense", 20, "One-Time", date));
        }
        adapter = new ExpenseAdapter(recurringExpenses);
        int SPACING = 10;
        recyclerView.addItemDecoration(new SpaceItemDecoration(SPACING));
        recyclerView.setAdapter(adapter);

    }

    private void updateRecurringExpensesDates(){
        Date currDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                temp = expenseDao.getRecurringExpenses();
            }
        });
        try{
            executor.awaitTermination(200, TimeUnit.MILLISECONDS);
        }catch (InterruptedException e){
            Toast.makeText(context, "Couldn't retrieve Expenses", Toast.LENGTH_SHORT).show();
            return;
        }
        for(Expense e: temp){
            if(e.nextDate.before(currDate)){
                calendar.setTime(e.nextDate);
                switch(e.frequency){
                    case"Weekly":
                        incrementToCurrentDate(calendar, Calendar.WEEK_OF_MONTH, 1);
                        e.incrementPayDates(new Date(calendar.getTimeInMillis()));
                        break;
                    case"Bi-Weekly":
                        incrementToCurrentDate(calendar, Calendar.WEEK_OF_MONTH, 2);
                        e.incrementPayDates(new Date(calendar.getTimeInMillis()));
                        break;
                    case"Monthly":
                        incrementToCurrentDate(calendar, Calendar.MONTH, 1);
                        e.incrementPayDates(new Date(calendar.getTimeInMillis()));
                        break;
                    case"Yearly":
                        incrementToCurrentDate(calendar, Calendar.YEAR, 1);
                        e.incrementPayDates(new Date(calendar.getTimeInMillis()));
                        break;
                }
            }
        }
        executor.execute((new Runnable() {
            @Override
            public void run() {
                expenseDao.updateExpenses((ArrayList<Expense>) temp);
            }
        }));
        try{
            executor.awaitTermination(200, TimeUnit.MILLISECONDS);
        }catch (InterruptedException e){
            Toast.makeText(context, "Couldn't retrieve Expenses", Toast.LENGTH_SHORT).show();
            return;
        }finally {
            temp.clear();
        }
    }

    private void incrementToCurrentDate(Calendar expenseDate, int field, int i){
        Date currDate = new Date(System.currentTimeMillis());
        while(currDate.after(new Date(expenseDate.getTimeInMillis()))){
            expenseDate.add(field, i);
        }
    }

    private void initiateDao(){
        AppDatabase db = AppDatabase.getInstance(this);
        expenseDao = db.expenseDao();
    }

    private void populateExpenses(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                recurringExpenses = expenseDao.getUpComingRecurringExpenses();
            }
        });
        try{
            executor.awaitTermination(200, TimeUnit.MILLISECONDS);
        }catch (InterruptedException e){
            Toast.makeText(context, "Couldn't retrieve Expenses", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case R.id.overview:
                intent = new Intent(this, ExpenseOverview.class);
                startActivity(intent);
                return true;
            case R.id.properties:
                intent = new Intent(this, Properties.class);
                startActivity(intent);
                return true;
            case R.id.osap:

                return true;
            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return onOptionsItemSelected(item);
        }
    }
}