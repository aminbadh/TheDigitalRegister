package com.aminbadh.tdrprofessorslpm.fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

import static com.aminbadh.tdrprofessorslpm.custom.Constants.HOURS_24;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.MAIN_PREFS;

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
        // Return a TimePickerDialog object.
        return new TimePickerDialog(getActivity(),
                onTimeSetListener, hour, 0, requireActivity()
                .getSharedPreferences(MAIN_PREFS, Context.MODE_PRIVATE)
                .getBoolean(HOURS_24, true));
    }
}
