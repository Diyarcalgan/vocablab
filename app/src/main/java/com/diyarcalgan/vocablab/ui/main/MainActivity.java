package com.diyarcalgan.vocablab.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.diyarcalgan.vocablab.R;
import com.diyarcalgan.vocablab.databinding.ActivityMainBinding;
import com.diyarcalgan.vocablab.ui.addword.AddWordActivity;
import com.diyarcalgan.vocablab.data.model.Word;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private TextToSpeech textToSpeech;
    private boolean isTTSReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            viewModel = new ViewModelProvider(this).get(MainViewModel.class);

            initTTS();
            initObservers();
            initListeners();
            setupBottomNav();

            // Background thread for database check and load
            new Thread(() -> {
                try {
                    if (viewModel.isDatabaseEmpty()) {
                        viewModel.loadWordsFromAssets(this);
                    } else {
                        viewModel.loadWords();
                    }
                    runOnUiThread(this::updateUI);
                } catch (Exception e) {
                    Log.e(TAG, "Database init error", e);
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "onCreate crash prevented", e);
            // If inflation fails, we can't do much, but let's try to show a toast
            Toast.makeText(this, "Arayüz yüklenirken bir hata oluştu.", Toast.LENGTH_LONG).show();
        }
    }

    private void initTTS() {
        try {
            textToSpeech = new TextToSpeech(this, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    isTTSReady = true;
                    textToSpeech.setLanguage(Locale.US);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "TTS init error", e);
        }
    }

    private void initObservers() {
        if (viewModel == null || binding == null) return;
        viewModel.getKnownCount().observe(this, count -> {
            if (binding != null) binding.textKnownCount.setText("Bilinen: " + (count != null ? count : 0));
        });
        viewModel.getUnknownCount().observe(this, count -> {
            if (binding != null) binding.textRepeatCount.setText("Tekrar: " + (count != null ? count : 0));
        });
        viewModel.getTotalCount().observe(this, count -> {
            if (binding != null) binding.textTotalCount.setText("Toplam: " + (count != null ? count : 0));
        });
    }

    private void initListeners() {
        if (binding == null) return;

        binding.btnShowMeaning.setOnClickListener(v -> {
            binding.translationArea.setVisibility(View.VISIBLE);
            binding.translationArea.setAlpha(0f);
            binding.translationArea.animate().alpha(1f).translationY(0).setDuration(300);
            binding.btnShowMeaning.setVisibility(View.GONE);
            binding.mainCard.setCardElevation(12f);
        });

        binding.btnSwipeRight.setOnClickListener(v -> {
            viewModel.nextWord(true);
            resetCardAndUI();
        });

        binding.btnSwipeLeft.setOnClickListener(v -> {
            viewModel.nextWord(false);
            resetCardAndUI();
        });

        binding.btnAddQuick.setOnClickListener(v -> startActivity(new Intent(this, AddWordActivity.class)));

        binding.btnTranslate.setOnClickListener(v -> {
            String current = viewModel.getCurrentLanguage();
            String next = current.equals("EN") ? "DE" : "EN";
            viewModel.setLanguage(next);
            binding.textLangSource.setText(next);
            if (isTTSReady && textToSpeech != null) {
                textToSpeech.setLanguage(next.equals("EN") ? Locale.US : Locale.GERMANY);
            }
            updateUI();
        });

        binding.textWord.setOnClickListener(v -> {
            if (isTTSReady && textToSpeech != null) {
                Word currentWord = viewModel.getCurrentWord();
                if (currentWord != null) {
                    textToSpeech.speak(currentWord.getOriginalWord(), TextToSpeech.QUEUE_FLUSH, null, "vocab_tts");
                }
            }
        });
    }

    private void setupBottomNav() {
        if (binding == null) return;
        binding.bottomNavigation.setSelectedItemId(R.id.nav_study);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_list) {
                startActivity(new Intent(this, WordListActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });
    }

    private void resetCardAndUI() {
        if (binding == null) return;
        binding.translationArea.setVisibility(View.INVISIBLE);
        binding.translationArea.setAlpha(0f);
        binding.translationArea.setTranslationY(20f);
        binding.btnShowMeaning.setVisibility(View.VISIBLE);
        binding.mainCard.setCardElevation(4f);
        updateUI();
    }

    private void updateUI() {
        if (binding == null || viewModel == null) return;
        if (viewModel.hasWords()) {
            Word currentWord = viewModel.getCurrentWord();
            if (currentWord != null) {
                binding.textWord.setText(currentWord.getOriginalWord());
                binding.textMeaning.setText(currentWord.getTranslatedWord());
                binding.textSentence.setText(currentWord.getExampleSentence());
            }
        } else {
            binding.textWord.setText("Kelime Yok");
            binding.textMeaning.setText("");
            binding.textSentence.setText("");
            binding.btnShowMeaning.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null) binding.bottomNavigation.setSelectedItemId(R.id.nav_study);
        if (viewModel != null) {
            new Thread(() -> {
                viewModel.loadWords();
                runOnUiThread(this::updateUI);
            }).start();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        binding = null;
        super.onDestroy();
    }
}
