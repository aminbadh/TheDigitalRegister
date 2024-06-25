package com.aminbadh.tdrprofessorslpm.fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    private final TimePickerDialog.OnTimeSetListener onTimeSetListener;

    public TimePickerFragment(TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        this.onTimeSetListener = onTimeSetListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create a Calendar object.
        Calendar calendar = Calendar.getInstance();
        // Get current hours from the Calendar object.
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        // Get current minutes from the Calendar object.
        int minute = calendar.get(Calendar.MINUTE);
        // Return a TimePickerDialog object.
        return new TimePickerDialog(getActivity(),
                onTimeSetListener,
                hour, minute, DateFormat.is24HourFormat(getActivity()));
    }
}
