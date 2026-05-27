package com.diyarcalgan.vocablab.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.diyarcalgan.vocablab.R;
import com.diyarcalgan.vocablab.data.WordRepository;
import com.diyarcalgan.vocablab.databinding.ActivityWordListBinding;
import com.diyarcalgan.vocablab.data.model.Word;
import com.diyarcalgan.vocablab.ui.addword.AddWordActivity;
import java.util.ArrayList;
import java.util.List;

public class WordListActivity extends AppCompatActivity {
    private static final String TAG = "WordListActivity";
    private ActivityWordListBinding binding;
    private WordRepository repository;
    private WordListAdapter adapter;
    private List<Word> cachedWords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityWordListBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            repository = new WordRepository(getApplication());
            setupRecyclerView();
            setupBottomNav();

            binding.fabAddWord.setOnClickListener(v -> startActivity(new Intent(this, AddWordActivity.class)));

            binding.editSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilter(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            
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

        adapter.setOnWordClickListener(word -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("wordId", word.getId());
            intent.putExtra("lang", word.getLanguage());
            startActivity(intent);
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
                        cachedWords = allWords != null ? allWords : new ArrayList<>();
                        applyFilter(binding.editSearch.getText() != null ? binding.editSearch.getText().toString() : "");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Load words error", e);
            }
        }).start();
    }

    private void applyFilter(String query) {
        if (binding == null || adapter == null) return;
        String q = query != null ? query.trim().toLowerCase() : "";
        if (q.isEmpty()) {
            adapter.setWords(cachedWords);
            binding.textWordCount.setText((cachedWords != null ? cachedWords.size() : 0) + " kelime öğreniliyor");
            return;
        }

        List<Word> filtered = new ArrayList<>();
        if (cachedWords != null) {
            for (Word w : cachedWords) {
                if (w == null) continue;
                String o = w.getOriginalWord() != null ? w.getOriginalWord().toLowerCase() : "";
                String t = w.getTranslatedWord() != null ? w.getTranslatedWord().toLowerCase() : "";
                if (o.contains(q) || t.contains(q)) {
                    filtered.add(w);
                }
            }
        }
        adapter.setWords(filtered);
        binding.textWordCount.setText(filtered.size() + " sonuç");
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
