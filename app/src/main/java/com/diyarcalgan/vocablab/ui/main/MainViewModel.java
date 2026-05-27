package com.diyarcalgan.vocablab.ui.main;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.diyarcalgan.vocablab.data.WordRepository;
import com.diyarcalgan.vocablab.data.model.Word;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private WordRepository repository;
    private List<Word> allWords;
    private int currentIndex = 0;

    private int knownCount = 0;
    private int repeatCount = 0;

    private String currentLanguage = "EN"; // Başlangıç dili İngilizce

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new WordRepository(application);
    }

    // Seçili dili değiştir ve o dilin kelimelerini yükle
    public void setLanguage(String lang) {
        this.currentLanguage = lang;
        currentIndex = 0;
        loadWords();
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void loadWords() {
        // Sadece seçili dilin kelimelerini çeker!
        allWords = repository.getWordsByLanguage(currentLanguage);
    }

    public Word getCurrentWord() {
        if (allWords != null && !allWords.isEmpty() && currentIndex < allWords.size()) {
            return allWords.get(currentIndex);
        }
        return null;
    }

    public void nextWord() {
        if (allWords != null && currentIndex < allWords.size() - 1) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }
    }

    public boolean hasWords() {
        return allWords != null && !allWords.isEmpty();
    }

    public void insertWord(Word word) {
        repository.insert(word);
    }

    public int getTotalWordCount() {
        return allWords != null ? allWords.size() : 0;
    }

    public boolean isDatabaseEmpty() {
        return repository.getTotalDatabaseCount() == 0;
    }

    public int getKnownCount() { return knownCount; }
    public int getRepeatCount() { return repeatCount; }

    public void incrementKnown() {
        knownCount++;
        nextWord();
    }

    public void incrementRepeat() {
        repeatCount++;
        nextWord();
    }

    public List<Word> getAllWordsDirectly() {
        return allWords;
    }

    public void resetCounters() {
        knownCount = 0;
        repeatCount = 0;
    }
}