package com.diyarcalgan.vocablab.data;

import android.app.Application;
import com.diyarcalgan.vocablab.data.local.AppDatabase;
import com.diyarcalgan.vocablab.data.local.WordDao;
import com.diyarcalgan.vocablab.data.model.Word;
import java.util.List;

public class WordRepository {
    private WordDao wordDao;

    public WordRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        wordDao = db.wordDao();
    }

    public void insert(Word word) {
        wordDao.insert(word);
    }

    public void update(Word word) {
        wordDao.update(word);
    }

    public void delete(Word word) {
        wordDao.delete(word);
    }

    public List<Word> getWordsByLanguage(String lang) {
        return wordDao.getWordsByLanguage(lang);
    }

    public List<Word> getAllWords() {
        return wordDao.getAllWords();
    }

    public int getTotalDatabaseCount() {
        return wordDao.getTotalWordCount();
    }

    public int getKnownCount(String lang) {
        return wordDao.getKnownCountByLanguage(lang);
    }

    public int getUnknownCount(String lang) {
        return wordDao.getUnknownCountByLanguage(lang);
    }
    
    public int getTotalCountByLanguage(String lang) {
        return wordDao.getTotalCountByLanguage(lang);
    }

    public void resetProgress() {
        wordDao.resetAllProgress();
    }

    public void clearAll() {
        wordDao.deleteAllWords();
    }
}
