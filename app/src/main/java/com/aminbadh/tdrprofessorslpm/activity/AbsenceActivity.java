package com.aminbadh.tdrprofessorslpm.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
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
import com.aminbadh.tdrprofessorslpm.adapter.ClassRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.adapter.LevelRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.adapter.StudentRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.custom.Absence;
import com.aminbadh.tdrprofessorslpm.custom.Class;
import com.aminbadh.tdrprofessorslpm.custom.Level;
import com.aminbadh.tdrprofessorslpm.custom.Professor;
import com.aminbadh.tdrprofessorslpm.custom.Registration;
import com.aminbadh.tdrprofessorslpm.custom.Student;
import com.aminbadh.tdrprofessorslpm.databinding.ActivityAbsenceBinding;
import com.aminbadh.tdrprofessorslpm.dialog.WaitDialog;
import com.aminbadh.tdrprofessorslpm.fragment.TimePickerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Objects;

import static com.aminbadh.tdrprofessorslpm.custom.Constants.CLASSES_COL;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.CLASS_NUMBER_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.FIELD_ABSENCES;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.LEVELS_COL;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.LEVEL_FIELD;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.MAIN_PREFS;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.REGISTRATIONS_COL;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.STUDENT_STATE_CHANGED;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.USERS_COLLECTION;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarRef;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.displaySnackbarStr;
import static com.aminbadh.tdrprofessorslpm.custom.Functions.getInternetConnectionStatus;

public class AbsenceActivity extends AppCompatActivity {

