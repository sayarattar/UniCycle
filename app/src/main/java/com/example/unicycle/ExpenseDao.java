package com.example.unicycle;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ExpenseDao {

    @Query("SELECT * FROM expense")
    List<Expense> getAll();

    @Query("SELECT * FROM expense WHERE frequency != 'One-Time' AND nextDate BETWEEN DATE('now', 'localtime') AND DATE('now', '+1 month')")
    List<Expense> getUpComingRecurringExpenses();

    @Query("SELECT * FROM expense WHERE frequency != 'One-Time'")
    List<Expense> getRecurringExpenses();

    @Query("SELECT * FROM expense WHERE frequency == 'One-Time' AND lastDate >= DATE('now', '-1 month') ")
    List<Expense> getRecentOneTimeExpenses();

    @Query("SELECT * FROM expense WHERE frequency != 'One-Time' AND " +
            "(lastDate BETWEEN DATE('now', 'start of month') AND DATE('now', 'start of month', '+1 month', '-1 day') " +
            "OR nextDate BETWEEN DATE('now', 'start of month') AND DATE('now', 'start of month', '+1 month', '-1 day'))")
    List<Expense> getThisMonthRecurring();

    @Update()
    void updateExpenses(ArrayList<Expense> expenses);

//    @Query("SELECT * FROM expense WHERE entityName = :entityName")
//    List<Expense> getEntityExpenses(String entityName);

    @Insert
    void insert(Expense ... expenses);

    @Delete
    void delete(Expense expense);
}
