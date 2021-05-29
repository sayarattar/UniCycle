package com.example.unicycle;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PersonalDao {

    @Insert
    void addPersonal(Personal personal);

    @Query("SELECT * FROM personal")
    List<Personal> getValues();

    @Update
    void updatePersonal(Personal personal);
}
