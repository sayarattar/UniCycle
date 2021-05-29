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

public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.RecurringExpenseViewHolder> {

    List<Expense> recurringExpenses;
    RecurringExpenseAdapter(List<Expense> recurringExpenses){
        this.recurringExpenses = recurringExpenses;
    }

    @NonNull
    @Override
    public RecurringExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recurring_expense_layout, parent, false);
        return new RecurringExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecurringExpenseViewHolder holder, int position) {
        holder.expenseName.setText(recurringExpenses.get(position).name);
        holder.expenseAmount.setText(String.format(Locale.CANADA,"$%s", recurringExpenses.get(position).amount));
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.CANADA);
        holder.expenseDate.setText(format.format(recurringExpenses.get(position).nextDate));
        holder.expenseFrequency.setText(recurringExpenses.get(position).frequency);
    }

    @Override
    public int getItemCount() {
        return recurringExpenses.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class RecurringExpenseViewHolder extends RecyclerView.ViewHolder{
        CardView cardview;
        TextView expenseName;
        TextView expenseAmount;
        TextView expenseDate;
        TextView expenseFrequency;

        RecurringExpenseViewHolder(View itemView){
            super(itemView);
            cardview = itemView.findViewById(R.id.recurring_card_view);
            expenseName = itemView.findViewById(R.id.recurringName);
            expenseAmount = itemView.findViewById(R.id.recurringAmount);
            expenseDate = itemView.findViewById(R.id.recurringDate);
            expenseFrequency = itemView.findViewById(R.id.recurringFrequency);
        }
    }
}
