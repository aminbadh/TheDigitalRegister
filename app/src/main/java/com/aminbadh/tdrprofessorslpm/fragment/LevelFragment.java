package com.aminbadh.tdrprofessorslpm.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aminbadh.tdrprofessorslpm.activity.ClassActivity;
import com.aminbadh.tdrprofessorslpm.adapter.LevelRecyclerAdapter;
import com.aminbadh.tdrprofessorslpm.custom.Level;
import com.aminbadh.tdrprofessorslpm.custom.LevelMain;
import com.aminbadh.tdrprofessorslpm.custom.Professor;
import com.aminbadh.tdrprofessorslpm.databinding.ListMainBinding;
import com.aminbadh.tdrprofessorslpm.interfaces.OnMainListener;

import java.util.ArrayList;
import java.util.Objects;

import static com.aminbadh.tdrprofessorslpm.custom.Constants.INTENT_FROM;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.LEVEL_MAIN_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.LEVEL_OBJECT;
import static com.aminbadh.tdrprofessorslpm.custom.Constants.PROF_OBJECT;

public class LevelFragment extends Fragment implements OnMainListener {

    private String resource;
    private ArrayList<Level> levels;
    private ListMainBinding binding;
    private Professor professor;

    public LevelFragment() {
        // This empty constructor MUST be in any Fragment.
    }

    public static LevelFragment newInstance(LevelMain levelMain) {
        Bundle args = new Bundle();
        args.putSerializable(LEVEL_MAIN_OBJECT, levelMain);
        LevelFragment levelFragment = new LevelFragment();
        levelFragment.setArguments(args);
        return levelFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Initialise the binding object.
        binding = ListMainBinding.inflate(inflater, container, false);
        // Get the saved argument and initialise the global objects.
        LevelMain levelMain = (LevelMain)
                Objects.requireNonNull(getArguments()).getSerializable(LEVEL_MAIN_OBJECT);
        resource = levelMain.getResource();
        levels = levelMain.getLevels();
        professor = levelMain.getProfessor();
        // Load the UI.
        loadUI();
        // Return the root View.
        return binding.getRoot();
    }

    private void loadUI() {
        if (levels.isEmpty()) {
            // If the levels ArrayList is empty, inform the user.
            binding.constraintLayoutNoDataMain.setVisibility(View.VISIBLE);
            binding.recyclerViewMain.setVisibility(View.GONE);
        } else {
            // If the levels ArrayList has data, setup the RecyclerView to display the levels.
            // First, create and initialise a LevelRecyclerAdapter.
            LevelRecyclerAdapter adapter = new LevelRecyclerAdapter(levels, this);
            // Setup the RecyclerView.
            binding.recyclerViewMain.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerViewMain.setHasFixedSize(true);
            binding.recyclerViewMain.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Assign the binding variable to null when the Fragment is destroyed.
        binding = null;
    }

    @Override
    public void onClickListener(int position) {
        // Create an Intent going to the ClassActivity.
        Intent intent = new Intent(getActivity(), ClassActivity.class);
        // Add the Professor object as an extra.
        intent.putExtra(PROF_OBJECT, professor);
        // Add the Level object as an extra.
        intent.putExtra(LEVEL_OBJECT, levels.get(position));
        // Add the resource String as an extra.
        intent.putExtra(INTENT_FROM, resource);
        // Start the operation.
        startActivity(intent);
    }
}
