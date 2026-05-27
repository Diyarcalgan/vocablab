package com.diyarcalgan.vocablab.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.diyarcalgan.vocablab.databinding.ActivityMainBinding;
import com.diyarcalgan.vocablab.ui.addword.AddWordActivity;
import com.diyarcalgan.vocablab.data.model.Word;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private TextToSpeech textToSpeech;
    private boolean isCardFlipped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // --- SESLENDİRME ---
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                setVoiceLanguage(viewModel.getCurrentLanguage());
            }
        });

        binding.iconSpeech.setOnClickListener(v -> {
            Word currentWord = viewModel.getCurrentWord();
            if (currentWord != null) {
                textToSpeech.speak(currentWord.getOriginalWord(), TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        // --- DİL SEÇİCİ BUTONLAR ---
        binding.btnLangEN.setOnClickListener(v -> switchLanguage("EN"));
        binding.btnLangDE.setOnClickListener(v -> switchLanguage("DE"));

        // --- ÜST MENÜ BUTONLARI ---
        binding.btnAddWordPage.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddWordActivity.class));
        });

        binding.btnInventory.setOnClickListener(v -> showInventoryDialog());
        binding.menuIcon.setOnClickListener(v -> showMenuDialog());

        // --- KART DÖNDÜRME ---
        binding.btnShowMeaning.setOnClickListener(v -> {
            if (!isCardFlipped) {
                binding.flashCard.animate().rotationX(90f).setDuration(150).withEndAction(() -> {
                    binding.textMeaning.setVisibility(View.VISIBLE);
                    binding.btnShowMeaning.setText("Geç (Sonraki)");
                    binding.flashCard.setRotationX(-90f);
                    binding.flashCard.animate().rotationX(0f).setDuration(150).start();
                }).start();
                isCardFlipped = true;
            } else {
                viewModel.nextWord();
                updateUI();
                resetCardPosition();
            }
        });

        // --- ALT BUTONLAR ---
        binding.btnSwipeLeft.setOnClickListener(v -> swipeOut(-1000f, false));
        binding.btnSwipeRight.setOnClickListener(v -> swipeOut(1000f, true));

        setupSwipeListener();

        // Uygulama açılışında verileri yükle
        loadWordsFromTxtFile();
    }

    // Dili değiştirir, UI renklerini ayarlar ve listeyi yeniler
    private void switchLanguage(String lang) {
        viewModel.setLanguage(lang);
        setVoiceLanguage(lang); // Seslendirmeyi de o dile ayarla

        if (lang.equals("EN")) {
            binding.btnLangEN.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3498DB")));
            binding.btnLangEN.setTextColor(Color.WHITE);
            binding.btnLangDE.setBackgroundTintList(null);
            binding.btnLangDE.setTextColor(Color.parseColor("#7F8C8D"));
        } else {
            binding.btnLangDE.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3498DB")));
            binding.btnLangDE.setTextColor(Color.WHITE);
            binding.btnLangEN.setBackgroundTintList(null);
            binding.btnLangEN.setTextColor(Color.parseColor("#7F8C8D"));
        }
        updateUI();
    }

    private void setVoiceLanguage(String lang) {
        if (textToSpeech != null) {
            if (lang.equals("DE")) {
                textToSpeech.setLanguage(Locale.GERMANY);
            } else {
                textToSpeech.setLanguage(Locale.US);
            }
        }
    }

    private void showInventoryDialog() {
        List<Word> words = viewModel.getAllWordsDirectly();
        if (words == null || words.isEmpty()) {
            Toast.makeText(this, "Bu dil için envanter boş!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] wordItems = new String[words.size()];
        for (int i = 0; i < words.size(); i++) {
            wordItems[i] = words.get(i).getOriginalWord() + " - " + words.get(i).getTranslatedWord();
        }

        new AlertDialog.Builder(this)
                .setTitle(viewModel.getCurrentLanguage() + " Kelimeleri (" + words.size() + ")")
                .setItems(wordItems, null)
                .setPositiveButton("Kapat", null)
                .show();
    }

    private void showMenuDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Ayarlar")
                .setMessage("İstatistikleri sıfırlamak istiyor musunuz?")
                .setPositiveButton("Evet, Sıfırla", (dialog, which) -> {
                    viewModel.resetCounters();
                    updateCounters();
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void setupSwipeListener() {
        binding.flashCard.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY, startX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getTranslationX() - event.getRawX();
                        dY = v.getTranslationY() - event.getRawY();
                        startX = event.getRawX();
                        v.clearAnimation();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;
                        v.setTranslationX(newX);
                        v.setTranslationY(newY);
                        v.setRotation(newX / 25f);
                        return true;

                    case MotionEvent.ACTION_UP:
                        float deltaX = event.getRawX() - startX;
                        int screenWidth = getResources().getDisplayMetrics().widthPixels;

                        if (deltaX > screenWidth / 3f) {
                            swipeOut(screenWidth, true);
                        } else if (deltaX < -(screenWidth / 3f)) {
                            swipeOut(-screenWidth, false);
                        } else {
                            v.animate()
                                    .translationX(0).translationY(0).rotation(0)
                                    .setDuration(300).start();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void swipeOut(float targetX, boolean isKnown) {
        binding.flashCard.animate()
                .translationX(targetX)
                .translationY(0)
                .rotation(targetX > 0 ? 30 : -30)
                .setDuration(250)
                .withEndAction(() -> {
                    if (isKnown) {
                        viewModel.incrementKnown();
                    } else {
                        viewModel.incrementRepeat();
                    }
                    updateCounters();
                    updateUI();

                    resetCardPosition();
                    binding.flashCard.setAlpha(0f);
                    binding.flashCard.animate().alpha(1f).setDuration(200).start();
                }).start();
    }

    private void resetCardPosition() {
        binding.flashCard.setTranslationX(0);
        binding.flashCard.setTranslationY(0);
        binding.flashCard.setRotation(0);
        binding.flashCard.setRotationX(0);
        binding.textMeaning.setVisibility(View.INVISIBLE);
        binding.btnShowMeaning.setText("Anlamını Göster");
        isCardFlipped = false;
    }

    private void updateCounters() {
        binding.textKnownCount.setText("Bildiğim: " + viewModel.getKnownCount());
        binding.textRepeatCount.setText("Tekrar: " + viewModel.getRepeatCount());
        binding.textTotalCount.setText("Top: " + viewModel.getTotalWordCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadWords();
        updateUI();
        updateCounters();
    }

    private void updateUI() {
        if (viewModel.hasWords()) {
            Word currentWord = viewModel.getCurrentWord();
            if (currentWord != null) {
                binding.textWord.setText(currentWord.getOriginalWord());
                binding.textMeaning.setText(currentWord.getTranslatedWord());

                String sentence = currentWord.getExampleSentence();
                if(sentence != null && !sentence.isEmpty()) {
                    binding.textSentence.setText(sentence);
                } else {
                    binding.textSentence.setText("");
                }
                resetCardPosition();
            }
        } else {
            binding.textWord.setText("Kategori Boş");
            binding.textSentence.setText("Bu dil için kelime ekleyin.");
            binding.textMeaning.setVisibility(View.INVISIBLE);
            binding.btnShowMeaning.setEnabled(false);
        }
        updateCounters();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void loadWordsFromTxtFile() {
        // YENİ: Sadece tüm veritabanı tamamen boşsa oku
        if (viewModel.isDatabaseEmpty()) {
            try {
                InputStream is = getAssets().open("words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) { // Artık en az 3 parça olmalı: Dil|Kelime|Anlam
                        String lang = parts[0].trim();
                        String eng = parts[1].trim();
                        String tur = parts[2].trim();
                        String sentence = (parts.length > 3) ? parts[3].trim() : "";

                        viewModel.insertWord(new Word(lang, eng, tur, sentence));
                    }
                }
                reader.close();

                Toast.makeText(this, "Kelimeler başarıyla yüklendi! Lütfen uygulamayı yeniden başlatın.", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}