package com.example.unicycle;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Personal {
    @NonNull
    @PrimaryKey
    int id;
    double income;

    Personal(int id, double income){
        this.id = id;
        this.income = income;
    }

}
