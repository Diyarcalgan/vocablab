package com.diyarcalgan.vocablab.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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

            // Veritabanı işlemleri için arka plan thread'i
            new Thread(() -> {
                try {
                    if (viewModel.isDatabaseEmpty()) {
                        viewModel.loadWordsFromAssets(this);
                    } else {
                        viewModel.loadWords();
                    }
                    runOnUiThread(this::updateUI);
                } catch (Exception e) {
                    Log.e(TAG, "Başlangıç verisi yükleme hatası", e);
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "onCreate hatası", e);
            Toast.makeText(this, "Uygulama başlatılamadı: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initTTS() {
        try {
            textToSpeech = new TextToSpeech(this, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    isTTSReady = true;
                    textToSpeech.setLanguage(Locale.US);
                } else {
                    Log.e(TAG, "TTS Başlatılamadı");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "TTS servisi hatası", e);
        }
    }

    private void initObservers() {
        viewModel.getKnownCount().observe(this, count -> {
            if (binding != null) binding.textKnownCount.setText("Bildiğim: " + (count != null ? count : 0));
        });
        viewModel.getUnknownCount().observe(this, count -> {
            if (binding != null) binding.textRepeatCount.setText("Tekrar: " + (count != null ? count : 0));
        });
        viewModel.getTotalCount().observe(this, count -> {
            if (binding != null) binding.textTotalCount.setText("Top: " + (count != null ? count : 0));
        });
    }

    private void initListeners() {
        binding.btnLangEN.setOnClickListener(v -> switchLanguage("EN"));
        binding.btnLangDE.setOnClickListener(v -> switchLanguage("DE"));
        binding.btnAddWordPage.setOnClickListener(v -> startActivity(new Intent(this, AddWordActivity.class)));
        
        binding.btnInventory.setOnClickListener(v -> startActivity(new Intent(this, WordListActivity.class)));

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
            if (!isTTSReady || textToSpeech == null) {
                Toast.makeText(this, "Ses servisi hazır değil", Toast.LENGTH_SHORT).show();
                return;
            }
            Word currentWord = viewModel.getCurrentWord();
            if (currentWord != null) {
                textToSpeech.speak(currentWord.getOriginalWord(), TextToSpeech.QUEUE_FLUSH, null, "vocab_tts");
            }
        });

        binding.menuIcon.setOnClickListener(v -> showMenu(v));
    }

    private void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add("İstatistikleri Sıfırla");
        popup.getMenu().add("Tüm Kelimeleri Sil");
        popup.getMenu().add("Hakkında");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("İstatistikleri Sıfırla")) {
                new Thread(() -> {
                    viewModel.resetProgress();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Tüm ilerleme sıfırlandı", Toast.LENGTH_SHORT).show();
                        updateUI();
                    });
                }).start();
            } else if (item.getTitle().equals("Tüm Kelimeleri Sil")) {
                new AlertDialog.Builder(this)
                        .setTitle("Tümünü Sil")
                        .setMessage("Tüm kelimeleri silmek istediğinize emin misiniz? Bu işlem geri alınamaz.")
                        .setPositiveButton("Evet", (dialog, which) -> {
                            new Thread(() -> {
                                viewModel.clearAll();
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Tüm kelimeler silindi", Toast.LENGTH_SHORT).show();
                                    updateUI();
                                });
                            }).start();
                        })
                        .setNegativeButton("Hayır", null)
                        .show();
            } else if (item.getTitle().equals("Hakkında")) {
                new AlertDialog.Builder(this)
                        .setTitle("Hakkında")
                        .setMessage("VocabLab v1.0\nKendi kelime kütüphanenizi oluşturun ve öğrenin.")
                        .setPositiveButton("Tamam", null)
                        .show();
            }
            return true;
        });
        popup.show();
    }

    private void switchLanguage(String lang) {
        viewModel.setLanguage(lang);
        updateUI();
        
        if (lang.equals("EN")) {
            binding.btnLangEN.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3498DB")));
            binding.btnLangEN.setTextColor(Color.WHITE);
            binding.btnLangDE.setBackgroundTintList(null);
            binding.btnLangDE.setTextColor(Color.parseColor("#7F8C8D"));
            if (isTTSReady && textToSpeech != null) textToSpeech.setLanguage(Locale.US);
        } else {
            binding.btnLangDE.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3498DB")));
            binding.btnLangDE.setTextColor(Color.WHITE);
            binding.btnLangEN.setBackgroundTintList(null);
            binding.btnLangEN.setTextColor(Color.parseColor("#7F8C8D"));
            if (isTTSReady && textToSpeech != null) textToSpeech.setLanguage(Locale.GERMANY);
        }
    }

    private void updateUI() {
        try {
            if (viewModel.hasWords()) {
                Word currentWord = viewModel.getCurrentWord();
                if (currentWord != null) {
                    binding.textWord.setText(currentWord.getOriginalWord());
                    binding.textMeaning.setText(currentWord.getTranslatedWord());
                    binding.textSentence.setText(currentWord.getExampleSentence());
                    
                    binding.textMeaning.setVisibility(View.INVISIBLE);
                    binding.btnShowMeaning.setVisibility(View.VISIBLE);
                }
            } else {
                binding.textWord.setText("Kelime Yok");
                binding.textMeaning.setText("");
                binding.textSentence.setText("");
                binding.textMeaning.setVisibility(View.INVISIBLE);
                binding.btnShowMeaning.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "UI güncelleme hatası", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
