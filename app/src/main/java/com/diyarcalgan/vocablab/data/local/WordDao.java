package com.diyarcalgan.vocablab.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.diyarcalgan.vocablab.data.model.Word;
import java.util.List;

@Dao
public interface WordDao {
    @Insert
    void insert(Word word);

    // YENİ: Sadece seçilen dile ait kelimeleri getir
    @Query("SELECT * FROM words WHERE language = :lang")
    List<Word> getWordsByLanguage(String lang);

    // Tüm kelimelerin sayısını öğrenmek için
    @Query("SELECT COUNT(*) FROM words")
    int getTotalWordCount();
}