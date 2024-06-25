package com.aminbadh.tdrprofessorslpm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.interfaces.OnMainListener;

import java.util.ArrayList;

public class AbsenceRecyclerAdapter extends RecyclerView.Adapter<AbsenceRecyclerAdapter.AbsenceHolder> {

    private final ArrayList<String> absentStudents;
    private final OnMainListener onMainListener;

    public AbsenceRecyclerAdapter(ArrayList<String> absentStudents, OnMainListener onMainListener) {
        this.absentStudents = absentStudents;
        this.onMainListener = onMainListener;
    }

    @NonNull
    @Override
    public AbsenceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_student, parent, false);
        return new AbsenceHolder(view, onMainListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsenceHolder holder, int position) {
        holder.textViewStudentName.setText(absentStudents.get(position));
        holder.imageView.setImageResource(R.drawable.ic_remove_circle);
    }

    @Override
    public int getItemCount() {
        return absentStudents.size();
    }

    static class AbsenceHolder extends RecyclerView.ViewHolder {
        TextView textViewStudentName;
        ImageView imageView;

        public AbsenceHolder(@NonNull View itemView, final OnMainListener onMainListener) {
            super(itemView);
            textViewStudentName = itemView.findViewById(R.id.textViewStudentName);
            imageView = itemView.findViewById(R.id.imageViewAdd);
            itemView.setOnClickListener(view -> onMainListener.onClickListener(getAdapterPosition()));
        }
    }
}
