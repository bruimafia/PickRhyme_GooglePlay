package com.gukov.pickrhyme.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gukov.pickrhyme.R;
import com.gukov.pickrhyme.object.Word;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private List<Word> words;

    class WordViewHolder extends RecyclerView.ViewHolder {
        private TextView text;

        WordViewHolder(View itemView) {
            super(itemView);
            this.text = itemView.findViewById(R.id.tv_word);
        }
    }

    public WordAdapter(List<Word> words) {
        this.words = words;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final WordViewHolder holder, final int position) {
        TextView tvName = holder.text;
        tvName.setText(words.get(position).getWord());
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

}