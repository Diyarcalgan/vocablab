package com.diyarcalgan.vocablab.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.diyarcalgan.vocablab.databinding.ActivityMainBinding;
import com.diyarcalgan.vocablab.ui.addword.AddWordActivity;
import com.diyarcalgan.vocablab.data.model.Word;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initTTS();
        initObservers();
        initListeners();

        // Veritabanı işlemleri için ana thread'i yormuyoruz
        new Thread(() -> {
            if (viewModel.isDatabaseEmpty()) {
                viewModel.loadWordsFromAssets(this);
            } else {
                viewModel.loadWords();
            }
            runOnUiThread(() -> updateUI());
        }).start();
    }

    private void initTTS() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
    }

    private void initObservers() {
        viewModel.getKnownCount().observe(this, count -> {
            binding.textKnownCount.setText("Bildiğim: " + count);
        });
        viewModel.getUnknownCount().observe(this, count -> {
            binding.textRepeatCount.setText("Tekrar: " + count);
        });
        viewModel.getTotalCount().observe(this, count -> {
            binding.textTotalCount.setText("Top: " + count);
        });
    }

    private void initListeners() {
        binding.btnLangEN.setOnClickListener(v -> switchLanguage("EN"));
        binding.btnLangDE.setOnClickListener(v -> switchLanguage("DE"));
        binding.btnAddWordPage.setOnClickListener(v -> startActivity(new Intent(this, AddWordActivity.class)));
        
        binding.btnShowMeaning.setOnClickListener(v -> {
            binding.textMeaning.setVisibility(View.VISIBLE);
            binding.btnShowMeaning.setVisibility(View.GONE);
        });

        binding.btnSwipeRight.setOnClickListener(v -> {
            viewModel.nextWord(true);
            updateUI();
        });

        binding.btnSwipeLeft.setOnClickListener(v -> {
            viewModel.nextWord(false);
            updateUI();
        });

        binding.iconSpeech.setOnClickListener(v -> {
            Word currentWord = viewModel.getCurrentWord();
            if (currentWord != null) {
                textToSpeech.speak(currentWord.getOriginalWord(), TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        binding.menuIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Menü yakında eklenecek!", Toast.LENGTH_SHORT).show();
        });

        binding.btnInventory.setOnClickListener(v -> {
            Toast.makeText(this, "Kelime listesi yakında eklenecek!", Toast.LENGTH_SHORT).show();
        });
    }

    private void switchLanguage(String lang) {
        viewModel.setLanguage(lang);
        updateUI();
        
        // UI colors for language selection
        if (lang.equals("EN")) {
            binding.btnLangEN.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3498DB")));
            binding.btnLangEN.setTextColor(Color.WHITE);
            binding.btnLangDE.setBackgroundTintList(null);
            binding.btnLangDE.setTextColor(Color.parseColor("#7F8C8D"));
            textToSpeech.setLanguage(Locale.US);
        } else {
            binding.btnLangDE.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3498DB")));
            binding.btnLangDE.setTextColor(Color.WHITE);
            binding.btnLangEN.setBackgroundTintList(null);
            binding.btnLangEN.setTextColor(Color.parseColor("#7F8C8D"));
            textToSpeech.setLanguage(Locale.GERMANY);
        }
    }

    private void updateUI() {
        if (viewModel.hasWords()) {
            Word currentWord = viewModel.getCurrentWord();
            if (currentWord != null) {
                binding.textWord.setText(currentWord.getOriginalWord());
                binding.textMeaning.setText(currentWord.getTranslatedWord());
                binding.textSentence.setText(currentWord.getExampleSentence());
                
                // Reset visibility
                binding.textMeaning.setVisibility(View.INVISIBLE);
                binding.btnShowMeaning.setVisibility(View.VISIBLE);
            }
        } else {
            binding.textWord.setText("Kelime Yok");
            binding.textMeaning.setText("");
            binding.textSentence.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
