package com.aminbadh.tdrprofessorslpm.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.adapter.AbsenceRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.adapter.RecentRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.adapter.StudentRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.custom.Class;
import com.aminbadh.tdrprofessorslpm.custom.Professor;
import com.aminbadh.tdrprofessorslpm.custom.Registration;
import com.aminbadh.tdrprofessorslpm.databinding.ActivityAbsenceBinding;
import com.aminbadh.tdrprofessorslpm.fragment.TimePickerFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import static com.aminbadh.tdrprofessorslpm.custom.Constants.ABSENCES_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.CLASSES_COL;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.CLASS_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.FROM_TIME_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.GROUP_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.LEVELS_REF;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.PIN_DOC;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.PIN_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.PROF_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.REGISTRATIONS_COL;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.REGISTRATION_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.TO_TIME_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarRef;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarStr;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.getInternetConnectionStatus;

// TODO: 10/30/2020 Add offline support.
public class RecentAbsenceActivity extends AppCompatActivity {

    private static final String TAG = RecentAbsenceActivity.class.getSimpleName();
    private StudentRecyclerAdapter studentsAdapterGr1, studentsAdapterGr2;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean group1, boolean1, boolean2, boolean3, boolean4;
    private ArrayList<String> absentStudents = new ArrayList<>();
    private AbsenceRecyclerAdapter absencesAdapter;
    private ActivityAbsenceBinding binding;
    private Registration registration;
    private String fromTime, toTime;
    private String PINCode = "";
    private Professor professor;
    private Class mClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise the binding object.
        binding = ActivityAbsenceBinding.inflate(getLayoutInflater());
        // Set the Activity's content view.
        setContentView(binding.getRoot());
        // Load the UI.
        loadUI();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_q)
                .setMessage(R.string.changes_not_save)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (arg0, arg1) ->
                        RecentAbsenceActivity.this.finish()).create().show();
    }

    private void loadUI() {
        if (getInternetConnectionStatus(this)) {
            // If the user has internet connection, get the passed objects from the Intent.
            getRecentAbsenceIntent();
            // Set the Activity's title.
            setTitle(RecentRecyclerAdapter.reformatTime(registration.getSubmitTime()));
            // Get the pin code.
            getPINCode();
            // Set up the login feedback TextView.
            setUpLoginFeedbackText();
            // Set up the RecyclerViews.
            setUpRecyclerViews();
            // Set up the time picker.
            setUpTimePicker();
            // Set the save button's onClickListener.
            binding.buttonSave.setOnClickListener(view -> save());
        } else {
            // If the user don't have internet connection, show the offline feedback.
            binding.textViewOffline.setVisibility(View.VISIBLE);
            binding.constraintLayoutOnline.setVisibility(View.GONE);
        }
    }

    private void getRecentAbsenceIntent() {
        // Get the passed objects from the intent.
        professor = (Professor) getIntent().getSerializableExtra(PROF_OBJECT);
        mClass = (Class) getIntent().getSerializableExtra(CLASS_OBJECT);
        registration = (Registration) getIntent().getSerializableExtra(REGISTRATION_OBJECT);
        if (professor == null || mClass == null || registration == null) {
            // If one of the objects is null, stop the app.
            Toast.makeText(this, R.string.a_prob_happened_getting_data,
                    Toast.LENGTH_LONG).show();
            RecentAbsenceActivity.this.finish();
        }
    }

    private void getPINCode() {
        // Get the Document containing the PIN code.
        db.document(PIN_DOC).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // If the task was successful, get the PIN code.
                    PINCode = documentSnapshot.getString(PIN_FIELD);
                }).addOnFailureListener(e -> {
            // If the task failed inform the user.
            Log.e(TAG, "onFailure: ", e);
            displaySnackbarStr(binding.getRoot(), e.getMessage());
        });
    }

    private void setUpLoginFeedbackText() {
        String loginFeedback = getString(R.string.logged_in_as) + " " + professor.getDisplayName();
        binding.textViewLoginFeedback.setText(loginFeedback);
    }

    private void setUpRecyclerViews() {
        // Setup the first group RecyclerView.
        setUpRecyclerViewStudentsGr1();
        // Setup the second group RecyclerView.
        setUpRecyclerViewStudentsGr2();
        // Setup the absences RecyclerView.
        setUpRecyclerViewAbsences();
        // Setup the group change behaviour.
        setUpGroupChangeBehaviour();
    }

    private void setUpRecyclerViewStudentsGr1() {
        // Initialise the adapter.
        studentsAdapterGr1 = new StudentRecyclerAdapter(mClass.getStudents1(), position -> {
            // Create a new String variable and assign to it the student name.
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

    private void setUpRecyclerViewStudentsGr2() {
        // Initialise the adapter.
        studentsAdapterGr2 = new StudentRecyclerAdapter(mClass.getStudents2(), position -> {
            // Create a new String variable and assign to it the student name.
            String name = studentsAdapterGr2.getStudentName(position);
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
        binding.recyclerViewStudentsGr2.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewStudentsGr2.setHasFixedSize(true);
        binding.recyclerViewStudentsGr2.setAdapter(studentsAdapterGr2);
    }

    private void setUpRecyclerViewAbsences() {
        // Initialise the absent students to the absences list from the Registration object.
        absentStudents = registration.getAbsences();
        // Initialise the AbsenceRecyclerAdapter object.
        absencesAdapter = new AbsenceRecyclerAdapter(absentStudents, position -> {
            absentStudents.remove(position);
            absencesAdapter.notifyDataSetChanged();
        });
        // Setup the recyclerViewAbsences.
        binding.recyclerViewAbsences.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewAbsences.setHasFixedSize(true);
        binding.recyclerViewAbsences.setAdapter(absencesAdapter);
    }

    private void setUpGroupChangeBehaviour() {
        if (registration.getGroup().equals(getString(R.string.group_1))) {
            // If the Registration group is group 1,
            // set the group1 boolean variable to true.
            group1 = true;
            // Set the radioButtonGr1 to checked.
            binding.radioButtonGr1.setChecked(true);
            // Show the recyclerViewStudentsGr1.
            binding.recyclerViewStudentsGr1.setVisibility(View.VISIBLE);
            binding.recyclerViewStudentsGr2.setVisibility(View.GONE);
        } else if (registration.getGroup().equals(getString(R.string.group_2))) {
            // If the Registration group is group 2,
            // set the group1 boolean variable to false.
            group1 = false;
            // Set the radioButtonGr2 to checked.
            binding.radioButtonGr2.setChecked(true);
            // Show the recyclerViewStudentsGr2.
            binding.recyclerViewStudentsGr1.setVisibility(View.GONE);
            binding.recyclerViewStudentsGr2.setVisibility(View.VISIBLE);
        }
        // Set the radioGroupGr's onCheckedChangeListener.
        binding.radioGroupGr.setOnCheckedChangeListener(
                (radioGroup, i) -> {
                    if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonGr1) {
                        // If the selected button ID is radioButtonGr1's ID,
                        // show recyclerViewStudentsGr1.
                        binding.recyclerViewStudentsGr1.setVisibility(View.VISIBLE);
                        binding.recyclerViewStudentsGr2.setVisibility(View.GONE);
                        // Set the group1 boolean variable to true.
                        group1 = true;
                        // Clear the absent students ArrayList.
                        absentStudents.clear();
                        // Notify the adapter.
                        absencesAdapter.notifyDataSetChanged();
                    } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonGr2) {
                        // If the selected button ID is radioButtonGr2's ID,
                        // Show recyclerViewStudentsGr2.
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

    private void setUpTimePicker() {
        // Assign the fromTime/toTime String object
        // to the Registration object's from time/to time.
        fromTime = registration.getFromTime();
        toTime = registration.getToTime();
        // Create a String object and assign it to the following * 2.
        String from = getString(R.string.from) + " " + fromTime;
        String to = getString(R.string.to) + " " + toTime;
        // Set the TextViews text values to the following.
        binding.textViewFromDisplay.setText(from);
        binding.textViewToDisplay.setText(to);
        binding.textViewFrom.setText(R.string.edit);
        binding.textViewTo.setText(R.string.edit);
        // Set the textViewFrom's onClickListener.
        binding.textViewFrom.setOnClickListener(view -> {
            // Create a DialogFragment object and assign to a new TimePickerFragment.
            DialogFragment timePicker = new TimePickerFragment((timePicker1, i, i1) -> {
                // Create a Sting object and assign to the passed time.
                String time = i + ":" + i1;
                // Create an other String object and assign it to the following.
                String toDisplay = getString(R.string.from) + "  " + time;
                // Set the textViewFromDisplay's to the following.
                binding.textViewFromDisplay.setText(toDisplay);
                // Update the fromTime String variable to the time String variable.
                fromTime = time;
            });
            // Show the created DialogFragment.
            timePicker.show(RecentAbsenceActivity.this.getSupportFragmentManager(),
                    "Time Picker");
        });
        // Set the textViewTo's onClickListener.
        binding.textViewTo.setOnClickListener(view -> {
            // Create a DialogFragment object and assign to a new TimePickerFragment.
            DialogFragment timePicker = new TimePickerFragment((timePicker12, i, i1) -> {
                // Create a Sting object and assign to the passed time.
                String time = i + ":" + i1;
                // Create an other String object and assign it to the following.
                String toDisplay = getString(R.string.to) + "  " + time;
                // Set the textViewFromDisplay's to the following.
                binding.textViewToDisplay.setText(toDisplay);
                // Update the fromTime String variable to the time String variable.
                toTime = time;
            });
            // Show the created DialogFragment.
            timePicker.show(RecentAbsenceActivity.this.getSupportFragmentManager(),
                    "Time Picker");
        });
    }

    private void save() {
        // Create a DocumentReference object and assign to it the document the will be updated.
        DocumentReference registrationRef = getRegistrationDocRef();
        // Create a String object and assign to it the editTextPIN text.
        String pin = binding.editTextPIN.getText().toString();
        if (pin.equals(PINCode)
                && getInternetConnectionStatus(this)
                && fromTime != null && toTime != null) {
            // Show the constraintLayoutWaitRoot.
            binding.constraintLayoutWaitRoot.setVisibility(View.VISIBLE);
            // Request focus for the layout root.
            binding.getRoot().requestFocus();
            // Create a String object and initialise it using getGroup method.
            String group = getGroup();
            if (!fromTime.equals(registration.getFromTime())) {
                // If the fromTime String object isn't equal to the Registration from time,
                // update the from time field with the fromTime String object.
                registrationRef.update(FROM_TIME_FIELD, fromTime)
                        .addOnSuccessListener(aVoid -> boolean1 = true)
                        .addOnFailureListener(e -> {
                            boolean1 = false;
                            Log.e(TAG, "onFailure: ", e);
                            displaySnackbarStr(binding.getRoot(), e.getMessage());
                        });
            } else {
                // If the fromTime didn't change, skip and set the boolean1 to true.
                boolean1 = true;
            }
            if (!toTime.equals(registration.getToTime())) {
                // If the toTime String object isn't equal to the Registration to time,
                // update the to time field with the toTime String object.
                registrationRef.update(TO_TIME_FIELD, toTime)
                        .addOnSuccessListener(aVoid -> boolean2 = true)
                        .addOnFailureListener(e -> {
                            boolean2 = false;
                            Log.e(TAG, "onFailure: ", e);
                            displaySnackbarStr(binding.getRoot(), e.getMessage());
                        });
            } else {
                // If the toTime didn't change, skip and set the boolean2 to true.
                boolean2 = true;
            }
            if (!group.equals(registration.getGroup())) {
                // If the group String object isn't equal to the Registration group,
                // update the to time field with the group String object.
                registrationRef.update(GROUP_FIELD, group)
                        .addOnSuccessListener(aVoid -> boolean3 = true)
                        .addOnFailureListener(e -> {
                            boolean3 = false;
                            Log.e(TAG, "onFailure: ", e);
                            displaySnackbarStr(binding.getRoot(), e.getMessage());
                        });
            } else {
                // If the group didn't change, skip and set the boolean3 to true.
                boolean3 = true;
            }
            registrationRef.update(ABSENCES_FIELD, absentStudents)
                    .addOnSuccessListener(aVoid -> {
                        boolean4 = true;
                        // Call the displayFeedback method.
                        displayFeedback();
                    })
                    .addOnFailureListener(e -> {
                        boolean4 = false;
                        Log.e(TAG, "onFailure: ", e);
                        displaySnackbarStr(binding.getRoot(), e.getMessage());
                        // Call the displayFeedback method.
                        displayFeedback();
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

    private DocumentReference getRegistrationDocRef() {
        return db.collection(LEVELS_REF)
                .document(mClass.getLevel().getDocId()).collection(CLASSES_COL)
                .document(mClass.getDocId()).collection(REGISTRATIONS_COL)
                .document(registration.getDocId());
    }

    private String getGroup() {
        String group;
        if (group1) {
            group = getString(R.string.group_1);
        } else {
            group = getString(R.string.group_2);
        }
        return group;
    }

    private void displayFeedback() {
        if (boolean1 && boolean2 && boolean3 && boolean4) {
            // If all operations succeeds, inform the user and exit the current Activity.
            Toast.makeText(RecentAbsenceActivity.this,
                    R.string.saved, Toast.LENGTH_SHORT).show();
            RecentAbsenceActivity.this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recent_absence, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.men_delete) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_q)
                    .setMessage(R.string.sure_to_delete)
                    .setNegativeButton(R.string.no, null)
                    .setPositiveButton(R.string.yes, (arg0, arg1) ->
                            getRegistrationDocRef().delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // If the task was successful, inform the user.
                                    Toast.makeText(RecentAbsenceActivity.this,
                                            R.string.deleted, Toast.LENGTH_LONG).show();
                                    RecentAbsenceActivity.this.finish();
                                } else {
                                    // If the task failed, log the exception.
                                    Log.e(TAG, "onComplete: ", task.getException());
                                    // Display the exception's message.
                                    displaySnackbarStr(binding.getRoot(),
                                            Objects.requireNonNull(task.getException()).getMessage());
                                }
                            })
                    ).create().show();
        } else if (item.getItemId() == android.R.id.home) {
            // If the item ID is android's back button, finish the current Activity.
            RecentAbsenceActivity.this.finish();
        }
        return true;
    }
}