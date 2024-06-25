package com.aminbadh.tdrprofessorslpm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.custom.Level;
import com.aminbadh.tdrprofessorslpm.interfaces.OnMainListener;

import java.util.ArrayList;

public class LevelRecyclerAdapter extends RecyclerView.Adapter<LevelRecyclerAdapter.LevelHolder> {

    private final ArrayList<Level> levels;
    private final OnMainListener onMainListener;

    public LevelRecyclerAdapter(ArrayList<Level> levels, OnMainListener onMainListener) {
        this.levels = levels;
        this.onMainListener = onMainListener;
    }

    @NonNull
    @Override
    public LevelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_level, parent, false);
        return new LevelHolder(view, onMainListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelHolder holder, int position) {
        holder.textViewName.setText(levels.get(position).getLevel());
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    static class LevelHolder extends RecyclerView.ViewHolder {
        TextView textViewName;

        public LevelHolder(@NonNull View itemView, OnMainListener onMainListener) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewMainName);
            itemView.setOnClickListener(view -> onMainListener.onClickListener(getAdapterPosition()));
        }
    }
}
