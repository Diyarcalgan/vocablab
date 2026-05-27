package com.diyarcalgan.vocablab.data;

import android.app.Application;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.diyarcalgan.vocablab.data.local.AppDatabase;
import com.diyarcalgan.vocablab.data.local.WordDao;
import com.diyarcalgan.vocablab.data.model.Word;

public class WordRepository {
    private WordDao wordDao;
    private ExecutorService executorService;

    public WordRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        wordDao = db.wordDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Word word) {
        executorService.execute(() -> wordDao.insert(word));
    }

    public List<Word> getWordsByLanguage(String lang) {
        return wordDao.getWordsByLanguage(lang);
    }

    public int getTotalDatabaseCount() {
        return wordDao.getTotalWordCount();
    }
}