package com.diyarcalgan.vocablab.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.diyarcalgan.vocablab.data.model.Word;
import java.util.List;

@Dao
public interface WordDao {
    @Insert
    void insert(Word word);

    @Update
    void update(Word word);

    @Query("SELECT * FROM words WHERE language = :lang")
    List<Word> getWordsByLanguage(String lang);

    @Query("SELECT COUNT(*) FROM words")
    int getTotalWordCount();

    @Query("SELECT COUNT(*) FROM words WHERE language = :lang AND isKnown = 1")
    int getKnownCountByLanguage(String lang);

    @Query("SELECT COUNT(*) FROM words WHERE language = :lang AND isKnown = 0")
    int getUnknownCountByLanguage(String lang);
    
    @Query("SELECT COUNT(*) FROM words WHERE language = :lang")
    int getTotalCountByLanguage(String lang);
}
