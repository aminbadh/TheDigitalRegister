package com.aminbadh.tdrprofessorslpm.activity;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aminbadh.tdrprofessorslpm.R;
import com.aminbadh.tdrprofessorslpm.adapter.AbsenceRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.adapter.StudentRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.custom.Class;
import com.aminbadh.tdrprofessorslpm.custom.Registration;
import com.aminbadh.tdrprofessorslpm.databinding.ActivityAbsenceBinding;
import com.aminbadh.tdrprofessorslpm.dialog.WaitDialog;
import com.aminbadh.tdrprofessorslpm.fragment.TimePickerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import static com.aminbadh.tdrprofessorslpm.custom.Constants.ABSENCES_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.FROM_TIME_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.GROUP_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.REGISTRATION_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.TO_TIME_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.USERS_COLLECTION;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarRef;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarStr;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.getInternetConnectionStatus;

public class RecentAbsenceActivity extends AppCompatActivity {

    private static final String TAG = RecentAbsenceActivity.class.getSimpleName();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityAbsenceBinding binding;
    private Registration registration;
    private FirebaseAuth auth;
    private String fromTime, toTime;
    private boolean group1, b1, b2, b3, b4, isOnline;
    private Class mClass;
    private StudentRecyclerAdapter studentsAdapterGr1, studentsAdapterGr2;
    private AbsenceRecyclerAdapter absencesAdapter;
    private ArrayList<String> absentStudents = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise the binding object.
        binding = ActivityAbsenceBinding.inflate(getLayoutInflater());
        // Lock the Activity's orientation.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        // Set the Activity's title.
        setTitle(R.string.review);
        // Hide the Toolbar.
        binding.toolbar.setVisibility(View.GONE);
        // Set the Activity's content view.
        setContentView(binding.getRoot());
        // Initialise the auth object.
        auth = FirebaseAuth.getInstance();
        // Initialise the registration object.
        registration = (Registration) getIntent().getSerializableExtra(REGISTRATION_OBJECT);
        // Initialise the online variable.
        isOnline = getInternetConnectionStatus(this);
        // Check if the user is online.
        if (isOnline) {
            // Review data.
            reviewData();
            // Disable the save button (temporary).
            // TODO: Allow the professors to apply changes.
            binding.buttonSave.setEnabled(false);
        } else {
            // Show the offline feedback.
            binding.textViewOffline.setVisibility(View.VISIBLE);
        }
    }

    private void reviewData() {
        // Show the setting up feedback.
        binding.CLSettingUp.setVisibility(View.VISIBLE);
        // Get the user's document.
        db.collection(USERS_COLLECTION).document(Objects.requireNonNull(auth
                .getCurrentUser()).getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // Get the class document.
                    getClassDoc();
                } else {
                    // Log a message.
                    Log.e(TAG, "onComplete: User doesn't exist!");
                    // Sign out the user.
                    auth.signOut();
                    // Finish the current Activity.
                    finish();
                }
            } else {
                // Hide the setting up feedback.
                binding.CLSettingUp.setVisibility(View.GONE);
                // Make sure there is an exception.
                assert task.getException() != null;
                // Log the exception.
                Log.e(TAG, "onFailure: ", task.getException());
                // Display the exception's message.
                displaySnackbarStr(binding.getRoot(), task.getException().getMessage());
            }
        });
    }

    private void getClassDoc() {
        // Get the class document.
        Objects.requireNonNull(db.document(registration.getDocRef()).getParent().getParent()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Initialise the mClass object.
                        mClass = task.getResult().toObject(Class.class);
                        // Load the UI.
                        loadUI();
                    } else {
                        // Hide the setting up feedback.
                        binding.CLSettingUp.setVisibility(View.GONE);
                        // Make sure there is an exception.
                        assert task.getException() != null;
                        // Log the exception.
                        Log.e(TAG, "onFailure: ", task.getException());
                        // Display the exception's message.
                        displaySnackbarStr(binding.getRoot(), task.getException().getMessage());
                    }
                });
    }

    private void loadUI() {
        // Set the textViewClassName's text to the class name.
        binding.textViewClassName.setText(registration.getClassName());
        // Setup group change behaviour.
        setupGroupChangeBehaviour();
        // Setup the time picker.
        setupTimePicker();
        // Setup RVs
        setupRecyclerViewStudentsGr1();
        setupRecyclerViewStudentsGr2();
        setupRecyclerViewAbsences();
        // Set the Button's onClickListener.
        binding.buttonSave.setOnClickListener(view -> {
            if (getInternetConnectionStatus(this)) {
                // Create and show an AlertDialog.
                new AlertDialog.Builder(this).setTitle(R.string.save_doc_q)
                        .setMessage(R.string.save_doc_info)
                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                            // Show a ProgressDialog.
                            ProgressDialog dialog = new WaitDialog(this);
                            dialog.show();
                            dialog.setContentView(R.layout.dialog_wait);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            // Create a DocumentReference from the registration.
                            DocumentReference registrationRef = db.document(registration.getDocRef());
                            // Create a new String object and initialise it using the getGroup method.
                            String group = getGroup();
                            // Update the changes.
                            if (!fromTime.equals(registration.getFromTime())) {
                                registrationRef.update(FROM_TIME_FIELD, fromTime)
                                        .addOnSuccessListener(aVoid -> b1 = true)
                                        .addOnFailureListener(e -> {
                                            b1 = false;
                                            Log.e(TAG, "onFailure: ", e);
                                            displaySnackbarStr(binding.getRoot(), e.getMessage());
                                        });
                            } else {
                                b1 = true;
                            }
                            if (!toTime.equals(registration.getToTime())) {
                                registrationRef.update(TO_TIME_FIELD, toTime)
                                        .addOnSuccessListener(aVoid -> b2 = true)
                                        .addOnFailureListener(e -> {
                                            b2 = false;
                                            Log.e(TAG, "onFailure: ", e);
                                            displaySnackbarStr(binding.getRoot(), e.getMessage());
                                        });
                            } else {
                                b2 = true;
                            }
                            if (!group.equals(registration.getGroup())) {
                                registrationRef.update(GROUP_FIELD, group)
                                        .addOnSuccessListener(aVoid -> b3 = true)
                                        .addOnFailureListener(e -> {
                                            b3 = false;
                                            Log.e(TAG, "onFailure: ", e);
                                            displaySnackbarStr(binding.getRoot(), e.getMessage());
                                        });
                            } else {
                                b3 = true;
                            }
                            registrationRef.update(ABSENCES_FIELD, absentStudents)
                                    .addOnSuccessListener(aVoid -> {
                                        b4 = true;
                                        endTask(dialog);
                                    })
                                    .addOnFailureListener(e -> {
                                        b4 = false;
                                        Log.e(TAG, "onFailure: ", e);
                                        displaySnackbarStr(binding.getRoot(), e.getMessage());
                                        endTask(dialog);
                                    });
                            Objects.requireNonNull(db.document(registration.getDocRef())
                                    .getParent().getParent()).update(ABSENCES_FIELD, getAbsences());
                        })
                        .setNegativeButton(R.string.no, null)
                        .create().show();
            } else {
                // If the user is offline, inform him that he need internet connection.
                displaySnackbarRef(binding.getRoot(), R.string.you_are_offline);
            }
        });
        // Show the ui.
        binding.nestedScrollView.setAlpha(0f);
        binding.nestedScrollView.setVisibility(View.VISIBLE);
        binding.nestedScrollView.animate().alpha(1f)
                .setDuration(getResources().getInteger
                        (android.R.integer.config_longAnimTime))
                .setListener(null);
        binding.CLSettingUp.setVisibility(View.GONE);
    }

    private ArrayList<String> getAbsences() {
        // Create a new ArrayList of strings that holds the absences.
        ArrayList<String> arrayList;
        // Check if the class's absences list is empty or null.
        if (mClass.getAbsences() == null || mClass.getAbsences().isEmpty()) {
            // Make sure that the registration's absences list isn't null.
            assert absentStudents != null;
            // Update the absent students ArrayList to the registration's absences list.
            arrayList = absentStudents;
        } else {
            // Update the absent students ArrayList to the class's absences list.
            arrayList = mClass.getAbsences();
            // Loop in the registration's absences list.
            for (String name : absentStudents) {
                // Check if the absent students ArrayList contains the student name.
                if (!arrayList.contains(name)) {
                    // Add the student to the absent students list.
                    arrayList.add(name);
                }
            }
        }
        // Return the ArrayList.
        return arrayList;
    }


    private void setupGroupChangeBehaviour() {
        if (registration.getGroup().equals(getString(R.string.group_1))) {
            // Check the first radioButton.
            binding.radioButtonGr1.setChecked(true);
            // Show the first RV.
            showRVStudentsGr1(true);
            // Set the group1's value to true.
            group1 = true;
        } else {
            // Check the second radioButton.
            binding.radioButtonGr2.setChecked(true);
            // Show the second RV.
            showRVStudentsGr1(false);
            // Set the group1's value to false.
            group1 = false;
        }
        // Set the radioGroupGr's onCheckedChangeListener.
        binding.radioGroupGr.setOnCheckedChangeListener(
                (radioGroup, i) -> {
                    if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonGr1) {
                        // Show the recyclerViewStudentsGr1.
                        showRVStudentsGr1(true);
                        // Set the group1 boolean variable to true.
                        group1 = true;
                        // Clear the absent students ArrayList.
                        absentStudents.clear();
                        // Notify the adapter.
                        absencesAdapter.notifyDataSetChanged();
                        // Show the no data feedback.
                        binding.recyclerViewAbsences.setVisibility(View.GONE);
                        binding.textViewNoAbsences.setVisibility(View.VISIBLE);
                    } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonGr2) {
                        // Show the recyclerViewStudentsGr2.
                        showRVStudentsGr1(false);
                        // Set the group1 boolean variable to false.
                        group1 = false;
                        // Clear the absent students ArrayList.
                        absentStudents.clear();
                        // Notify the adapter.
                        absencesAdapter.notifyDataSetChanged();
                        // Show the no data feedback.
                        binding.recyclerViewAbsences.setVisibility(View.GONE);
                        binding.textViewNoAbsences.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showRVStudentsGr1(boolean b) {
        if (b) {
            // Show the first group RV.
            binding.recyclerViewStudentsGr2.setVisibility(View.GONE);
            binding.recyclerViewStudentsGr1.setAlpha(0f);
            binding.recyclerViewStudentsGr1.setVisibility(View.VISIBLE);
            binding.recyclerViewStudentsGr1.animate().alpha(1f)
                    .setDuration(getResources().getInteger
                            (android.R.integer.config_shortAnimTime))
                    .setListener(null);
        } else {
            // Show the second group RV.
            binding.recyclerViewStudentsGr1.setVisibility(View.GONE);
            binding.recyclerViewStudentsGr2.setAlpha(0f);
            binding.recyclerViewStudentsGr2.setVisibility(View.VISIBLE);
            binding.recyclerViewStudentsGr2.animate().alpha(1f)
                    .setDuration(getResources().getInteger
                            (android.R.integer.config_shortAnimTime))
                    .setListener(null);
        }
    }

    private void setupTimePicker() {
        // Set the from and to time values.
        fromTime = registration.getFromTime();
        toTime = registration.getToTime();
        // Update the text data.
        binding.textViewFrom.setText(fromTime);
        binding.textViewTo.setText(toTime);
        // Set the CLFrom's onClickListener.
        binding.CLFrom.setOnClickListener(view -> {
            // Create a DialogFragment object and initialise it to a new TimePickerFragment.
            DialogFragment timePicker = new TimePickerFragment((timePicker1, i, i1) -> {
                // Get the time and assign it to a String object.
                String time = i + ":" + i1;
                // Set the textView's text
                binding.textViewFrom.setText(time);
                // Assign the time String object to the global fromTime String object.
                fromTime = time;
            });
            // Show the DialogFragment object.
            timePicker.show(getSupportFragmentManager(), TAG);
        });
        // Set the CLTo's onClickListener.
        binding.CLTo.setOnClickListener(view -> {
            // Create a DialogFragment object and initialise it to a new TimePickerFragment.
            DialogFragment timePicker = new TimePickerFragment((timePicker12, i, i1) -> {
                // Get the time and assign it to a String object.
                String time = i + ":" + i1;
                // Set the textView's text
                binding.textViewTo.setText(time);
                // Assign the time String object to the global fromTime String object.
                toTime = time;
            });
            // Show the DialogFragment object.
            timePicker.show(getSupportFragmentManager(), TAG);
        });
    }

    private void setupRecyclerViewStudentsGr1() {
        // Create a new ArrayList to save students' names.
        ArrayList<String> students = new ArrayList<>();
        if (mClass.getStudents1() != null) {
            students = mClass.getStudents1();
        }
        // Initialise the adapter.
        studentsAdapterGr1 = new StudentRecyclerAdapter(students, mClass.getAbsences(),
                position -> {
                    // Create a new String object and assign to it the student name.
                    String name = studentsAdapterGr1.getStudentName(position);
                    // Add the student to the absences list.
                    addAbsentStudent(name);
                });
        // Setup the RecyclerView.
        binding.recyclerViewStudentsGr1.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewStudentsGr1.setHasFixedSize(true);
        binding.recyclerViewStudentsGr1.setAdapter(studentsAdapterGr1);
    }

    private void setupRecyclerViewStudentsGr2() {
        // Create a new ArrayList to save students' names.
        ArrayList<String> students = new ArrayList<>();
        if (mClass.getStudents2() != null) {
            students = mClass.getStudents2();
        }
        // Initialise the adapter.
        studentsAdapterGr2 = new StudentRecyclerAdapter(students, mClass.getAbsences(),
                position -> {
                    // Create a new String object and assign to it the student name.
                    String name = studentsAdapterGr2.getStudentName(position);
                    // Add the student to the absences list.
                    addAbsentStudent(name);
                });
        // Setup the RecyclerView.
        binding.recyclerViewStudentsGr2.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewStudentsGr2.setHasFixedSize(true);
        binding.recyclerViewStudentsGr2.setAdapter(studentsAdapterGr2);
    }

    private void setupRecyclerViewAbsences() {
        // Initialise the absentStudents ArrayList.
        absentStudents = registration.getAbsences();
        // Initialise the AbsenceRecyclerAdapter object.
        absencesAdapter = new AbsenceRecyclerAdapter(absentStudents, this::removeAbsentStudent);
        // Setup the RecyclerView.
        binding.recyclerViewAbsences.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewAbsences.setHasFixedSize(true);
        binding.recyclerViewAbsences.setAdapter(absencesAdapter);
        // Show the recyclerView.
        if (!absentStudents.isEmpty()) {
            binding.recyclerViewAbsences.setVisibility(View.VISIBLE);
            binding.textViewNoAbsences.setVisibility(View.GONE);
        }
    }

    private void addAbsentStudent(String name) {
        if (absentStudents.contains(name)) {
            // Inform the user that the student is already marked as absent.
            displaySnackbarRef(binding.getRoot(), R.string.this_student_is_absent);
            return;
        }
        // Add the student name to the absent students ArrayList.
        absentStudents.add(name);
        // Notify the adapter.
        absencesAdapter.notifyDataSetChanged();
        // Hide the no data feedback.
        binding.textViewNoAbsences.setVisibility(View.GONE);
        binding.recyclerViewAbsences.setVisibility(View.VISIBLE);
    }

    private void removeAbsentStudent(int position) {
        // Remove the student name from the absent students ArrayList.
        absentStudents.remove(position);
        // Notify the adapter.
        absencesAdapter.notifyDataSetChanged();
        // Show the no data feedback if the list is empty.
        if (absentStudents.isEmpty()) {
            binding.textViewNoAbsences.setVisibility(View.VISIBLE);
            binding.recyclerViewAbsences.setVisibility(View.GONE);
        }
    }

    private String getGroup() {
        // Create a String object.
        String group;
        if (group1) {
            // Assign the String object to "Group 1".
            group = getString(R.string.group_1);
        } else {
            // Assign the String object to "Group 2".
            group = getString(R.string.group_2);
        }
        // Return the String object.
        return group;
    }

    private void endTask(ProgressDialog dialog) {
        // Dismiss the dialog.
        dialog.dismiss();
        if (b1 && b2 && b3 && b4) {
            // Finish the Activity.
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isOnline) {
            // Inflate the menu.
            getMenuInflater().inflate(R.menu.menu_recent, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Finish.
            finish();
        } else {
            // Delete the document.
            deleteDocument();
        }
        return true;
    }

    private void deleteDocument() {
        // Inform the user that the data will be lost.
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_doc)
                .setMessage(R.string.delete_info)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (arg0, arg1) -> {
                    // Inform the user that the deleting operation has started.
                    displaySnackbarRef(binding.getRoot(), R.string.deleting);
                    // Delete the registration document.
                    db.document(registration.getDocRef()).delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Finish the Activity.
                            finish();
                        } else {
                            // Make sure there is an exception.
                            assert task.getException() != null;
                            // Log the exception.
                            Log.e(TAG, "onFailure: ", task.getException());
                            // Display the exception's message.
                            displaySnackbarStr(binding.getRoot(), task.getException().getMessage());
                        }
                    });
                }).create().show();
    }
}
