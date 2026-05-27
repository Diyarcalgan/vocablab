package com.diyarcalgan.vocablab; // Burası sende zaten yazılıdır, değiştirmene gerek yok

public class Word {

    // 1. Kelimenin Özellikleri (Değişkenler)
    private String english;
    private String turkish;
    private String exampleSentence; // İleride boşluk doldurma için kullanacağız
    private int status; // 0 = Yeni Kelime, 1 = Bildim (Sağa kaydırdım), -1 = Bilemedim (Sola kaydırdım)

    // 2. İnşa Edici (Constructor) - Yeni bir kelime yaratırken bu çalışır
    public Word(String english, String turkish, String exampleSentence) {
        this.english = english;
        this.turkish = turkish;
        this.exampleSentence = exampleSentence;
        this.status = 0; // Bir kelime ilk eklendiğinde varsayılan olarak "Yeni" (0) durumundadır
    }

    // 3. Getter Metodları (Dışarıdan bu özelliklere ulaşmak için)
    public String getEnglish() {
        return english;
    }

    public String getTurkish() {
        return turkish;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public int getStatus() {
        return status;
    }

    // 4. Setter Metodu (Sadece durumu dışarıdan değiştirebilmek için - Sağa/Sola kaydırdığımızda bunu kullanacağız)
    public void setStatus(int status) {
        this.status = status;
    }
}