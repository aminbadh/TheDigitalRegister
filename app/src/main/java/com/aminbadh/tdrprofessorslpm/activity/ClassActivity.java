package com.aminbadh.tdrprofessorslpm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.adapter.ClassRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.custom.Class;
import com.aminbadh.tdrprofessorslpm.custom.Level;
import com.aminbadh.tdrprofessorslpm.custom.Professor;
import com.aminbadh.tdrprofessorslpm.databinding.ListMainBinding;
import com.aminbadh.tdrprofessorslpm.interfaces.OnMainListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import static com.aminbadh.tdrprofessorslpm.custom.Constants.CLASSES_COL;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.CLASS_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.CLASS_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.INTENT_FROM;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.LEVELS_REF;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.LEVEL_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.PROF_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.RECENT_FRAGMENT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.REGISTER_FRAGMENT;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarStr;

public class ClassActivity extends AppCompatActivity implements OnMainListener {

    private static final String TAG = ClassActivity.class.getSimpleName();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<Class> classes = new ArrayList<>();
    private ListMainBinding binding;
    private Professor professor;
    private String resource;
    private Level level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise the binding object.
        binding = ListMainBinding.inflate(getLayoutInflater());
        // Set the Activity's content view.
        setContentView(binding.getRoot());
        // Get the objects from the intent.
        getClassIntent();
        // Set the Activity's title.
        setTitle(level.getLevel());
        // Get the data.
        getData();
    }

    private void getClassIntent() {
        // Get objects from the intent.
        professor = (Professor) getIntent().getSerializableExtra(PROF_OBJECT);
        level = (Level) getIntent().getSerializableExtra(LEVEL_OBJECT);
        resource = getIntent().getStringExtra(INTENT_FROM);
        if (professor == null || level == null || resource == null) {
            // If one of the objects is null, stop the app.
            Toast.makeText(this, R.string.a_prob_happened_getting_data,
                    Toast.LENGTH_LONG).show();
            ClassActivity.this.finish();
        }
    }

    private void getData() {
        // Show the wait feedback.
        binding.constraintLayoutWaitMainList.setVisibility(View.VISIBLE);
        // Get the classes.
        getQuery().get().addOnCompleteListener(task -> {
            // Set the wait feedback visibility to Gone.
            binding.constraintLayoutWaitMainList.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                // If the task was successful, loop in the results.
                for (QueryDocumentSnapshot documentSnapshot : Objects
                        .requireNonNull(task.getResult())) {
                    // Convert the QueryDocumentSnapshot object to a Class object.
                    Class mClass = documentSnapshot.toObject(Class.class);
                    // Add the document ID to the Class object.
                    mClass.setDocId(documentSnapshot.getId());
                    // Add the Level object to the Class object.
                    mClass.setLevel(level);
                    // Add the Class object to the classes ArrayList.
                    classes.add(mClass);
                }
            } else {
                // If the task failed, log the exception.
                Log.e(TAG, "onComplete: Task Failed", task.getException());
                // Display the exception's error.
                displaySnackbarStr(binding.getRoot(), Objects.requireNonNull(task
                        .getException()).getMessage());
            }
            // Setup the RecyclerView.
            setupRecyclerView();
        });
    }

    private Query getQuery() {
        // Create a new Query object.
        Query query;
        // Create a CollectionReference for the classes.
        CollectionReference classes = db.collection(LEVELS_REF)
                .document(level.getDocId()).collection(CLASSES_COL);
        // Create a String variables with current level in it and log it.
        String currentLevel = level.getLevel();
        Log.i(TAG, "getQuery: Current Level = " + currentLevel);
        // Create a switch statement.
        switch (currentLevel) {
            case "1S":
                // If the current level is "1S", search for Documents in that Collection.
                query = classes.whereIn(CLASS_FIELD, professor.getLevel1S());
                break;
            case "2Sc":
                // If the current level is "2Sc", search for Documents in that Collection.
                query = classes.whereIn(CLASS_FIELD, professor.getLevel2Sc());
                break;
            case "3M":
                // If the current level is "3M", search for Documents in that Collection.
                query = classes.whereIn(CLASS_FIELD, professor.getLevel3M());
                break;
            case "3Sc":
                // If the current level is "3Sc", search for Documents in that Collection.
                query = classes.whereIn(CLASS_FIELD, professor.getLevel3Sc());
                break;
            case "4L":
                // If the current level is "4L", search for Documents in that Collection.
                query = classes.whereIn(CLASS_FIELD, professor.getLevel4L());
                break;
            case "4M":
                // If the current level is "4M", search for Documents in that Collection.
                query = classes.whereIn(CLASS_FIELD, professor.getLevel4M());
                break;
            case "4Sc":
                // If the current level is "4Sc", search for Documents in that Collection.
                query = classes.whereIn(CLASS_FIELD, professor.getLevel4Sc());
                break;
            case "4T":
                // If the current level is "4T", search for Documents in that Collection.
                query = classes.whereIn(CLASS_FIELD, professor.getLevel4T());
                break;
            default:
                // If none of the cases are true, initialise the Query object to null.
                query = null;
                break;
        }
        // Return the Query object.
        return query;
    }

    private void setupRecyclerView() {
        if (classes.isEmpty()) {
            // If the classes ArrayList is empty, show the no data feedback.
            binding.constraintLayoutNoDataMain.setVisibility(View.VISIBLE);
            binding.recyclerViewMain.setVisibility(View.GONE);
        } else {
            // If there is data in the classes ArrayList, sort it by the class number.
            Collections.sort(classes, (aClass, t1) -> aClass.getClassNum().compareTo(t1.getClassNum()));
            // Create a ClassRecyclerAdapter object and initialise it.
            ClassRecyclerAdapter adapter = new ClassRecyclerAdapter(classes, this);
            // Setup the RecyclerView.
            binding.recyclerViewMain.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerViewMain.setHasFixedSize(true);
            binding.recyclerViewMain.setAdapter(adapter);
        }
    }

    @Override
    public void onClickListener(int position) {
        // Create an Intent object.
        Intent intent = new Intent();
        if (resource.equals(REGISTER_FRAGMENT)) {
            // If the intent was coming from the register fragment,
            // initialise the Intent object to an Intent going to the AbsenceActivity class.
            intent = new Intent(ClassActivity.this, AbsenceActivity.class);
        } else if (resource.equals(RECENT_FRAGMENT)) {
            // If the intent was coming from the recent fragment,
            // initialise the Intent object to an Intent going to the RecentActivity class.
            intent = new Intent(ClassActivity.this, RecentActivity.class);
        }
        // Add the Professor object as an extra to the Intent object.
        intent.putExtra(PROF_OBJECT, professor);
        // Add the Class object as an extra to the Intent object.
        intent.putExtra(CLASS_OBJECT, classes.get(position));
        // Start the Intent.
        startActivity(intent);
    }
}