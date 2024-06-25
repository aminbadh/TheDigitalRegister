package com.aminbadh.tdrprofessorslpm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.adapter.RecentRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.custom.Registration;
import com.aminbadh.tdrprofessorslpm.databinding.ActivityMainBinding;
import com.aminbadh.tdrprofessorslpm.interfaces.OnEmpty;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import static com.aminbadh.tdrprofessorslpm.custom.Constants.LOADING_SIZE;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.MAIN_PREFS;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.REGISTRATIONS_COL;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.REGISTRATION_OBJECT;

public class MainActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecentRecyclerAdapter adapter;
    private ActivityMainBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise the binding object.
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // Set the Activity's content view.
        setContentView(binding.getRoot());
        // Set the Activity's title.
        setTitle(R.string.app_bar_main);
        // Initialise the auth object.
        auth = FirebaseAuth.getInstance();
        // Setup the RecyclerView.
        setupRecyclerView();
        // Setup the FloatingActionButton.
        setupFloatingActionButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // Start listening.
            adapter.startListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start the login Activity.
            startActivity(new Intent(this, LoginActivity.class));
            // Stop listening.
            adapter.stopListening();
            // Finish the current Activity.
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop listening.
        adapter.stopListening();
    }

    private void setupRecyclerView() {
        // Assert that the current user isn't null.
        assert auth.getCurrentUser() != null;
        // Create a Query.
        Query query = db.collectionGroup(REGISTRATIONS_COL)
                .whereEqualTo("professorId", auth.getCurrentUser().getUid())
                .orderBy("submitTime", Query.Direction.DESCENDING)
                .limit(getSharedPreferences(MAIN_PREFS, MODE_PRIVATE).getInt(LOADING_SIZE, 10));
        // Initialise the adapter.
        adapter = new RecentRecyclerAdapter(new FirestoreRecyclerOptions.Builder<Registration>()
                .setLifecycleOwner(this).setQuery(query, Registration.class).build(), new OnEmpty() {
            @Override
            public void onEmpty() {
                if (binding.clNoDataMain.getVisibility() == View.GONE) {
                    // Show the clNoDataMain.
                    binding.clNoDataMain.setAlpha(0f);
                    binding.clNoDataMain.setVisibility(View.VISIBLE);
                    binding.clNoDataMain.animate().alpha(1f)
                            .setDuration(getResources().getInteger
                                    (android.R.integer.config_mediumAnimTime))
                            .setListener(null);
                    binding.recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onData() {
                if (binding.recyclerView.getVisibility() == View.GONE) {
                    // Show the recyclerView.
                    binding.recyclerView.setAlpha(0f);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.recyclerView.animate().alpha(1f)
                            .setDuration(getResources().getInteger
                                    (android.R.integer.config_mediumAnimTime))
                            .setListener(null);
                    binding.clNoDataMain.setVisibility(View.GONE);
                }
            }
        }, position -> {
            // Create an Intent with an extra of the clicked registration and start it.
            Intent intent = new Intent(MainActivity.this, RecentAbsenceActivity.class);
            intent.putExtra(REGISTRATION_OBJECT, adapter.getRegistration(position));
            startActivity(intent);
        });
        // Set the RecyclerView's size as fixed.
        binding.recyclerView.setHasFixedSize(true);
        // Set the RecyclerView's layout manager.
        binding.recyclerView.setLayoutManager(new GridLayoutManager
                (this, calculateNoOfColumns(400)));
        // Set the RecyclerView's adapter.
        binding.recyclerView.setAdapter(adapter);
    }

    public int calculateNoOfColumns(float columnWidthDp) {
        // Create a DisplayMetrics object.
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        // Create a screenWidth variable.
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        // Return the number of possible columns.
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }

    private void setupFloatingActionButton() {
        // Start the absence Activity.
        binding.fab.setOnClickListener(view -> startActivity(
                new Intent(MainActivity.this, AbsenceActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Return true.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Start the Settings Activity.
        startActivity(new Intent(this, SettingsActivity.class));
        // Return true.
        return true;
    }
}