package com.diyarcalgan.vocablab.ui.main;

import android.app.Application;
import android.content.Context;
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
    private WordRepository repository;
    private List<Word> allWords = new ArrayList<>();
    private String currentLanguage = "EN";
    private int currentIndex = 0;
    
    private MutableLiveData<Integer> knownCount = new MutableLiveData<>(0);
    private MutableLiveData<Integer> totalCount = new MutableLiveData<>(0);
    private MutableLiveData<Integer> unknownCount = new MutableLiveData<>(0);

    public MainViewModel(Application application) {
        super(application);
        repository = new WordRepository(application);
    }

    public void loadWordsFromAssets(Context context) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("words.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 4) {
                    repository.insert(new Word(p[0], p[1], p[2], p[3]));
                }
            }
            loadWords();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public boolean isDatabaseEmpty() {
        return repository.getTotalDatabaseCount() == 0;
    }

    public void setLanguage(String lang) {
        this.currentLanguage = lang;
        loadWords();
    }

    public void loadWords() {
        allWords = repository.getWordsByLanguage(currentLanguage);
        currentIndex = 0;
        refreshCounts();
    }

    private void refreshCounts() {
        knownCount.postValue(repository.getKnownCount(currentLanguage));
        unknownCount.postValue(repository.getUnknownCount(currentLanguage));
        totalCount.postValue(repository.getTotalCountByLanguage(currentLanguage));
    }

    public boolean hasWords() { return allWords != null && !allWords.isEmpty(); }
    
    public Word getCurrentWord() { 
        if (hasWords() && currentIndex < allWords.size()) {
            return allWords.get(currentIndex);
        }
        return null;
    }

    public void nextWord(boolean known) {
        if (hasWords() && currentIndex < allWords.size()) {
            Word currentWord = allWords.get(currentIndex);
            currentWord.setKnown(known);
            repository.update(currentWord);
            
            currentIndex++;
            if (currentIndex >= allWords.size()) {
                currentIndex = 0;
            }
            refreshCounts();
        }
    }

    public LiveData<Integer> getKnownCount() { return knownCount; }
    public LiveData<Integer> getUnknownCount() { return unknownCount; }
    public LiveData<Integer> getTotalCount() { return totalCount; }
    public int getCurrentIndex() { return currentIndex; }
    public String getCurrentLanguage() { return currentLanguage; }
}
