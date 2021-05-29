package com.example.unicycle;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.Date;

@Entity
public class Expense {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    long id;
    String name;
    double amount;
    String frequency;
    Date lastDate;
    Date nextDate;
    //String entityName;

    Expense(String name, double amount, String frequency, Date lastDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastDate);

        this.name = name;
        this.amount = amount;
        this.frequency = frequency;
        this.lastDate = lastDate;
        //this.entityName = entityName;

        switch(frequency){
            case"One-Time":
                this.nextDate = this.lastDate;
            case"Weekly":
                incrementToCurrentDate(calendar, Calendar.WEEK_OF_MONTH, 1);
                this.nextDate = new Date(calendar.getTimeInMillis());
                break;
            case"Bi-Weekly":
                incrementToCurrentDate(calendar, Calendar.WEEK_OF_MONTH, 2);
                this.nextDate = new Date(calendar.getTimeInMillis());
                break;
            case"Monthly":
                incrementToCurrentDate(calendar, Calendar.MONTH, 1);
                this.nextDate= new Date(calendar.getTimeInMillis());
                break;
            case"Yearly":
                incrementToCurrentDate(calendar, Calendar.YEAR, 1);
                this.nextDate= new Date(calendar.getTimeInMillis());
                break;
        }
    }

    private void incrementToCurrentDate(Calendar expenseDate, int field, int i){
        Date currDate = new Date(System.currentTimeMillis());
        while(currDate.after(new Date(expenseDate.getTimeInMillis()))){
            expenseDate.add(field, i);
        }
    }

    public void incrementPayDates(Date newDate){
        lastDate = nextDate;
        nextDate = newDate;
    }
}
