package com.diyarcalgan.vocablab.ui.main;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    public interface OnWordDeleteListener {
        void onWordDelete(Word word);
    }

    public void setWords(List<Word> words) {
        this.words = words;
        notifyDataSetChanged();
    }

    public void setOnWordDeleteListener(OnWordDeleteListener listener) {
        this.deleteListener = listener;
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
        
        if ("DE".equals(word.getLanguage())) {
            holder.textLang.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E67E22")));
        } else {
            holder.textLang.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3498DB")));
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onWordDelete(word);
            }
        });
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView textOriginal, textTranslated, textLang;
        ImageView btnDelete;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            textOriginal = itemView.findViewById(R.id.textItemOriginal);
            textTranslated = itemView.findViewById(R.id.textItemTranslated);
            textLang = itemView.findViewById(R.id.textItemLang);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}
