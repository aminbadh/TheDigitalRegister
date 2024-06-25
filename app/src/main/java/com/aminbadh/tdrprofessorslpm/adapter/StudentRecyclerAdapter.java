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
import java.util.Collections;

public class StudentRecyclerAdapter extends RecyclerView.Adapter<
        StudentRecyclerAdapter.StudentHolder> {

    private final OnMainListener onMainListener;
    private final ArrayList<String> students;
    private ArrayList<String> absences = new ArrayList<>();

    public StudentRecyclerAdapter(ArrayList<String> students, ArrayList<String> absences,
                                  OnMainListener onMainListener) {
        Collections.sort(students);
        this.students = students;
        this.onMainListener = onMainListener;
        if (!(absences == null || absences.isEmpty())) {
            this.absences = absences;
        }
    }

    @NonNull
    @Override
    public StudentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_student,
                parent, false);
        return new StudentHolder(view, onMainListener);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentHolder holder, int position) {
        String name = students.get(position);
        if (absences.contains(name)) {
            holder.imageViewAbsent.setVisibility(View.VISIBLE);
        }
        holder.textViewStudentName.setText(name);
        holder.imageViewAdd.setImageResource(R.drawable.ic_add_circle);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public String getStudentName(int position) {
        return students.get(position);
    }

    static class StudentHolder extends RecyclerView.ViewHolder {
        TextView textViewStudentName;
        ImageView imageViewAdd, imageViewAbsent;

        public StudentHolder(@NonNull View itemView, final OnMainListener onMainListener) {
            super(itemView);
            textViewStudentName = itemView.findViewById(R.id.textViewStudentName);
            imageViewAdd = itemView.findViewById(R.id.imageViewAdd);
            imageViewAbsent = itemView.findViewById(R.id.imageViewAbsent);
            itemView.setOnClickListener(view -> onMainListener.onClickListener(getAdapterPosition()));
        }
    }
}
