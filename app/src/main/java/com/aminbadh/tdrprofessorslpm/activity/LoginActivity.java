package com.aminbadh.tdrprofessorslpm.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.custom.LocaleHelper;
import com.aminbadh.tdrprofessorslpm.custom.Professor;
import com.aminbadh.tdrprofessorslpm.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static com.aminbadh.tdrprofessorslpm.custom.Constants.PROF_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.ROLE_DEV;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.ROLE_PROF;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.USERS_COLLECTION;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarRef;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarStr;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set activity's title.
        setTitle(R.string.login);
        // Initialise the binding object.
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        // Set Activity's content view.
        setContentView(binding.getRoot());
        // Initialise the auth object.
        auth = FirebaseAuth.getInstance();
        // Set the wait feedback visibility to gone.
        setWaitFeedbackVisibility(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            // Disable the login button.
            enableLoginButton(false);
            // Set the wait feedback visibility to visible.
            setWaitFeedbackVisibility(true);
            // Check user document.
            checkUserDoc(auth.getCurrentUser().getUid());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    public void login(View view) {
        // Retrieve texts from the email and password EditTexts.
        String email = binding.editTextEmailAddress.getText().toString();
        String password = binding.editTextPassword.getText().toString();
        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            // if the passed data is empty,
            // inform the user that he must fill all required inputs.
            displaySnackbarRef(binding.getRoot(), R.string.fill_inputs);
        } else {
            // Disable the login button.
            enableLoginButton(false);
            // Set the wait feedback visibility to visible.
            setWaitFeedbackVisibility(true);
            // Request the focus for the root.
            binding.getRoot().requestFocus();
            // Login the user using the given data.
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Create a FirebaseUser variable and assign to it the current user.
                            FirebaseUser user = auth.getCurrentUser();
                            // Log the ID of the current user.
                            assert user != null;
                            Log.i(TAG, "onComplete: User signed in successfully => " +
                                    user.getUid());
                            // Check user's role.
                            checkUserDoc(user.getUid());
                        } else {
                            // Set the wait feedback visibility to Gone.
                            setWaitFeedbackVisibility(false);
                            // Enable the login button.
                            enableLoginButton(true);
                            // Log the exception.
                            Log.e(TAG, "onComplete: User sign in failed", task.getException());
                            // Display the exception's message.
                            displaySnackbarStr(binding.getRoot(), Objects
                                    .requireNonNull(task.getException()).getMessage());
                        }
                    });
        }
    }

    private void checkUserDoc(String id) {
        db.collection(USERS_COLLECTION).document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Professor professor = documentSnapshot.toObject(Professor.class);
                    // Update the UI.
                    assert professor != null;
                    updateUI(professor);
                }).addOnFailureListener(e -> {
            // Set the wait feedback visibility to Gone.
            setWaitFeedbackVisibility(false);
            // Enable the login button.
            enableLoginButton(true);
            // Log the exception.
            Log.e(TAG, "onFailure: Doc", e);
            // Display the exception's message.
            displaySnackbarStr(binding.getRoot(), e.getMessage());
            // Logout the user.
            auth.signOut();
        });
    }

    private void updateUI(Professor professor) {
        // Set the wait feedback visibility to Gone.
        setWaitFeedbackVisibility(false);
        String role = professor.getRole();
        if (role.equals(ROLE_PROF) || role.equals(ROLE_DEV)) {
            // If the user's role is a Professor or a Developer,
            // create a new Intent object going to the MainActivity class.
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            // Add the Professor object as an extra.
            intent.putExtra(PROF_OBJECT, professor);
            // Start the intent.
            startActivity(intent);
            // Finish the current activity.
            LoginActivity.this.finish();
        } else {
            // Enable the login button.
            enableLoginButton(true);
            // Inform the user that his role isn't compatible.
            displaySnackbarRef(binding.getRoot(), R.string.current_role_false);
            // Logout the user.
            auth.signOut();
        }
    }

    private void setWaitFeedbackVisibility(boolean b) {
        if (b) {
            binding.constraintLayoutWait.setVisibility(View.VISIBLE);
        } else {
            binding.constraintLayoutWait.setVisibility(View.GONE);
        }
    }

    private void enableLoginButton(boolean b) {
        binding.buttonLogin.setEnabled(b);
    }
}