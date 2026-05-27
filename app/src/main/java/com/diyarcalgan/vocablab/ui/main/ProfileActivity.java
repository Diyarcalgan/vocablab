package com.diyarcalgan.vocablab.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.diyarcalgan.vocablab.R;
import com.diyarcalgan.vocablab.data.WordRepository;
import com.diyarcalgan.vocablab.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private WordRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new WordRepository(getApplication());
        setupBottomNav();

        binding.btnResetAll.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Verileri Sıfırla")
                    .setMessage("Tüm kelimeler ve ilerleme silinecek. Emin misiniz?")
                    .setPositiveButton("Evet", (dialog, which) -> {
                        new Thread(() -> {
                            repository.clearAll();
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Tüm veriler silindi", Toast.LENGTH_SHORT).show();
                            });
                        }).start();
                    })
                    .setNegativeButton("Hayır", null)
                    .show();
        });
    }

    private void setupBottomNav() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_list) {
                startActivity(new Intent(this, WordListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_study) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }
}
