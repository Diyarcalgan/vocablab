package com.diyarcalgan.vocablab.ui.main;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.diyarcalgan.vocablab.data.WordRepository;
import com.diyarcalgan.vocablab.data.model.Word;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "MainViewModel";
    private WordRepository repository;
    private List<Word> allWords = new ArrayList<>();
    private String currentLanguage = "EN";
    private int currentIndex = 0;
    
    private final MutableLiveData<Integer> knownCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> unknownCount = new MutableLiveData<>(0);

    public MainViewModel(Application application) {
        super(application);
        try {
            repository = new WordRepository(application);
        } catch (Exception e) {
            Log.e(TAG, "Repository başlatılamadı", e);
        }
    }

    public void loadWordsFromAssets(Context context) {
        if (repository == null) return;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("words.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 4) {
                    repository.insert(new Word(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim()));
                }
            }
            loadWords();
        } catch (Exception e) {
            Log.e(TAG, "Assets okuma hatası", e);
        }
    }

    public boolean isDatabaseEmpty() {
        if (repository == null) return true;
        try {
            return repository.getTotalDatabaseCount() == 0;
        } catch (Exception e) {
            Log.e(TAG, "Veritabanı kontrol hatası", e);
            return true;
        }
    }

    public void setLanguage(String lang) {
        this.currentLanguage = lang;
        loadWords();
    }

    public void loadWords() {
        if (repository == null) return;
        try {
            allWords = repository.getWordsByLanguage(currentLanguage);
            if (allWords == null) allWords = new ArrayList<>();
            currentIndex = 0;
            refreshCounts();
        } catch (Exception e) {
            Log.e(TAG, "Kelimeler yüklenemedi", e);
            allWords = new ArrayList<>();
        }
    }

    private void refreshCounts() {
        if (repository == null) return;
        try {
            knownCount.postValue(repository.getKnownCount(currentLanguage));
            unknownCount.postValue(repository.getUnknownCount(currentLanguage));
            totalCount.postValue(repository.getTotalCountByLanguage(currentLanguage));
        } catch (Exception e) {
            Log.e(TAG, "Sayaçlar güncellenemedi", e);
        }
    }

    public boolean hasWords() { return allWords != null && !allWords.isEmpty(); }
    
    public Word getCurrentWord() { 
        if (hasWords() && currentIndex >= 0 && currentIndex < allWords.size()) {
            return allWords.get(currentIndex);
        }
        return null;
    }

    public void nextWord(boolean known) {
        if (repository == null) return;
        if (hasWords() && currentIndex < allWords.size()) {
            try {
                Word currentWord = allWords.get(currentIndex);
                currentWord.setKnown(known);
                
                new Thread(() -> {
                    try {
                        repository.update(currentWord);
                        refreshCounts();
                    } catch (Exception e) {
                        Log.e(TAG, "Kelime güncellenemedi", e);
                    }
                }).start();
                
                currentIndex++;
                if (currentIndex >= allWords.size()) {
                    currentIndex = 0;
                }
            } catch (Exception e) {
                Log.e(TAG, "Sıradaki kelimeye geçilemedi", e);
            }
        }
    }

    public void resetProgress() {
        if (repository != null) {
            repository.resetProgress();
            loadWords();
        }
    }

    public void clearAll() {
        if (repository != null) {
            repository.clearAll();
            loadWords();
        }
    }

    public LiveData<Integer> getKnownCount() { return knownCount; }
    public LiveData<Integer> getUnknownCount() { return unknownCount; }
    public LiveData<Integer> getTotalCount() { return totalCount; }
    public int getCurrentIndex() { return currentIndex; }
    public String getCurrentLanguage() { return currentLanguage; }
}
