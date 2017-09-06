package org.secuso.privacyfriendlybreakreminder.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.secuso.privacyfriendlybreakreminder.ExerciseLocale;
import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.adapter.ExerciseSetAdapter;
import org.secuso.privacyfriendlybreakreminder.database.SQLiteHelper;
import org.secuso.privacyfriendlybreakreminder.database.data.ExerciseSet;

public class EditExerciseSetActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<ExerciseSet> {

    // extras
    public static final String EXTRA_EXERCISE_SET_ID = "EXTRA_EXERCISE_SET_ID";
    public static final String EXTRA_EXERCISE_SET_NAME = "EXTRA_EXERCISE_SET_NAME";

    // UI
    private TextView exerciseSetNameText;
    private RecyclerView exerciseList;
    private ProgressBar loadingSpinner;

    private ExerciseSetAdapter exerciseSetAdapter;
    private ActionBar actionBar;
    private Toolbar toolbar;

    // exercise set information
    private long exerciseSetId = -1L;
    private String exerciseSetName = "";
    private boolean modificationsDone = false;

    //methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_exercise_set);

        Intent i = getIntent();
        exerciseSetId = i.getLongExtra(EXTRA_EXERCISE_SET_ID, -1L);
        exerciseSetName = i.getStringExtra(EXTRA_EXERCISE_SET_NAME);

        if(exerciseSetId < 0L || TextUtils.isEmpty(exerciseSetName)) {
            // no valid exercise
            super.onBackPressed();
        }

        initResources();

        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void initResources() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        exerciseSetNameText = (TextView) findViewById(R.id.exercise_set_name);
        exerciseList = (RecyclerView) findViewById(R.id.exercise_list);
        exerciseSetAdapter = new ExerciseSetAdapter(this, null);
        exerciseList.setAdapter(exerciseSetAdapter);
        //exerciseList.setLayoutManager(new GridLayoutManager(this, 2));
        exerciseList.setLayoutManager(new LinearLayoutManager(this));
        loadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);

        exerciseSetNameText.setText(exerciseSetName);
        exerciseSetNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                modificationsDone = true;
            }
        });

        setupActionBar();
    }

    private void setupActionBar() {
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
        }

        actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setTitle(R.string.activity_title_edit_exercise_set);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white);
        }
    }

    @Override
    public Loader<ExerciseSet> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ExerciseSet>(this) {
            @Override
            public ExerciseSet loadInBackground() {
                SQLiteHelper helper = new SQLiteHelper(getContext());

                return helper.getExerciseListForSet((int)exerciseSetId, ExerciseLocale.getLocale());
            }

            @Override
            protected void onStartLoading() {
                loadingSpinner.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            protected void onReset() {}
        };
    }

    @Override
    public void onLoadFinished(Loader<ExerciseSet> loader, ExerciseSet set) {
        loadingSpinner.animate().alpha(0.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loadingSpinner.setVisibility(View.GONE);
            }
        });

        exerciseSetAdapter.updateData(set);
    }

    @Override
    public void onLoaderReset(Loader<ExerciseSet> loader) {}

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        getSupportLoaderManager().restartLoader(0, null, this);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(modificationsDone) {
                    showDiscardDialog();
                } else {
                    super.onBackPressed();
                }
                return true;
            case R.id.save:
                saveChanges();
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChanges() {
        ExerciseSet set = exerciseSetAdapter.getExerciseSet();

        // TODO: save changes to database
        // man könnte den unterschied, der gespeichert werden muss rausfinden, indem man nur die änderungen speichert..
        // man könnte auch einfach alle dateneinträge zu dem set löschen und neu eintragen
        // man könnte das exerciseSet clonable machen und eine original kopie abspeichern und dann mit dem aus dem adapter vergleichen
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_exercise_sets, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(modificationsDone) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showDiscardDialog() {
        new AlertDialog.Builder(this)
            .setPositiveButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            })
            .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EditExerciseSetActivity.super.onBackPressed();
                }
            })
            .setMessage(R.string.dialog_discard_confirmation)
            .create().show();
    }
}