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
            String wordText = binding.editEnglishWord.getText().toString().trim();
            String trMeaning = binding.editTurkishMeaning.getText().toString().trim();
            String sentence = binding.editExampleSentence.getText().toString().trim();

            if (wordText.isEmpty() || trMeaning.isEmpty()) {
                Toast.makeText(this, "Kelime ve anlamı boş bırakılamaz!", Toast.LENGTH_SHORT).show();
                return;
            }

            // İŞTE HATAYI ÇÖZEN SATIR BURASI: İlk parametre olarak "EN" ekledik
            viewModel.saveWord("EN", wordText, trMeaning, sentence);

            Toast.makeText(this, "Kelime Eklendi!", Toast.LENGTH_SHORT).show();

            binding.editEnglishWord.setText("");
            binding.editTurkishMeaning.setText("");
            binding.editExampleSentence.setText("");

            finish();
        });
    }
}