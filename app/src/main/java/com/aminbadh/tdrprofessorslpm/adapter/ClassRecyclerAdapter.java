package com.aminbadh.tdrprofessorslpm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.custom.Class;
import com.aminbadh.tdrprofessorslpm.interfaces.OnMainListener;

import java.util.ArrayList;

public class ClassRecyclerAdapter extends RecyclerView.Adapter<ClassRecyclerAdapter.ClassHolder> {

    private final ArrayList<Class> classes;
    private final OnMainListener onMainListener;

    public ClassRecyclerAdapter(ArrayList<Class> classes, OnMainListener onMainListener) {
        this.classes = classes;
        this.onMainListener = onMainListener;
    }

    @NonNull
    @Override
    public ClassHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_main, parent, false);
        return new ClassHolder(view, onMainListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassHolder holder, int position) {
        holder.textViewName.setText(classes.get(position).getClassNum());
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    static class ClassHolder extends RecyclerView.ViewHolder {
        TextView textViewName;

        public ClassHolder(@NonNull View itemView, OnMainListener onMainListener) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewMainName);
            itemView.setOnClickListener(view -> onMainListener.onClickListener(getAdapterPosition()));
        }
    }
}
