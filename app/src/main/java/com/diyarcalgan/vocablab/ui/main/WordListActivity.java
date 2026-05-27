package com.diyarcalgan.vocablab.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.diyarcalgan.vocablab.R;
import com.diyarcalgan.vocablab.data.WordRepository;
import com.diyarcalgan.vocablab.databinding.ActivityWordListBinding;
import com.diyarcalgan.vocablab.data.model.Word;
import com.diyarcalgan.vocablab.ui.addword.AddWordActivity;
import java.util.List;

public class WordListActivity extends AppCompatActivity {
    private static final String TAG = "WordListActivity";
    private ActivityWordListBinding binding;
    private WordRepository repository;
    private WordListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityWordListBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            repository = new WordRepository(getApplication());
            setupRecyclerView();
            setupBottomNav();

            binding.btnAddWord.setOnClickListener(v -> startActivity(new Intent(this, AddWordActivity.class)));
            
            loadWords();
        } catch (Exception e) {
            Log.e(TAG, "onCreate error", e);
        }
    }

    private void setupRecyclerView() {
        if (binding == null) return;
        adapter = new WordListAdapter();
        binding.recyclerViewWords.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerViewWords.setAdapter(adapter);

        adapter.setOnWordDeleteListener(word -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sil")
                    .setMessage("'" + word.getOriginalWord() + "' silinsin mi?")
                    .setPositiveButton("Evet", (dialog, which) -> {
                        new Thread(() -> {
                            try {
                                repository.delete(word);
                                runOnUiThread(this::loadWords);
                            } catch (Exception e) {
                                Log.e(TAG, "Delete error", e);
                            }
                        }).start();
                    })
                    .setNegativeButton("Hayır", null)
                    .show();
        });
    }

    private void setupBottomNav() {
        if (binding == null) return;
        binding.bottomNavigation.setSelectedItemId(R.id.nav_list);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_study) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return true;
        });
    }

    private void loadWords() {
        if (repository == null) return;
        new Thread(() -> {
            try {
                List<Word> allWords = repository.getAllWords();
                runOnUiThread(() -> {
                    if (binding != null) {
                        adapter.setWords(allWords);
                        binding.textWordCount.setText((allWords != null ? allWords.size() : 0) + " kelime öğreniliyor");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Load words error", e);
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null) binding.bottomNavigation.setSelectedItemId(R.id.nav_list);
        loadWords();
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
