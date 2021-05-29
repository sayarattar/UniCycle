package com.example.unicycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    List<Expense> expenses;
    ExpenseAdapter(List<Expense> expenses){
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.onetime_expense_card_layout, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.expenseName.setText(expenses.get(position).name);
        holder.expenseAmount.setText(String.format(Locale.CANADA,"$%s", expenses.get(position).amount));
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.CANADA);
        holder.expenseDate.setText(format.format(expenses.get(position).nextDate));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder{
        CardView cardview;
        TextView expenseName;
        TextView expenseAmount;
        TextView expenseDate;

        ExpenseViewHolder(View itemView){
            super(itemView);
            cardview = itemView.findViewById(R.id.expense_card_view);
            expenseName = itemView.findViewById(R.id.expenseName);
            expenseAmount = itemView.findViewById(R.id.expenseAmount);
            expenseDate = itemView.findViewById(R.id.expenseDate);
        }
    }
}
