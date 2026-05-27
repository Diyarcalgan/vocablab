package com.diyarcalgan.vocablab.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    private ActivityWordListBinding binding;
    private WordRepository repository;
    private WordListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWordListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new WordRepository(getApplication());
        setupRecyclerView();
        setupBottomNav();

        binding.btnAddWord.setOnClickListener(v -> startActivity(new Intent(this, AddWordActivity.class)));
        
        loadWords();
    }

    private void setupRecyclerView() {
        adapter = new WordListAdapter();
        // Modern bento grid style: 2 columns
        binding.recyclerViewWords.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerViewWords.setAdapter(adapter);

        adapter.setOnWordDeleteListener(word -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sil")
                    .setMessage("'" + word.getOriginalWord() + "' silinsin mi?")
                    .setPositiveButton("Evet", (dialog, which) -> {
                        new Thread(() -> {
                            repository.delete(word);
                            runOnUiThread(this::loadWords);
                        }).start();
                    })
                    .setNegativeButton("Hayır", null)
                    .show();
        });
    }

    private void setupBottomNav() {
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
        new Thread(() -> {
            List<Word> allWords = repository.getAllWords();
            runOnUiThread(() -> {
                adapter.setWords(allWords);
                binding.textWordCount.setText((allWords != null ? allWords.size() : 0) + " kelime öğreniliyor");
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigation.setSelectedItemId(R.id.nav_list);
        loadWords();
    }
}
