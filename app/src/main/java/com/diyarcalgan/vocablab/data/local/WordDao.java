package com.diyarcalgan.vocablab.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
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

    @Delete
    void delete(Word word);

    @Query("SELECT * FROM words WHERE language = :lang")
    List<Word> getWordsByLanguage(String lang);

    @Query("SELECT * FROM words ORDER BY id DESC")
    List<Word> getAllWords();

    @Query("SELECT COUNT(*) FROM words")
    int getTotalWordCount();

    @Query("SELECT COUNT(*) FROM words WHERE language = :lang AND isKnown = 1")
    int getKnownCountByLanguage(String lang);

    @Query("SELECT COUNT(*) FROM words WHERE language = :lang AND isKnown = 0")
    int getUnknownCountByLanguage(String lang);
    
    @Query("SELECT COUNT(*) FROM words WHERE language = :lang")
    int getTotalCountByLanguage(String lang);

    @Query("UPDATE words SET isKnown = 0")
    void resetAllProgress();

    @Query("DELETE FROM words")
    void deleteAllWords();
}
