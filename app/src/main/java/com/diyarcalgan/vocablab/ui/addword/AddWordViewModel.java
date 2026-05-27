package com.diyarcalgan.vocablab.ui.addword;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.diyarcalgan.vocablab.data.WordRepository;
import com.diyarcalgan.vocablab.data.model.Word;

public class AddWordViewModel extends AndroidViewModel {
    private WordRepository repository;

    public AddWordViewModel(@NonNull Application application) {
        super(application);
        repository = new WordRepository(application);
    }

    // YENİ: Artık 4. parametre olarak "lang" (dil) bilgisini de alıp Word modeline veriyoruz
    public void saveWord(String lang, String english, String turkish, String sentence) {
        Word newWord = new Word(lang, english, turkish, sentence);
        repository.insert(newWord);
    }
}