    private static final String TAG = AbsenceActivity.class.getSimpleName();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<Level> levels = new ArrayList<>();
    private final ArrayList<Class> classes = new ArrayList<>();
    private final ArrayList<String> absentStudents = new ArrayList<>();
    private ActivityAbsenceBinding binding;
    private FirebaseAuth auth;
    private Professor professor;
    private boolean group1, isOnline;
    private String fromTime, toTime;
    private StudentRecyclerAdapter studentsAdapterGr1, studentsAdapterGr2;
    private AbsenceRecyclerAdapter absencesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise the binding object.
        binding = ActivityAbsenceBinding.inflate(getLayoutInflater());
        // Lock the Activity's orientation.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        // Set the Activity's content view.
        setContentView(binding.getRoot());
        // Set Activity's title.
        setTitle(R.string.new_registration);
        // Set the Activity's toolbar.
        setSupportActionBar(binding.toolbar);
        // Change the navigation icon.
        binding.toolbar.setNavigationIcon(R.drawable.ic_exit);
        // Initialise the FirebaseAuth object.
        auth = FirebaseAuth.getInstance();
        // Initialise the isOnline variable.
        isOnline = getInternetConnectionStatus(this);
        // Check if the user is online.
        if (isOnline) {
            // Review data.
            reviewData();
        } else {
            // Show the offline feedback.
            binding.textViewOffline.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getSharedPreferences(MAIN_PREFS, MODE_PRIVATE)
                .getBoolean(STUDENT_STATE_CHANGED, false)) {
            getSharedPreferences(MAIN_PREFS, MODE_PRIVATE).edit()
                    .putBoolean(STUDENT_STATE_CHANGED, false).apply();
            finish();
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
                    // Initialise the professor object.
                    professor = task.getResult().toObject(Professor.class);
                    // Get the levels.
                    getLevelsData();
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

    private void getLevelsData() {
        // Get the levels whereIn the levels that the professor teach.
        db.collection(LEVELS_COL).whereIn(LEVEL_FIELD, professor.getLevels())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Loop in each documentSnapshot and do the following.
                for (QueryDocumentSnapshot documentSnapshot : Objects
                        .requireNonNull(task.getResult())) {
                    // Convert the documentSnapshot to a Level object.
                    Level level = documentSnapshot.toObject(Level.class);
                    // Set the Level object Id.
                    level.setDocId(documentSnapshot.getId());
                    // Add the Level object.
                    levels.add(level);
                }
                // Sort the levels ArrayList.
                Collections.sort(levels, (level, t1) -> level.getLevel().compareTo(t1.getLevel()));
                // Setup the RecyclerView.
                LevelRecyclerAdapter adapter = new LevelRecyclerAdapter(levels, position -> {
                    // Log a message.
                    Log.i(TAG, "onClickListener: Level selected: " +
                            levels.get(position).getLevel());
                    // Get the classes data.
                    getClassesData(levels.get(position));
                    // Hide the levels ui.
                    binding.CLLevels.animate().alpha(0f)
                            .setDuration(getResources().getInteger
                                    (android.R.integer.config_shortAnimTime))
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    binding.CLLevels.setVisibility(View.GONE);
                                }
                            });
                });
                binding.RVLevels.setHasFixedSize(true);
                binding.RVLevels.setLayoutManager(new LinearLayoutManager(this));
                binding.RVLevels.setAdapter(adapter);
                // Hide the setting up feedback.
                binding.CLSettingUp.setVisibility(View.GONE);
                // Show the levels ui.
                binding.CLLevels.setAlpha(0f);
                binding.CLLevels.setVisibility(View.VISIBLE);
                binding.CLLevels.animate().alpha(1f)
                        .setDuration(getResources().getInteger
                                (android.R.integer.config_shortAnimTime))
                        .setListener(null);
            } else {
                // Hide the setting up feedback.
                binding.CLSettingUp.setVisibility(View.GONE);
                // If the task failed, log the exception.
                Log.e(TAG, "onComplete: Task Failed", task.getException());
                // Display the exception's message.
                displaySnackbarStr(binding.getRoot(), Objects.requireNonNull(task
                        .getException()).getMessage());
            }
        });
    }

    private void getClassesData(Level level) {
        // Show the setting up feedback.
        binding.CLSettingUp.setVisibility(View.VISIBLE);
        // Get the classes.
        getQuery(level).get().addOnCompleteListener(task -> {
            // Set the setting up visibility to Gone.
            binding.CLSettingUp.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                // If the task was successful, loop in the results.
                for (QueryDocumentSnapshot documentSnapshot : Objects
                        .requireNonNull(task.getResult())) {
                    // Convert the QueryDocumentSnapshot object to a Class object.
                    Class mClass = documentSnapshot.toObject(Class.class);
                    // Add the document ID to the Class object.
                    mClass.setDocRef(documentSnapshot.getReference().getPath());
                    // Add the Class object to the classes ArrayList.
                    classes.add(mClass);
                }
                // Sort the classes ArrayList.
                Collections.sort(classes, (aClass, t1) -> aClass.getData().get("number")
                        .compareTo(t1.getData().get("number")));
                // Setup the RecyclerView.
                ClassRecyclerAdapter adapter = new ClassRecyclerAdapter(classes, position -> {
                    // Log a message.
                    Log.i(TAG, "onClickListener: Class selected: " +
                            classes.get(position).getClassName());
                    // Setup the absences UI.
                    SetupAbsencesUI(classes.get(position));
                    // Hide the classes UI.
                    binding.CLClasses.animate().alpha(0f)
                            .setDuration(getResources().getInteger
                                    (android.R.integer.config_shortAnimTime))
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    binding.CLLevels.setVisibility(View.GONE);
                                }
                            });
                });
                binding.RVClasses.setHasFixedSize(true);
                binding.RVClasses.setLayoutManager(new LinearLayoutManager(this));
                binding.RVClasses.setAdapter(adapter);
                // Hide the setting up feedback.
                binding.CLSettingUp.setVisibility(View.GONE);
                // Show the classes ui.
                binding.CLClasses.setAlpha(0f);
                binding.CLClasses.setVisibility(View.VISIBLE);
                binding.CLClasses.animate().alpha(1f)
                        .setDuration(getResources().getInteger
                                (android.R.integer.config_shortAnimTime))
                        .setListener(null);
            } else {
                // Hide the setting up feedback.
                binding.CLSettingUp.setVisibility(View.GONE);
                // If the task failed, log the exception.
                Log.e(TAG, "onComplete: Task Failed", task.getException());
                // Display the exception's error.
                displaySnackbarStr(binding.getRoot(), Objects.requireNonNull(task
                        .getException()).getMessage());
            }

        });
    }

    private Query getQuery(Level level) {
        // Create a new Query object.
        Query query;
        // Create a CollectionReference for the classes.
        CollectionReference classes = db.collection(LEVELS_COL)
                .document(level.getDocId()).collection(CLASSES_COL);
        // Create a String variables with current level in it and log it.
        String currentLevel = level.getLevel();
        Log.i(TAG, "getQuery: Current Level = " + currentLevel);
        // Create a switch statement.
        switch (currentLevel) {
            case "1S":
                // If the current level is "1S", search for Documents in that Collection.
                query = classes.whereIn(CLASS_NUMBER_FIELD, professor.getLevel1S());
                break;
            case "2Sc":
                // If the current level is "2Sc", search for Documents in that Collection.
                query = classes.whereIn(CLASS_NUMBER_FIELD, professor.getLevel2Sc());
                break;
            case "3M":
                // If the current level is "3M", search for Documents in that Collection.
                query = classes.whereIn(CLASS_NUMBER_FIELD, professor.getLevel3M());
                break;
            case "3Sc":
                // If the current level is "3Sc", search for Documents in that Collection.
                query = classes.whereIn(CLASS_NUMBER_FIELD, professor.getLevel3Sc());
                break;
            case "4L":
                // If the current level is "4L", search for Documents in that Collection.
                query = classes.whereIn(CLASS_NUMBER_FIELD, professor.getLevel4L());
                break;
            case "4M":
                // If the current level is "4M", search for Documents in that Collection.
                query = classes.whereIn(CLASS_NUMBER_FIELD, professor.getLevel4M());
                break;
            case "4Sc":
                // If the current level is "4Sc", search for Documents in that Collection.
                query = classes.whereIn(CLASS_NUMBER_FIELD, professor.getLevel4Sc());
                break;
            case "4T":
                // If the current level is "4T", search for Documents in that Collection.
                query = classes.whereIn(CLASS_NUMBER_FIELD, professor.getLevel4T());
                break;
            default:
                // If none of the cases are true, initialise the Query object to null.
                query = null;
                break;
        }
        // Return the Query object.
        return query;
    }

    private void SetupAbsencesUI(Class mClass) {
        // Setup the UI.
        binding.textViewClassName.setText(mClass.getClassName());
        setupGroupChangeBehaviour();
        setupTimePicker();
        setupRecyclerViewStudentsGr1(mClass);
        setupRecyclerViewStudentsGr2(mClass);
        setupRecyclerViewAbsences();
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
                            // Create a new String object and initialise it using the getGroup method.
                            String group = getGroup();
                            // Create a Registration object using the passed data.
                            Registration registration = new Registration(professor.getDisplayName(),
                                    Objects.requireNonNull(auth.getCurrentUser()).getUid(),
                                    professor.getSubject(), fromTime, toTime, group, mClass.getClassName(),
                                    absentStudents, System.currentTimeMillis());
                            // Save the Registration object to the database.
                            getRegCol(mClass).add(registration).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Update the absences list in the class doc.
                                    updateAbsencesList(mClass, registration, dialog);
                                } else {
                                    // If the task failed, log the exception.
                                    Log.e(TAG, "onComplete: Task failed", task.getException());
                                    // Display the exception's message.
                                    displaySnackbarStr(binding.getRoot(), Objects.requireNonNull(task
                                            .getException()).getMessage());
                                }
                            });
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
    }

    private String getGroup() {
        // Create a String object.
        String group;
        if (group1) {
            // If the current selected group is group 1, assign the String object to "Group 1".
            group = getString(R.string.group_1);
        } else {
            // If not, assign the String object to "Group 2".
            group = getString(R.string.group_2);
        }
        // Return the String object.
        return group;
    }

    private CollectionReference getRegCol(Class mClass) {
        // Return the Registrations collection reference.
        return db.document(mClass.getDocRef()).collection(REGISTRATIONS_COL);
    }

    private void updateAbsencesList(Class mClass, Registration registration,
                                    ProgressDialog dialog) {
        // Create a new ArrayList of strings that holds the absences.
        ArrayList<String> absentStudents;
        // Check if the class's absences list is empty or null.
        if (mClass.getAbsences() == null || mClass.getAbsences().isEmpty()) {
            // Make sure that the registration's absences list isn't null.
            assert registration.getAbsences() != null;
            // Update the absent students ArrayList to the registration's absences list.
            absentStudents = registration.getAbsences();
        } else {
            // Update the absent students ArrayList to the class's absences list.
            absentStudents = mClass.getAbsences();
            // Loop in the registration's absences list.
            for (String name : registration.getAbsences()) {
                // Check if the absent students ArrayList contains the student name.
                if (!absentStudents.contains(name)) {
                    // Add the student to the absent students list.
                    absentStudents.add(name);
                }
            }
        }
        // Update the class's absences list.
        db.document(mClass.getDocRef()).update(FIELD_ABSENCES, absentStudents)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Create the absence document.
                        createAbsence(registration, dialog, mClass);
                    } else {
                        // Dismiss the dialog.
                        dialog.dismiss();
                        // If the task failed, log the exception.
                        Log.e(TAG, "onComplete: Task failed", task.getException());
                        // Display the exception's message.
                        displaySnackbarStr(binding.getRoot(), Objects.requireNonNull(task
                                .getException()).getMessage());
                    }
                });
    }

    private void createAbsence(Registration registration, ProgressDialog dialog, Class mClass) {
        // Create a Calendar that holds the current time.
        Calendar current = Calendar.getInstance();
        // Create a Calendar that holds the current date.
        Calendar date = new GregorianCalendar(current.get(Calendar.YEAR),
                current.get(Calendar.MONTH), current.get(Calendar.DATE),
                0, 0, 0);
        // Get the absence document.
        db.collection("absences").whereEqualTo("date", date.getTimeInMillis())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Create an array that holds the fields.
                String[] fields = {"f8t9", "f9t10", "f10t11", "f11t12", "f12t13",
                        "f13t14", "f14t15", "f15t16", "f16t17", "f17t18"};
                // Check if the result is empty.
                if (task.getResult().getDocuments().isEmpty()) {
                    // Create an absence object.
                    Absence absence = new Absence();
                    // Set the date.
                    absence.setDate(date.getTimeInMillis());
                    // Create an arrayList of students.
                    ArrayList<Student> students = new ArrayList<>();
                    // Loop in the absences length.
                    for (int i = 0; i < registration.getAbsences().size(); i++) {
                        // Add the student.
                        students.add(new Student(registration.getAbsences().get(i), studentIndex(mClass,
                                registration.getGroup(), registration.getAbsences().get(i)), registration.getClassName()));
                        // Get the from time.
                        int from = getTime(registration.getFromTime());
                        // Get the to time.
                        int to = getTime(registration.getToTime());
                        // Get the range.
                        int range = to - from;
                        // Loop in the range.
                        for (int j = 0; j < range; j++) {
                            // Get the arrayList of the given time.
                            ArrayList<String> timeArray = getTimeArray(absence, fields[j + from - 8]);
                            // Add the student name.
                            timeArray.add(registration.getAbsences().get(i));
                            // Set the array.
                            absence = setTimeArray(absence, timeArray, fields[j + from - 8]);
                        }
                    }
                    // Set the absences.
                    absence.setAbsences(students);
                    // Add the absence object to the database.
                    db.collection("absences").add(absence)
                            .addOnCompleteListener(task1 -> {
                                // Dismiss the dialog.
                                dialog.dismiss();
                                if (task1.isSuccessful()) {
                                    // Finish the Activity.
                                    AbsenceActivity.this.finish();
                                } else {
                                    // If the task failed, log the exception.
                                    Log.e(TAG, "onComplete: Task failed", task.getException());
                                    // Display the exception's message.
                                    displaySnackbarStr(binding.getRoot(), Objects.requireNonNull(task
                                            .getException()).getMessage());
                                }
                            });
                } else {
                    // Create an absence object that holds the absence document.
                    DocumentSnapshot ds = task.getResult().getDocuments().get(0);
                    // Create an absence object that holds the document.
                    Absence absence = ds.toObject(Absence.class);
                    // Get the absences list.
                    ArrayList<Student> students = absence.getAbsences();
                    // Loop in the absences length.
                    for (int i = 0; i < registration.getAbsences().size(); i++) {
                        // Check if the user is already there.
                        if (containsStudents(students, registration.getAbsences().get(i))) {
                            // Get the from time.
                            int from = getTime(registration.getFromTime());
                            // Get the to time.
                            int to = getTime(registration.getToTime());
                            // Get the range.
                            int range = to - from;
                            // Loop in the range.
                            for (int j = 0; j < range; j++) {
                                // Get the arrayList of the given time.
                                ArrayList<String> timeArray = getTimeArray(absence,
                                        fields[j + from - 8]);
                                // Check if the timeArray doesn't contain the student.
                                if (!timeArray.contains(registration.getAbsences().get(i))) {
                                    // Add the student to the timeArray.
                                    timeArray.add(registration.getAbsences().get(i));
                                    // Set the array.
                                    absence = setTimeArray(absence, timeArray, fields[j + from - 8]);
                                }
                            }
                        } else {
                            // Add the student.
                            students.add(new Student(registration.getAbsences().get(i), studentIndex(mClass,
                                    registration.getGroup(), registration.getAbsences().get(i)), registration.getClassName()));
                            // Get the from time.
                            int from = getTime(registration.getFromTime());
                            // Get the to time.
                            int to = getTime(registration.getToTime());
                            // Get the range.
                            int range = to - from;
                            // Loop in the range.
                            for (int j = 0; j < range; j++) {
                                // Get the arrayList of the given time.
                                ArrayList<String> timeArray = getTimeArray(absence,
                                        fields[j + from - 8]);
                                // Check if the timeArray doesn't contain the student.
                                if (!timeArray.contains(registration.getAbsences().get(i))) {
                                    // Add the student to the timeArray.
                                    timeArray.add(registration.getAbsences().get(i));
                                    // Set the array.
                                    absence = setTimeArray(absence, timeArray, fields[j + from - 8]);
                                }
                            }
                        }
                    }
                    // Set the absences.
                    absence.setAbsences(students);
                    // Upload the absence object.
                    db.collection("absences").document(ds.getId()).set(absence)
                            .addOnCompleteListener(task1 -> {
                                // Dismiss the dialog.
                                dialog.dismiss();
                                if (task1.isSuccessful()) {
                                    // Finish the Activity.
                                    AbsenceActivity.this.finish();
                                } else {
                                    // If the task failed, log the exception.
                                    Log.e(TAG, "onComplete: Task failed", task.getException());
                                    // Display the exception's message.
                                    displaySnackbarStr(binding.getRoot(), Objects.requireNonNull(task
                                            .getException()).getMessage());
                                }
                            });
                }
            } else {
                // Dismiss the dialog.
                dialog.dismiss();
                // If the task failed, log the exception.
                Log.e(TAG, "onComplete: Task failed", task.getException());
                // Display the exception's message.
                displaySnackbarStr(binding.getRoot(), Objects.requireNonNull(task
                        .getException()).getMessage());
            }
        });
    }

    private boolean containsStudents(ArrayList<Student> students, String name) {
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            if (student.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private int getTime(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]);
    }

    private ArrayList<String> getTimeArray(Absence absence, String field) {
        switch (field) {
            case "f8t9":
                return absence.getF8t9();
            case "f9t10":
                return absence.getF9t10();
            case "f10t11":
                return absence.getF10t11();
            case "f11t12":
                return absence.getF11t12();
            case "f12t13":
                return absence.getF12t13();
            case "f13t14":
                return absence.getF13t14();
            case "f14t15":
                return absence.getF14t15();
            case "f15t16":
                return absence.getF15t16();
            case "f16t17":
                return absence.getF16t17();
            case "f17t18":
                return absence.getF17t18();
        }
        return null;
    }

    Absence setTimeArray(Absence absence, ArrayList<String> timeArray, String field) {
        switch (field) {
            case "f8t9":
                absence.setF8t9(timeArray);
                break;
            case "f9t10":
                absence.setF9t10(timeArray);
                break;
            case "f10t11":
                absence.setF10t11(timeArray);
                break;
            case "f11t12":
                absence.setF11t12(timeArray);
                break;
            case "f12t13":
                absence.setF12t13(timeArray);
                break;
            case "f13t14":
                absence.setF13t14(timeArray);
                break;
            case "f14t15":
                absence.setF14t15(timeArray);
                break;
            case "f15t16":
                absence.setF15t16(timeArray);
                break;
            case "f16t17":
                absence.setF16t17(timeArray);
                break;
            case "f17t18":
                absence.setF17t18(timeArray);
                break;
        }
        return absence;
    }

    private String studentIndex(Class mClass, String group, String name) {
        if (group.equals("Group 1")) {
            return String.valueOf(mClass.getStudents1().indexOf(name) + 1);
        } else {
            return String.valueOf(mClass.getStudents2().indexOf(name) + mClass.getStudents1().size() + 1);
        }
    }

    private void setupGroupChangeBehaviour() {
        // Set the radioButtonGr1 as checked.
        binding.radioButtonGr1.setChecked(true);
        // Set the group1 variable to true.
        group1 = true;
        // Show the recyclerViewStudentsGr1.
        showRVStudentsGr1(true);
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
            // Show the recyclerViewStudentsGr1.
            binding.recyclerViewStudentsGr2.setVisibility(View.GONE);
            binding.recyclerViewStudentsGr1.setAlpha(0f);
            binding.recyclerViewStudentsGr1.setVisibility(View.VISIBLE);
            binding.recyclerViewStudentsGr1.animate().alpha(1f)
                    .setDuration(getResources().getInteger
                            (android.R.integer.config_shortAnimTime))
                    .setListener(null);
        } else {
            // Show the recyclerViewStudentsGr2.
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
        // Create hours variables.
        final int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        final int nextHour = currentHour + 1;
        // Set the from and to time values.
        fromTime = currentHour + ":" + "0";
        toTime = nextHour + ":" + "0";
        // Update the text data.
        binding.textViewFrom.setText(fromTime);
        binding.textViewTo.setText(toTime);
        // Set the textViewFrom's onClickListener.
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
        // Set the textViewTo's onClickListener.
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

    private void setupRecyclerViewStudentsGr1(Class mClass) {
        // Create a new ArrayList to save students' names.
        ArrayList<String> students = new ArrayList<>();
        if (mClass.getStudents1() != null) {
            // Update the students ArrayList to the class's students list.
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

    private void setupRecyclerViewStudentsGr2(Class mClass) {
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
        // Initialise the AbsenceRecyclerAdapter object.
        absencesAdapter = new AbsenceRecyclerAdapter(absentStudents, this::removeAbsentStudent);
        // Setup the RecyclerView.
        binding.recyclerViewAbsences.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewAbsences.setHasFixedSize(true);
        binding.recyclerViewAbsences.setAdapter(absencesAdapter);
    }

    private void addAbsentStudent(String name) {
        if (absentStudents.contains(name)) {
            // Inform the user that this student is already marked as absent.
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

    @Override
    public void onBackPressed() {
        // Go back.
        back();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Go back.
        back();
        return true;
    }

    private void back() {
        if (isOnline) {
            // Inform the user that the data is not going to be saved.
            new AlertDialog.Builder(this)
                    .setTitle(R.string.exit_q)
                    .setMessage(R.string.changes_not_save)
                    .setNegativeButton(R.string.no, null)
                    .setPositiveButton(R.string.yes, (arg0, arg1) -> finish())
                    .create().show();
        } else {
            // Finish the Activity.
            finish();
        }
    }
}
