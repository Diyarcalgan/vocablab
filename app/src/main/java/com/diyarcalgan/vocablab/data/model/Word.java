package com.diyarcalgan.vocablab.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "words")
public class Word {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String language; // YENİ: Hangi dil olduğunu tutacak (EN veya DE)
    private String originalWord;
    private String translatedWord;
    private String exampleSentence;
    private boolean isKnown = false;

    @Ignore
    public Word() {}

    public Word(String language, String originalWord, String translatedWord, String exampleSentence) {
        this.language = language;
        this.originalWord = originalWord;
        this.translatedWord = translatedWord;
        this.exampleSentence = exampleSentence;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getOriginalWord() { return originalWord; }
    public void setOriginalWord(String originalWord) { this.originalWord = originalWord; }

    public String getTranslatedWord() { return translatedWord; }
    public void setTranslatedWord(String translatedWord) { this.translatedWord = translatedWord; }

    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }

    public boolean isKnown() { return isKnown; }
    public void setKnown(boolean known) { isKnown = known; }
}