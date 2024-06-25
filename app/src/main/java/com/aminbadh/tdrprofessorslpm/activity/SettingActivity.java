package com.aminbadh.tdrprofessorslpm.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.custom.LocaleHelper;
import com.aminbadh.tdrprofessorslpm.custom.Professor;
import com.aminbadh.tdrprofessorslpm.databinding.ActivitySettingBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import static com.aminbadh.tdrprofessorslpm.custom.Constants.ARABIC;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.ENGLISH;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.FRENCH;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.PROF_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarRef;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarStr;

public class SettingActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    private static final String TAG = SettingActivity.class.getSimpleName();
    private ActivitySettingBinding binding;
    private boolean changed = false;
    private Professor professor;
    private FirebaseAuth auth;
    private String local;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the Activity's title.
        setTitle(R.string.settings);
        // Initialise the binding object.
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        // initialise the FirebaseAuth object.
        auth = FirebaseAuth.getInstance();
        // Set the Activity's content view.
        setContentView(binding.getRoot());
        // Get the Professor object.
        getProfessor();
        // Setup the login feedback TextView.
        setupLoginFeedback();
        // Setup the Spinner.
        setupSpinner();
        // Setup email change.
        setupEmailChange();
        // Setup password change.
        setupPasswordChange();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    private void getProfessor() {
        // Get the passed Professor object an assign it to the global Professor variable.
        professor = (Professor) getIntent().getSerializableExtra(PROF_OBJECT);
        if (professor == null) {
            // If the Professor object is null, stop the app.
            Toast.makeText(this, R.string.a_prob_happened_getting_data,
                    Toast.LENGTH_LONG).show();
            SettingActivity.this.finish();
        }
    }

    private void setupLoginFeedback() {
        // Create and initialise a new String object.
        String loginFeedback = getString(R.string.logged_in_as) + " " + professor.getDisplayName();
        // Set the textViewLoginFeedbackS's text.
        binding.textViewLoginFeedbackS.setText(loginFeedback);
    }

    private void setupSpinner() {
        // Create and initialise a new ArrayAdapter.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        // Set the adapter's dropDownViewResource.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set the spinner's onItemSelectedListener.
        binding.spinner.setOnItemSelectedListener(this);
        // Set the spinner's adapter.
        binding.spinner.setAdapter(adapter);
        // Set the spinner's selected item.
        setupSpinnerSelection();
        // Set the textViewApplyLanguage's onClickListener.
        binding.buttonApplyLanguageChange.setOnClickListener(view -> {
            // Change the locale.
            LocaleHelper.setLocale(SettingActivity.this, local);
            // Inform the user that he need to restart the app to change the language.
            displaySnackbarRef(binding.getRoot(), R.string.restart_to_change_the_language);
            // Disable the button.
            binding.buttonApplyLanguageChange.setEnabled(false);
            // Update the changed boolean variable to true.
            changed = true;
        });
    }

    private void setupSpinnerSelection() {
        switch (LocaleHelper.getLanguage(this)) {
            case ENGLISH:
                // If the current locale is "en", select the english field.
                binding.spinner.setSelection(0);
                break;
            case FRENCH:
                // If the current locale is "fr", select the french field.
                binding.spinner.setSelection(1);
                break;
            case ARABIC:
                // If the current locale is "ar", select the arabic field.
                binding.spinner.setSelection(2);
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                // If the selected item is english, update the local variable to "en".
                local = ENGLISH;
                break;
            case 1:
                // If the selected item is french, update the local variable to "fr".
                local = FRENCH;
                break;
            case 2:
                local = ARABIC;
                // If the selected item is arabic, update the local variable to "ar".
                break;
        }
        if (!changed) {
            // Enable the button.
            binding.buttonApplyLanguageChange.setEnabled(!LocaleHelper
                    .getLanguage(SettingActivity.this).equals(local));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Do noting.
    }

    private void setupEmailChange() {
        binding.buttonChangeEmail.setOnClickListener(view -> {
            // Get the given email address.
            String email = binding.editTextNewEmailAddress.getText().toString();
            if (email.isEmpty()) {
                // If the email variable is empty, inform the user.
                displaySnackbarRef(binding.getRoot(), R.string.fill_inputs);
            } else {
                // Inform the user that the operation started.
                displaySnackbarRef(binding.getRoot(), R.string.please_wait);
                // Update the user's email address.
                Objects.requireNonNull(auth.getCurrentUser()).updateEmail(email).
                        addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // If the task was successful, log a message.
                                Log.i(TAG, "onComplete: Email Address changed.");
                                // Display a message for the user.
                                displaySnackbarRef(binding.getRoot(), R.string.saved);
                            } else {
                                // If the task failed, log the exception.
                                Log.e(TAG, "onComplete: ", task.getException());
                                // Display the exception's message.
                                displaySnackbarStr(binding.getRoot(),
                                        Objects.requireNonNull(task.getException()).getMessage());
                            }
                        });
            }
        });
    }

    private void setupPasswordChange() {
        binding.buttonChangePassword.setOnClickListener(view -> {
            // Get the passed password.
            String password = binding.editTextNewPassword.getText().toString();
            if (password.isEmpty()) {
                // If the given password is empty, inform the user.
                displaySnackbarRef(binding.getRoot(), R.string.fill_inputs);
            } else {
                // Inform the user that the operation started.
                displaySnackbarRef(binding.getRoot(), R.string.please_wait);
                // Update the user's password.
                Objects.requireNonNull(auth.getCurrentUser()).updatePassword(password).
                        addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // If the task was successful, log a message.
                                Log.i(TAG, "onComplete: Password changed.");
                                // Display a message for the user.
                                displaySnackbarRef(binding.getRoot(), R.string.saved);
                            } else {
                                // If the task failed, log the exception.
                                Log.e(TAG, "onComplete: ", task.getException());
                                // Display the exception's message.
                                displaySnackbarStr(binding.getRoot(),
                                        Objects.requireNonNull(task.getException()).getMessage());
                            }
                        });
            }
        });
    }
}