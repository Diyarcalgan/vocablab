package com.diyarcalgan.vocablab.ui.addword;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.diyarcalgan.vocablab.databinding.ActivityAddWordBinding;

public class AddWordActivity extends AppCompatActivity {
    private ActivityAddWordBinding binding;
    private AddWordViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddWordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(AddWordViewModel.class);

        binding.btnSaveWord.setOnClickListener(v -> {
            String eng = binding.editEnglishWord.getText().toString().trim();
            String tur = binding.editTurkishMeaning.getText().toString().trim();
            String sent = binding.editExampleSentence.getText().toString().trim();

            if (eng.isEmpty() || tur.isEmpty()) {
                Toast.makeText(this, "Boş bırakılamaz!", Toast.LENGTH_SHORT).show();
                return;
            }
            // 4 parametreli constructor
            viewModel.saveWord("EN", eng, tur, sent);
            finish();
        });
    }
}