package com.aminbadh.tdrprofessorslpm.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.adapter.AbsenceRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.adapter.StudentRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.custom.Class;
import com.aminbadh.tdrprofessorslpm.custom.Professor;
import com.aminbadh.tdrprofessorslpm.custom.Registration;
import com.aminbadh.tdrprofessorslpm.databinding.ActivityAbsenceBinding;
import com.aminbadh.tdrprofessorslpm.fragment.TimePickerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import static com.aminbadh.tdrprofessorslpm.custom.Constants.CLASSES_COL;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.CLASS_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.LEVELS_REF;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.PIN_DOC;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.PIN_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.PROF_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.REGISTRATIONS_COL;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarRef;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarStr;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.getInternetConnectionStatus;

// TODO: 10/30/2020 Add offline support.
public class AbsenceActivity extends AppCompatActivity {

    private static final String TAG = AbsenceActivity.class.getSimpleName();
    private StudentRecyclerAdapter studentsAdapterGr1, studentsAdapterGr2;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<String> absentStudents = new ArrayList<>();
    private AbsenceRecyclerAdapter absencesAdapter;
    private ActivityAbsenceBinding binding;
    private String fromTime, toTime;
    private boolean group1 = true;
    private String PINCode = "";
    private Professor professor;
    private FirebaseAuth auth;
    private Class mClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise the binding object.
        binding = ActivityAbsenceBinding.inflate(getLayoutInflater());
        // Set the Activity's content view.
        setContentView(binding.getRoot());
        // Initialise the FirebaseAuth object.
        auth = FirebaseAuth.getInstance();
        // Load the UI.
        loadUI();
    }

    @Override
    public void onBackPressed() {
        // If the user clicks on the back key,
        // inform him that changes won't be saved.
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_q)
                .setMessage(R.string.changes_not_save)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (arg0, arg1) ->
                        AbsenceActivity.this.finish()).create().show();
    }

    private void loadUI() {
        if (getInternetConnectionStatus(this)) {
            // If the user is connected, get the passed objects from the intent.
            getAbsenceIntent();
            // Set the Activity's title.
            setTitle(mClass.getClassName());
            // Get the pin code.
            getPINCode();
            // Setup the login feedback TextView.
            setupLoginFeedbackText();
            // Setup the RecyclerViews.
            setupRecyclerViews();
            // Setup the time picker.
            setupTimePicker();
            // Set the save button onClickListener.
            binding.buttonSave.setOnClickListener(view -> save());
        } else {
            // If the user is offline, inform him.
            binding.textViewOffline.setVisibility(View.VISIBLE);
            binding.constraintLayoutOnline.setVisibility(View.GONE);
        }
    }

    private void setupTimePicker() {
        // Set the TextViews text values.
        binding.textViewFromDisplay.setText(R.string.from);
        binding.textViewFrom.setText(R.string.set);
        binding.textViewToDisplay.setText(R.string.to);
        binding.textViewTo.setText(R.string.set);
        // Set the textViewFrom's onClickListener.
        binding.textViewFrom.setOnClickListener(view -> {
            // Create a DialogFragment object and initialise it to a new TimePickerFragment.
            DialogFragment timePicker = new TimePickerFragment((timePicker1, i, i1) -> {
                // Get the time and assign it to a String object.
                String time = i + ":" + i1;
                // Display this selected time in the textViewFromDisplay.
                String toDisplay = getString(R.string.from) + "  " + time;
                binding.textViewFromDisplay.setText(toDisplay);
                // Update the textViewFrom's text value to "Edit".
                binding.textViewFrom.setText(R.string.edit);
                // Assign the time String object to the global fromTime String object.
                fromTime = time;
            });
            // Show the DialogFragment object.
            timePicker.show(AbsenceActivity.super.getSupportFragmentManager(),
                    "Time Picker");
        });
        // Set the textViewTo's onClickListener.
        binding.textViewTo.setOnClickListener(view -> {
            // Create a DialogFragment object and initialise it to a new TimePickerFragment.
            DialogFragment timePicker = new TimePickerFragment((timePicker12, i, i1) -> {
                // Get the time and assign it to a String object.
                String time = i + ":" + i1;
                // Display this selected time in the textViewFromDisplay.
                String toDisplay = getString(R.string.to) + "  " + time;
                binding.textViewToDisplay.setText(toDisplay);
                // Update the textViewFrom's text value to "Edit".
                binding.textViewTo.setText(R.string.edit);
                // Assign the time String object to the global fromTime String object.
                toTime = time;
            });
            // Show the DialogFragment object.
            timePicker.show(AbsenceActivity.super.getSupportFragmentManager(),
                    "Time Picker");
        });
    }

    private void setupRecyclerViews() {
        // Setup the first group's RecyclerView.
        setupRecyclerViewStudentsGr1();
        // Setup the second group's RecyclerView.
        setupRecyclerViewStudentsGr2();
        // Setup the absences' RecyclerView.
        setupRecyclerViewAbsences();
        // Setup the group change behaviour.
        setupGroupChangeBehaviour();
    }

    private void setupGroupChangeBehaviour() {
        // Set the radioButtonGr1 as checked.
        binding.radioButtonGr1.setChecked(true);
        // Show the recyclerViewStudentsGr1 and hide the recyclerViewStudentsGr2.
        binding.recyclerViewStudentsGr1.setVisibility(View.VISIBLE);
        binding.recyclerViewStudentsGr2.setVisibility(View.GONE);
        // Set the radioGroupGr's onCheckedChangeListener.
        binding.radioGroupGr.setOnCheckedChangeListener(
                (radioGroup, i) -> {
                    if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonGr1) {
                        // If the checked radio button ID is radioButtonGr1's ID,
                        // Show the recyclerViewStudentsGr1 and hide the recyclerViewStudentsGr2.
                        binding.recyclerViewStudentsGr1.setVisibility(View.VISIBLE);
                        binding.recyclerViewStudentsGr2.setVisibility(View.GONE);
                        // Set the group1 boolean variable to true.
                        group1 = true;
                        // Clear the absent students ArrayList.
                        absentStudents.clear();
                        // Notify the adapter.
                        absencesAdapter.notifyDataSetChanged();
                    } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonGr2) {
                        // If the checked radio button ID is radioButtonGr1's ID,
                        // Show the recyclerViewStudentsGr2 and hide the recyclerViewStudentsGr1.
                        binding.recyclerViewStudentsGr1.setVisibility(View.GONE);
                        binding.recyclerViewStudentsGr2.setVisibility(View.VISIBLE);
                        // Set the group1 boolean variable to false.
                        group1 = false;
                        // Clear the absent students ArrayList.
                        absentStudents.clear();
                        // Notify the adapter.
                        absencesAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupRecyclerViewStudentsGr2() {
        // Create a new ArrayList of Strings.
        ArrayList<String> students = new ArrayList<>();
        if (mClass.getStudents2() != null) {
            // If the class's getStudents1 method don't returns null,
            // assign this value to the students ArrayList.
            students = mClass.getStudents2();
        }
        // Initialise the adapter.
        studentsAdapterGr2 = new StudentRecyclerAdapter(students, position -> {
            // Create a new String object and assign to it the student name.
            String name = studentsAdapterGr2.getStudentName(position);
            if (absentStudents.contains(name)) {
                // If the student is already marked as absent,
                // Inform the user and stop the operation.
                displaySnackbarRef(binding.getRoot(), R.string.this_student_is_absent);
                return;
            }
            // Add the student name to the absent students ArrayList.
            absentStudents.add(name);
            // Notify the adapter.
            absencesAdapter.notifyDataSetChanged();
        });
        // Setup the RecyclerView.
        binding.recyclerViewStudentsGr2.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewStudentsGr2.setHasFixedSize(true);
        binding.recyclerViewStudentsGr2.setAdapter(studentsAdapterGr2);
    }

    private void setupRecyclerViewStudentsGr1() {
        // Create a new ArrayList of Strings.
        ArrayList<String> students = new ArrayList<>();
        if (mClass.getStudents1() != null) {
            // If the class's getStudents1 method don't returns null,
            // assign this value to the students ArrayList.
            students = mClass.getStudents1();
        }
        // Initialise the adapter.
        studentsAdapterGr1 = new StudentRecyclerAdapter(students, position -> {
            // Create a new String object and assign to it the student name.
            String name = studentsAdapterGr1.getStudentName(position);
            if (absentStudents.contains(name)) {
                // If the student is already marked as absent, inform the user and stop the operation.
                displaySnackbarRef(binding.getRoot(), R.string.this_student_is_absent);
                return;
            }
            // Add the student name to the absent students ArrayList.
            absentStudents.add(name);
            // Notify the adapter.
            absencesAdapter.notifyDataSetChanged();
        });
        // Setup the RecyclerView.
        binding.recyclerViewStudentsGr1.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewStudentsGr1.setHasFixedSize(true);
        binding.recyclerViewStudentsGr1.setAdapter(studentsAdapterGr1);
    }

    private void setupRecyclerViewAbsences() {
        // Initialise the AbsenceRecyclerAdapter object.
        absencesAdapter = new AbsenceRecyclerAdapter(absentStudents, position -> {
            // Remove the student name from the absent students ArrayList.
            absentStudents.remove(position);
            // Notify the adapter.
            absencesAdapter.notifyDataSetChanged();
        });
        // Setup the RecyclerView.
        binding.recyclerViewAbsences.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewAbsences.setHasFixedSize(true);
        binding.recyclerViewAbsences.setAdapter(absencesAdapter);
    }

    private void setupLoginFeedbackText() {
        String loginFeedback = getString(R.string.logged_in_as) + " " + professor.getDisplayName();
        binding.textViewLoginFeedback.setText(loginFeedback);
    }

    private void getAbsenceIntent() {
        // Get the passed objects.
        professor = (Professor) getIntent().getSerializableExtra(PROF_OBJECT);
        mClass = (Class) getIntent().getSerializableExtra(CLASS_OBJECT);
        if (professor == null || mClass == null) {
            // If one of the objects is null, stop the app.
            Toast.makeText(this, R.string.a_prob_happened_getting_data,
                    Toast.LENGTH_LONG).show();
            AbsenceActivity.this.finish();
        }
    }

    private void getPINCode() {
        // Get the Document containing the PIN code.
        db.document(PIN_DOC).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // If the task was successful, get the PIN code.
                    PINCode = documentSnapshot.getString(PIN_FIELD);
                }).addOnFailureListener(e -> {
            // If the task failed, inform the user.
            Log.e(TAG, "onFailure: ", e);
            displaySnackbarStr(binding.getRoot(), e.getMessage());
        });
    }

    private void save() {
        // Get the text from editTextPIN and assign it to a new String object.
        String pin = binding.editTextPIN.getText().toString();
        if (pin.equals(PINCode)
                && getInternetConnectionStatus(this)
                && fromTime != null && toTime != null) {
            // If the user has internet connection and filled the required inputs,
            // Show the wait feedback.
            binding.constraintLayoutWaitRoot.setVisibility(View.VISIBLE);
            // Request focus for the root.
            binding.getRoot().requestFocus();
            // Create a new String object and initialise it using the getGroup method.
            String group = getGroup();
            // Create a Registration object using the passed data.
            Registration registration = new Registration(professor.getDisplayName(),
                    Objects.requireNonNull(auth.getCurrentUser()).getUid(), professor.getSubject(),
                    fromTime, toTime, group, absentStudents, System.currentTimeMillis());
            // Save the Registration object to the database.
            getInfoCol().add(registration).addOnCompleteListener(task -> {
                // Hide the wait feedback.
                binding.constraintLayoutWaitRoot.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    // If the task was successful, inform the user.
                    Toast.makeText(AbsenceActivity.this, getString(R.string.saved),
                            Toast.LENGTH_LONG).show();
                    // and exit the current Activity.
                    AbsenceActivity.this.finish();
                } else {
                    // If the task failed, log the exception.
                    Log.e(TAG, "onComplete: Task failed ", task.getException());
                    // Display the exception's message.
                    displaySnackbarStr(binding.getRoot(), Objects.requireNonNull(task
                            .getException()).getMessage());
                }
            });
        } else if (!getInternetConnectionStatus(this)) {
            // If the user is offline, inform him that he need internet connection.
            displaySnackbarRef(binding.getRoot(), R.string.you_are_offline);
        } else {
            if (fromTime == null || toTime == null) {
                // If the user didn't enter a time, inform him that he need to enter it.
                displaySnackbarRef(binding.getRoot(), R.string.please_enter_time);
            } else if (!pin.equals(PINCode)) {
                // If the PIN code was wrong, inform him.
                displaySnackbarRef(binding.getRoot(), R.string.wrong_pin_code);
            }
        }
    }

    private String getGroup() {
        // Create a String object.
        String group;
        if (group1) {
            // If the current selected group is group 1, assign the String object to "Group 1".
            group = getString(R.string.group_1);
        } else {
            // If not, assign the String object to "Group 1".
            group = getString(R.string.group_2);
        }
        // Return the String object.
        return group;
    }

    private CollectionReference getInfoCol() {
        return db.collection(LEVELS_REF).document(mClass.getLevel().getDocId())
                .collection(CLASSES_COL).document(mClass.getDocId()).collection(REGISTRATIONS_COL);
    }
}