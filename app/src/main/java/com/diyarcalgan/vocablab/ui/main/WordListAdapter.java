package com.diyarcalgan.vocablab.ui.main;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.diyarcalgan.vocablab.R;
import com.diyarcalgan.vocablab.data.model.Word;
import java.util.ArrayList;
import java.util.List;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.WordViewHolder> {
    private List<Word> words = new ArrayList<>();
    private OnWordDeleteListener deleteListener;
    private OnWordClickListener clickListener;

    public interface OnWordDeleteListener {
        void onWordDelete(Word word);
    }

    public interface OnWordClickListener {
        void onWordClick(Word word);
    }

    public void setWords(List<Word> words) {
        this.words = words;
        notifyDataSetChanged();
    }

    public void setOnWordDeleteListener(OnWordDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setOnWordClickListener(OnWordClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = words.get(position);
        holder.textOriginal.setText(word.getOriginalWord());
        holder.textTranslated.setText(word.getTranslatedWord());
        holder.textLang.setText(word.getLanguage());
        
        if (word.isKnown()) {
            holder.iconStatus.setImageResource(android.R.drawable.checkbox_on_background);
            holder.iconStatus.setColorFilter(Color.parseColor("#006E2F"));
            holder.progressBar.setProgress(100);
            holder.textStatusLabel.setText("Öğrenildi");
        } else {
            holder.iconStatus.setImageResource(android.R.drawable.ic_menu_edit);
            holder.iconStatus.setColorFilter(Color.parseColor("#767586"));
            holder.progressBar.setProgress(30);
            holder.textStatusLabel.setText("Çalışılıyor");
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onWordDelete(word);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onWordClick(word);
            }
        });
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView textOriginal, textTranslated, textLang, textStatusLabel;
        ImageView btnDelete, iconStatus;
        ProgressBar progressBar;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            textOriginal = itemView.findViewById(R.id.textItemOriginal);
            textTranslated = itemView.findViewById(R.id.textItemTranslated);
            textLang = itemView.findViewById(R.id.textItemLang);
            textStatusLabel = itemView.findViewById(R.id.textStatusLabel);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
            iconStatus = itemView.findViewById(R.id.iconStatus);
            progressBar = itemView.findViewById(R.id.itemProgress);
        }
    }
}
