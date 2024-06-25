package com.aminbadh.tdrprofessorslpm.dialog;

import android.app.ProgressDialog;
import android.content.Context;

public class WaitDialog extends ProgressDialog {

    public WaitDialog(Context context) {
        super(context);
    }

    @Override
    public void onBackPressed() {
        // Don't do anything.
    }
}
