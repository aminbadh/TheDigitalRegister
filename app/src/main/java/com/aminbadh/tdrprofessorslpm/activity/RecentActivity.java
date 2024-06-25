package com.aminbadh.tdrprofessorslpm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.adapter.RecentRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.custom.Class;
import com.aminbadh.tdrprofessorslpm.custom.Professor;
import com.aminbadh.tdrprofessorslpm.custom.Registration;
import com.aminbadh.tdrprofessorslpm.databinding.ListMainBinding;
import com.aminbadh.tdrprofessorslpm.interfaces.OnMainListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

import static com.aminbadh.tdrprofessorslpm.custom.Constants.CLASSES_COL;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.CLASS_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.LEVELS_REF;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.PROF_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.REGISTRATIONS_COL;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.REGISTRATION_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarRef;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarStr;

public class RecentActivity extends AppCompatActivity implements OnMainListener, View.OnClickListener {

    private final ArrayList<Registration> registrations = new ArrayList<>();
    private static final String TAG = RecentActivity.class.getSimpleName();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecentRecyclerAdapter adapter;
    private DocumentSnapshot lastVisible;
    private ListMainBinding binding;
    private Professor professor;
    private FirebaseAuth auth;
    private Class mClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise the binding object.
        binding = ListMainBinding.inflate(getLayoutInflater());
        // Set the Activity's content view.
        setContentView(binding.getRoot());
        // Initialise the auth object.
        auth = FirebaseAuth.getInstance();
        // Load the UI.
        loadUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!registrations.isEmpty()) {
            // If the registrations ArrayList isn't empty, clear it.
            registrations.clear();
        }
        // Get data.
        getData();
    }

    private void loadUI() {
        // Show the wait feedback.
        binding.constraintLayoutWaitMainList.setVisibility(View.VISIBLE);
        // Get the objects passed by the Intent.
        getRecentIntent();
        // Set the Activity's title.
        setTitle(mClass.getClassName());
        // Setup the RecyclerView.
        setupRecyclerView();
    }

    private void getRecentIntent() {
        // Get the objects passed from the intent.
        professor = (Professor) getIntent().getSerializableExtra(PROF_OBJECT);
        mClass = (Class) getIntent().getSerializableExtra(CLASS_OBJECT);
        if (professor == null || mClass == null) {
            // If one of the objects is null, stop the app.
            Toast.makeText(this, R.string.a_prob_happened_getting_data,
                    Toast.LENGTH_LONG).show();
            RecentActivity.this.finish();
        }
    }

    private void getData() {
        // Get the data and limit it to 10 documents.
        getQuery().limit(10).get().addOnCompleteListener(task -> {
            // Hide the wait feedback.
            binding.constraintLayoutWaitMainList.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                if (Objects.requireNonNull(task.getResult()).size() > 0) {
                    // If the task was successful, and the result isn't empty, loop in the results.
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        // Convert the QueryDocumentSnapshot object to a Registration object.
                        Registration registration = documentSnapshot.toObject(Registration.class);
                        // Set the Registration object's document ID.
                        registration.setDocId(documentSnapshot.getId());
                        // Add the Registration object to the registrations ArrayList.
                        registrations.add(registration);
                    }
                    // Initialise the DocumentSnapshot object to the last DS in the results.
                    lastVisible = task.getResult().getDocuments()
                            .get(task.getResult().size() - 1);
                }
            } else {
                // If the task failed, log the exception.
                Log.e(TAG, "onComplete: Task Failed", task.getException());
                // Display the exception's message.
                displaySnackbarStr(binding.getRoot(), Objects.requireNonNull(task
                        .getException()).getMessage());
                return;
            }
            // Update the data (UI).
            updateData();
        });
    }

    private void setupRecyclerView() {
        // Create and initialise the RecentRecyclerAdapter object.
        adapter = new RecentRecyclerAdapter(registrations,
                this, this, this);
        // Set up the RecyclerView.
        binding.recyclerViewMain.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewMain.setHasFixedSize(true);
        binding.recyclerViewMain.setAdapter(adapter);

    }

    private void updateData() {
        if (registrations.isEmpty()) {
            // If the registration ArrayList is empty, show the no data feedback.
            binding.constraintLayoutNoDataMain.setVisibility(View.VISIBLE);
            binding.recyclerViewMain.setVisibility(View.GONE);
        } else {
            // If there was data in the registrations ArrayList, add fake data.
            addFakeData();
            // Hide the no data feedback.
            binding.constraintLayoutNoDataMain.setVisibility(View.GONE);
            binding.recyclerViewMain.setVisibility(View.VISIBLE);
            // Notify the adapter.
            adapter.notifyDataSetChanged();
        }
    }

    private Query getQuery() {
        return db.collection(LEVELS_REF).document(mClass.getLevel().getDocId())
                .collection(CLASSES_COL).document(mClass.getDocId()).collection(REGISTRATIONS_COL)
                .whereEqualTo("professorId", Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .orderBy("submitTime", Query.Direction.DESCENDING);
    }

    @Override
    public void onClickListener(int position) {
        // Create an Intent object going to the RecentAbsenceActivity class.
        Intent intent = new Intent(RecentActivity.this,
                RecentAbsenceActivity.class);
        // Add the Professor object as an extra.
        intent.putExtra(PROF_OBJECT, professor);
        // Add the Class object as an extra.
        intent.putExtra(CLASS_OBJECT, mClass);
        // Add the resource String object as an extra.
        intent.putExtra(REGISTRATION_OBJECT, registrations.get(position));
        // Start the Intent.
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        // Disable the button.
        view.setEnabled(false);
        // Get the data after the lastVisible DocumentSnapshot object
        // with a limit of 10 documents.
        getQuery().startAfter(lastVisible).limit(10).get().addOnCompleteListener(task -> {
            // Enable the button.
            view.setEnabled(true);
            if (task.isSuccessful()) {
                // If the task was successful, remove the fake data.
                removeFakeData();
                if (Objects.requireNonNull(task.getResult()).size() > 0) {
                    // If the result isn't empty, loop in the results.
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        // Convert the QueryDocumentSnapshot object to a Registration object.
                        Registration registration = documentSnapshot.toObject(Registration.class);
                        // Set the Registration object's document ID.
                        registration.setDocId(documentSnapshot.getId());
                        // Add the Registration object to the registrations ArrayList.
                        registrations.add(registration);
                    }
                    // Initialise the DocumentSnapshot object to the last DS in the results.
                    lastVisible = task.getResult().getDocuments()
                            .get(task.getResult().size() - 1);
                } else {
                    // If the result was empty, inform the user.
                    displaySnackbarRef(binding.getRoot(), R.string.no_more_data);
                }
                // Add fake data.
                addFakeData();
                // Notify the adapter.
                adapter.notifyDataSetChanged();
            } else {
                // If the task failed, log the exception.
                Log.e(TAG, "onComplete: Task Failed", task.getException());
                // Display the exception's message.
                displaySnackbarStr(binding.getRoot(), Objects.requireNonNull(task
                        .getException()).getMessage());
            }
        });
    }

    private void addFakeData() {
        registrations.add(new Registration());
    }

    private void removeFakeData() {
        registrations.remove(registrations.size() - 1);
    }
}