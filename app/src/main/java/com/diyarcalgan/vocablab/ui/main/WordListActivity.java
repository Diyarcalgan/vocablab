package com.diyarcalgan.vocablab.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.diyarcalgan.vocablab.data.WordRepository;
import com.diyarcalgan.vocablab.databinding.ActivityWordListBinding;
import com.diyarcalgan.vocablab.data.model.Word;
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

        binding.btnBack.setOnClickListener(v -> finish());
        
        loadWords();
    }

    private void setupRecyclerView() {
        adapter = new WordListAdapter();
        binding.recyclerViewWords.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewWords.setAdapter(adapter);

        adapter.setOnWordDeleteListener(word -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sil")
                    .setMessage("'" + word.getOriginalWord() + "' kelimesini silmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet", (dialog, which) -> {
                        new Thread(() -> {
                            repository.delete(word);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Kelime silindi", Toast.LENGTH_SHORT).show();
                                loadWords();
                            });
                        }).start();
                    })
                    .setNegativeButton("Hayır", null)
                    .show();
        });
    }

    private void loadWords() {
        new Thread(() -> {
            List<Word> allWords = repository.getAllWords();
            runOnUiThread(() -> {
                if (allWords == null || allWords.isEmpty()) {
                    binding.textNoWords.setVisibility(View.VISIBLE);
                    adapter.setWords(allWords);
                } else {
                    binding.textNoWords.setVisibility(View.GONE);
                    adapter.setWords(allWords);
                }
            });
        }).start();
    }
}
