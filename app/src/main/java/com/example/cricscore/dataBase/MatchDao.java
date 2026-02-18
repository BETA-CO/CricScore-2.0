package com.example.cricscore.dataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.cricscore.dataTypes.Match;

import java.util.List;

@Dao
public interface MatchDao {
    @Insert
    void insert(Match match);

    @Delete
    void delete(Match match);

    @Query("SELECT * FROM matches ORDER BY id DESC")
    List<Match> getAllMatches();

}
