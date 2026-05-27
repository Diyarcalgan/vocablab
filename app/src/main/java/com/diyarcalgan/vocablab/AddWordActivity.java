package com.diyarcalgan.vocablab;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddWordActivity extends AppCompatActivity {

    private EditText editEnglishWord, editTurkishMeaning, editExampleSentence;
    private Button btnSaveWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        // 1. Ana sayfadan gelen GİZLİ NOTU (Çalışma dilini) cebimize alıyoruz
        String activeLanguage = getIntent().getStringExtra("secret_note");

        // 2. Sahnedeki nesneleri bağlıyoruz
        editEnglishWord = findViewById(R.id.editEnglishWord);
        editTurkishMeaning = findViewById(R.id.editTurkishMeaning);
        editExampleSentence = findViewById(R.id.editExampleSentence);
        btnSaveWord = findViewById(R.id.btnSaveWord);

        // 3. Kaydet Butonuna Basıldığında Olacaklar
        btnSaveWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String english = editEnglishWord.getText().toString();
                String turkish = editTurkishMeaning.getText().toString();
                String sentence = editExampleSentence.getText().toString();

                if (english.isEmpty() || turkish.isEmpty()) {
                    Toast.makeText(AddWordActivity.this, "Lütfen en azından kelimeyi ve anlamını yazın!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 4. Verileri paketliyoruz
                android.content.Intent returnIntent = new android.content.Intent();
                returnIntent.putExtra("key_english", english);
                returnIntent.putExtra("key_turkish", turkish);
                returnIntent.putExtra("key_sentence", sentence);

                // 5. YENİ: Cebimizdeki gizli notu da pakete ekleyip geri gönderiyoruz!
                returnIntent.putExtra("secret_note", activeLanguage);

                // Paketi sisteme teslim et ve sayfayı kapat
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}