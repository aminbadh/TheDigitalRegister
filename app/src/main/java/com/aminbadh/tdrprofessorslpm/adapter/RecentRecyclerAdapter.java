package com.aminbadh.tdrprofessorslpm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.custom.Registration;
import com.aminbadh.tdrprofessorslpm.interfaces.OnMainListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RecentRecyclerAdapter extends RecyclerView.Adapter<RecentRecyclerAdapter.RecentHolder> {

    private final ArrayList<Registration> registrations;
    private final Context context;
    private final OnMainListener onMainListener;
    private final View.OnClickListener onClickListener;

    public RecentRecyclerAdapter(ArrayList<Registration> registrations, Context context,
                                 OnMainListener onMainListener,
                                 View.OnClickListener onClickListener) {
        this.registrations = registrations;
        this.context = context;
        this.onMainListener = onMainListener;
        this.onClickListener = onClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == (getItemCount() - 1)) {
            return 2;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public RecentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_recent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_more, parent, false);
        }
        return new RecentHolder(view, onMainListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentHolder holder, int position) {
        if (position == (getItemCount() - 1)) {
            holder.buttonLoadMore.setOnClickListener(onClickListener);
        } else {
            Registration currentRegistration = registrations.get(position);
            holder.textViewDate.setText(reformatTime(currentRegistration.getSubmitTime()));
            String from = context.getString(R.string.from) + " " + currentRegistration.getFromTime();
            holder.textViewFrom.setText(from);
            String to = context.getString(R.string.to) + " " + currentRegistration.getToTime();
            holder.textViewTo.setText(to);
        }
    }

    @Override
    public int getItemCount() {
        return registrations.size();
    }

    public static String reformatTime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy",
                Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return formatter.format(calendar.getTime());
    }

    class RecentHolder extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewFrom, textViewTo;
        Button buttonLoadMore;

        public RecentHolder(@NonNull View itemView, final OnMainListener onMainListener) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewFrom = itemView.findViewById(R.id.textViewFromR);
            textViewTo = itemView.findViewById(R.id.textViewToR);
            buttonLoadMore = itemView.findViewById(R.id.buttonLoadMore);
            if (!(getAdapterPosition() == (getItemCount() - 1))) {
                itemView.setOnClickListener(view ->
                        onMainListener.onClickListener(getAdapterPosition()));
            }
        }
    }
}
