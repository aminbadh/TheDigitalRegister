package com.aminbadh.tdrprofessorslpm.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.custom.Registration;
import com.aminbadh.tdrprofessorslpm.interfaces.OnEmpty;
import com.aminbadh.tdrprofessorslpm.interfaces.OnMainListener;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class RecentRecyclerAdapter extends FirestoreRecyclerAdapter<Registration,
        RecentRecyclerAdapter.RecentHolder> {

    private static final String TAG = RecentRecyclerAdapter.class.getSimpleName();
    private final OnEmpty onEmpty;
    private final OnMainListener onMainListener;

    public RecentRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Registration> options,
                                 OnEmpty onEmpty, OnMainListener onMainListener) {
        super(options);
        this.onEmpty = onEmpty;
        this.onMainListener = onMainListener;
    }

    public static String reformatTime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy",
                Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return formatter.format(calendar.getTime());
    }

    @Override
    public void onDataChanged() {
        if (getItemCount() <= 0) {
            onEmpty.onEmpty();
        } else {
            onEmpty.onData();
        }
    }

    @NonNull
    @Override
    public RecentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_recent, parent, false);
        return new RecentHolder(view, onMainListener);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecentHolder holder, int position,
                                    @NonNull Registration model) {
        holder.textViewClass.setText(model.getClassName());
        holder.textViewDate.setText(reformatTime(model.getSubmitTime()));
        holder.textViewFrom.setText(model.getFromTime());
        holder.textViewTo.setText(model.getToTime());
    }

    @Override
    public void onError(@NonNull FirebaseFirestoreException e) {
        super.onError(e);
        Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        onEmpty.onEmpty();
    }

    public Registration getRegistration(int i) {
        Registration registration = getItem(i);
        registration.setDocRef(getSnapshots().getSnapshot(i).getReference().getPath());
        return registration;
    }

    static class RecentHolder extends RecyclerView.ViewHolder {
        TextView textViewClass, textViewDate, textViewFrom, textViewTo;

        public RecentHolder(@NonNull View itemView, OnMainListener onMainListener) {
            super(itemView);
            textViewClass = itemView.findViewById(R.id.textViewClass);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewFrom = itemView.findViewById(R.id.textViewFromR);
            textViewTo = itemView.findViewById(R.id.textViewToR);
            itemView.setOnClickListener(view -> onMainListener.onClickListener(getAdapterPosition()));
        }
    }
}
