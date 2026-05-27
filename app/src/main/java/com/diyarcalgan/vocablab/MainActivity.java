package com.diyarcalgan.vocablab; // Paket adın değişmesin!

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // Kartı yöneteceğiz

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private CardView flashCard; // YENİ: Kartımızın kendisi
    private TextView textWord, textMeaning, textSentence;
    private Button btnShowMeaning;
    private ImageView menuIcon, iconSpeech;
    private TextToSpeech tts;
    private TextView textKnownCount, textRepeatCount; // Yeni metin alanlarımız
    private int knownCount = 0; // Sağa kaydırma sayacı
    private int repeatCount = 0; // Sola kaydırma sayacı
    // YENİ: İçinde "Word" nesneleri tutan dinamik listeler (ArrayList)
    private java.util.ArrayList<Word> englishWordsList;
    private java.util.ArrayList<Word> germanWordsList;
    private java.util.ArrayList<Word> currentWordsList;
    private java.util.ArrayList<Word> myEnglishLabList;
    private java.util.ArrayList<Word> myGermanLabList;
    // AKILLI POSTACIMIZ (Yeni sayfadan gelen veriyi yakalayacak)
    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> addWordLauncher;
    private String currentWorkingLanguage = "EN"; // Başlangıçta İngilizce modundayız
    private int currentIndex = 0;

    // Kaydırma (Swipe) hesaplamaları için değişkenler
    private float xDown = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Arayüz elemanlarını bağlama
        flashCard = findViewById(R.id.flashCard); // Kartı bağladık
        textWord = findViewById(R.id.textWord);
        textSentence = findViewById(R.id.textSentence);
        textSentence = findViewById(R.id.textSentence);
        textMeaning = findViewById(R.id.textMeaning);
        btnShowMeaning = findViewById(R.id.btnShowMeaning);
        menuIcon = findViewById(R.id.menuIcon);
        iconSpeech = findViewById(R.id.iconSpeech);
        textKnownCount = findViewById(R.id.textKnownCount);
        textRepeatCount = findViewById(R.id.textRepeatCount);
        Button btnAddWordPage = findViewById(R.id.btnAddWordPage);
        btnAddWordPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Niyetimizi (Intent) oluşturuyoruz
                android.content.Intent intent = new android.content.Intent(MainActivity.this, AddWordActivity.class);

                // 2. YENİ: O anki çalışma dilimizi gizli bir not olarak pakete koyuyoruz
                intent.putExtra("secret_note", currentWorkingLanguage);

                // 3. Sayfayı başlatıyoruz
                addWordLauncher.launch(intent);
            }
        });
        // --- ENVANTER BUTONU GÖREVİ ---
        Button btnInventory = findViewById(R.id.btnInventory);
        btnInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentWordsList == null || currentWordsList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Şu anki laboratuvar tamamen boş!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] displayArray = new String[currentWordsList.size()];
                for (int i = 0; i < currentWordsList.size(); i++) {
                    Word w = currentWordsList.get(i);
                    displayArray[i] = w.getEnglish() + " - " + w.getTurkish();
                }

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Envanter (Toplam: " + currentWordsList.size() + " Kelime)");

                builder.setItems(displayArray, new android.content.DialogInterface.OnClickListener() {
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        currentIndex = which;
                        updateWordOnScreen();
                    }
                });

                builder.setNegativeButton("Kapat", null);
                builder.show();
            }
        });
        // ------------------------------
        // --- OOP FABRİKASI: Kelime nesnelerimizi üretiyoruz ---
        englishWordsList = new java.util.ArrayList<>();
        myEnglishLabList = new java.util.ArrayList<>();
        myGermanLabList = new java.util.ArrayList<>();
        // --- AKILLI POSTACI: GELEN PAKETİ DOĞRU LABORATUVARA DAĞIT ---
        addWordLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        android.content.Intent data = result.getData();

                        // Paketin içindeki kelimeyi ve GİZLİ NOTU cımbızla çekiyoruz
                        String eng = data.getStringExtra("key_english");
                        String tr = data.getStringExtra("key_turkish");
                        String sentence = data.getStringExtra("key_sentence");
                        String langNote = data.getStringExtra("secret_note");

                        // GİZLİ NOTA BAKARAK DOĞRU LİSTEYE ATIYORUZ
                        if (langNote != null && langNote.equals("DE")) {
                            myGermanLabList.add(new Word(eng, tr, sentence));
                            Toast.makeText(MainActivity.this, "Almanca Laboratuvarına Eklendi!", Toast.LENGTH_SHORT).show();
                        } else {
                            myEnglishLabList.add(new Word(eng, tr, sentence));
                            Toast.makeText(MainActivity.this, "İngilizce Laboratuvarına Eklendi!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        // --------------------------------------------------------------
        englishWordsList.add(new Word("Apple", "Elma", "I eat an _ every day."));
        englishWordsList.add(new Word("Computer", "Bilgisayar", "My _ is very fast."));
        englishWordsList.add(new Word("Develop", "Geliştirmek", "I want to _ a new app."));

        germanWordsList = new java.util.ArrayList<>();
        germanWordsList.add(new Word("Apfel", "Elma", "Ich esse jeden Tag einen _."));
        germanWordsList.add(new Word("Computer", "Bilgisayar", "Mein _ ist sehr schnell."));
        germanWordsList.add(new Word("Entwickeln", "Geliştirmek", "Ich möchte eine App _."));

        currentWordsList = englishWordsList; // Uygulama açıldığında İngilizce listesi aktif olsun
        // -----------------------------------------------------
        updateWordOnScreen();

        // TTS (Ses) Motorunu Başlatma
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        // Hoparlör Görevi
        iconSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak(textWord.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        // Menü Görevi
        // --- 4 SEÇENEKLİ YENİ MENÜ GÖREVİ ---
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, menuIcon);
                // 4 Farklı Destemiz
                popup.getMenu().add("Sistem - İngilizce");
                popup.getMenu().add("Sistem - Almanca");
                popup.getMenu().add("Benim Lab - İngilizce");
                popup.getMenu().add("Benim Lab - Almanca");

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals("Sistem - İngilizce")) {
                            currentWordsList = englishWordsList;
                            currentWorkingLanguage = "EN"; // Sisteme "İngilizce modundayız" diyoruz
                            tts.setLanguage(java.util.Locale.ENGLISH); // Hoparlörü İngilizce yap

                        } else if (item.getTitle().equals("Sistem - Almanca")) {
                            currentWordsList = germanWordsList;
                            currentWorkingLanguage = "DE"; // Sisteme "Almanca modundayız" diyoruz
                            tts.setLanguage(java.util.Locale.GERMAN); // Hoparlörü Almanca yap

                        } else if (item.getTitle().equals("Benim Lab - İngilizce")) {
                            if (myEnglishLabList.isEmpty()) {
                                Toast.makeText(MainActivity.this, "İngilizce laboratuvarın boş!", Toast.LENGTH_SHORT).show();
                                return false; // Çökmeyi engelle
                            }
                            currentWordsList = myEnglishLabList;
                            currentWorkingLanguage = "EN";
                            tts.setLanguage(java.util.Locale.ENGLISH);

                        } else if (item.getTitle().equals("Benim Lab - Almanca")) {
                            if (myGermanLabList.isEmpty()) {
                                Toast.makeText(MainActivity.this, "Almanca laboratuvarın boş!", Toast.LENGTH_SHORT).show();
                                return false; // Çökmeyi engelle
                            }
                            currentWordsList = myGermanLabList;
                            currentWorkingLanguage = "DE";
                            tts.setLanguage(java.util.Locale.GERMAN);
                        }

                        currentIndex = 0; // Listeyi başa sar
                        updateWordOnScreen(); // Ekranı yeni listeye göre güncelle
                        return true;
                    }
                });
                popup.show();
            }
        });

        // Anlam Gösterme
        btnShowMeaning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textMeaning.setVisibility(View.VISIBLE);
            }
        });

        // --- YENİ: TINDER TARZI KAYDIRMA (SWIPE) MANTIĞI ---
        flashCard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Parmağı ilk bastığımız anki X koordinatını kaydet
                        xDown = event.getRawX();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Parmağı sürükledikçe kartı da sürükle (Matematik kısmı)
                        float movementX = event.getRawX() - xDown;
                        view.setTranslationX(movementX);
                        // Kart sağa sola giderken biraz da eğilsin (Tinder efekti)
                        view.setRotation(movementX / 20f);
                        return true;

                    case MotionEvent.ACTION_UP:
                        // Parmağı ekrandan çektiğimiz an
                        float finalX = event.getRawX() - xDown;

                        // Eğer kart 300 pikselden fazla SAĞA kaydırıldıysa (BİLİYORUM)
                        if (finalX > 300) {
                            // SAĞA KAYDIRILDI
                            currentWordsList.get(currentIndex).setStatus(1);

                            // YENİ: Sayacı 1 arttır ve ekrana yazdır
                            knownCount++;
                            textKnownCount.setText("Bildiğim: " + knownCount);

                            Toast.makeText(MainActivity.this, "Öğrenildi!", Toast.LENGTH_SHORT).show();
                            goToNextWord(view);
                        }
                        else if (finalX < -300) {
                            // SOLA KAYDIRILDI
                            currentWordsList.get(currentIndex).setStatus(-1);

                            // YENİ: Sayacı 1 arttır ve ekrana yazdır
                            repeatCount++;
                            textRepeatCount.setText("Tekrar: " + repeatCount);

                            Toast.makeText(MainActivity.this, "Tekrar Edilecek!", Toast.LENGTH_SHORT).show();
                            goToNextWord(view);
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    // Kart kaydırılınca yeni kelimeye geçiren özel metod
    private void goToNextWord(View card) {
        currentIndex++;
        // YENİ: Artık .length değil, ArrayList kullandığımız için .size() diyoruz
        if (currentIndex >= currentWordsList.size()) {
            currentIndex = 0;
        }
        updateWordOnScreen();

        card.setTranslationX(0);
        card.setRotation(0);
    }

    private void updateWordOnScreen() {
        Word currentObj = currentWordsList.get(currentIndex);

        textWord.setText(currentObj.getEnglish());
        textMeaning.setText(currentObj.getTurkish());
        textMeaning.setVisibility(View.INVISIBLE);

        // --- YENİ: BOŞLUK DOLDURMA SİHRİ ---
        // 1. Cümleyi ve hedef kelimeyi elimize alıyoruz
        String sentence = currentObj.getExampleSentence();
        String targetWord = currentObj.getEnglish();

        // 2. Cümlenin içindeki hedef kelimeyi bulup "____" ile değiştiriyoruz.
        // (Kelime cümlenin içinde küçük harfle geçiyor olabilir diye .toLowerCase() ile küçük harflisini de aratıyoruz)
        String hiddenSentence = sentence.replace(targetWord, "_____").replace(targetWord.toLowerCase(), "_____");

        // 3. Sansürlü cümleyi ekrana basıyoruz
        textSentence.setText(hiddenSentence);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}