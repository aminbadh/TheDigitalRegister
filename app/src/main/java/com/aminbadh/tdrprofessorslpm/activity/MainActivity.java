package com.aminbadh.tdrprofessorslpm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.custom.Level;
import com.aminbadh.tdrprofessorslpm.custom.LevelMain;
import com.aminbadh.tdrprofessorslpm.custom.MAObject;
import com.aminbadh.tdrprofessorslpm.custom.Professor;
import com.aminbadh.tdrprofessorslpm.databinding.ActivityMainBinding;
import com.aminbadh.tdrprofessorslpm.fragment.LevelFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

import static com.aminbadh.tdrprofessorslpm.custom.Constants.LEVELS_REF;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.LEVEL_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.MAIN_ACTIVITY_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.PROF_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.RECENT_FRAGMENT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.REGISTER_FRAGMENT;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarStr;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Level> levels = new ArrayList<>();
    private LevelMain levelMainRegister;
    private ActivityMainBinding binding;
    private LevelMain levelMainRecent;
    private boolean register = true;
    private Professor professor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise the binding object.
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // Set Activity's content view.
        setContentView(binding.getRoot());
        // Get the Professor object.
        getProfessor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (levels.isEmpty()) {
            // Get the levels' data.
            getLevelsData();
        } else {
            // Setup the BottomNavigationView.
            setupBottomNav();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // Create a new MAObject object and initialise it.
        MAObject object = new MAObject(register, levels);
        // Add this object to the outState Bundle.
        outState.putSerializable(MAIN_ACTIVITY_OBJECT, object);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        // Create and initialise a new MAObject object.
        MAObject object = (MAObject) savedInstanceState.getSerializable(MAIN_ACTIVITY_OBJECT);
        // Assign the object's register boolean value to the global register variable.
        register = object.isRegister();
        // Assign the object's levels ArrayList to the global one.
        levels = object.getLevels();
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void getProfessor() {
        // Get the passed Professor object an assign it to the global Professor variable.
        professor = (Professor) getIntent().getSerializableExtra(PROF_OBJECT);
        if (professor == null) {
            // If the Professor object is null, stop the app.
            Toast.makeText(this, R.string.a_prob_happened_getting_data,
                    Toast.LENGTH_LONG).show();
            MainActivity.this.finish();
        }
    }

    private void setupBottomNav() {
        // Initialise the LevelMain objects.
        levelMainRegister = new LevelMain(REGISTER_FRAGMENT, levels, professor);
        levelMainRecent = new LevelMain(RECENT_FRAGMENT, levels, professor);
        // Set the BottomNavigationView's onNavigationItemSelectedListener.
        binding.bottomNavMain.setOnNavigationItemSelectedListener(
                item -> {
                    Fragment selectedFragment = null;
                    if (item.getItemId() == R.id.nav_register) {
                        // If the selected item is the "register" item,
                        // assign the Fragment object to a new LevelFragment.
                        selectedFragment = LevelFragment.newInstance(levelMainRegister);
                        register = true;
                    } else if (item.getItemId() == R.id.nav_recent) {
                        // If the selected item is the "recent" item,
                        // assign the Fragment object to a new LevelFragment.
                        selectedFragment = LevelFragment.newInstance(levelMainRecent);
                        register = false;
                    }
                    // Display the Fragment object.
                    assert selectedFragment != null;
                    getSupportFragmentManager().beginTransaction().replace(
                            R.id.fragmentContainer, selectedFragment).commit();
                    // Return true.
                    return true;
                });
        if (register) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, LevelFragment.newInstance(levelMainRegister))
                    .commitAllowingStateLoss();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, LevelFragment.newInstance(levelMainRecent))
                    .commitAllowingStateLoss();
        }
    }

    private void getLevelsData() {
        // Show the wait feedback.
        binding.constraintLayoutWaitMain.setVisibility(View.VISIBLE);
        // Get the levels whereIn the levels that the professor teach.
        db.collection(LEVELS_REF).whereIn(LEVEL_FIELD, professor.getLevels())
                .get().addOnCompleteListener(task -> {
            // Set the wait feedback visibility to Gone.
            binding.constraintLayoutWaitMain.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                // If the task was successful, loop in each documentSnapshot and do the following.
                for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                    // Convert the documentSnapshot to a Level object.
                    Level level = documentSnapshot.toObject(Level.class);
                    // Set the Level object Id.
                    level.setDocId(documentSnapshot.getId());
                    // Add the Level object.
                    levels.add(level);
                }
            } else {
                // If the task failed, log the exception.
                Log.e(TAG, "onComplete: Task Failed", task.getException());
                // Display the exception's message.
                displaySnackbarStr(binding.getRoot(), Objects.requireNonNull(task.getException()).getMessage());
            }
            // Setup the BottomNavigationView.
            setupBottomNav();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.men_logout) {
            // Logout the user.
            FirebaseAuth.getInstance().signOut();
            // Start an Intent going to the LoginActivity class.
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            // Finish the current Activity.
            MainActivity.this.finish();
        } else if (item.getItemId() == R.id.men_settings) {
            // Create an Intent object going to the SettingActivity class.
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            // Add the Professor object as an extra.
            intent.putExtra(PROF_OBJECT, professor);
            // Start the intent.
            startActivity(intent);
        }
        return true;
    }
}