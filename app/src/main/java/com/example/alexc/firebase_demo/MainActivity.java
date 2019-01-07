package com.example.alexc.firebase_demo;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.example.alexc.firebase_demo.models.Note;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "MainActivity";

    private final MainActivity mMainActivity = this;

    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;


    //widgets
    private FloatingActionButton mFab;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mNotesListView;

    //vars
    private View mParentLayout;
    private ArrayList<Note> mNotes = new ArrayList<>();
    private NoteViewAdapter mAdapter;
    private DocumentSnapshot mLastQueriedDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFab =  findViewById(R.id.fab);
        mParentLayout = findViewById(android.R.id.content);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        mFab.setOnClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        initListView();
        setupFirebaseAuth();
        getNotes();

    }

    private void initListView() {
        mNotesListView = findViewById(R.id.note_list_view);
        mAdapter = new NoteViewAdapter(this, R.layout.layout_note_list_item,
                this.mNotes);
        mNotesListView.setAdapter(mAdapter);
        mNotesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mAdapter.setmSelectedNoteIndex(i);
                mMainActivity.onNoteSelected(mNotes.get(i));
            }
        });
        getNotes();
    }

    private void onNoteSelected(Note note) {
        ViewNoteDialog dialog = ViewNoteDialog.newInstance(note);
        dialog.show(getSupportFragmentManager(), getString(R.string.dialog_view_note));
    }

    public void createNewNote(String title, String content) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference newNoteRef = db
                .collection("notes")
                .document();

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setNote_id(newNoteRef.getId());
        note.setUser_id(userId);

        newNoteRef.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(mMainActivity, "Created new note", Toast.LENGTH_SHORT).show();
                    getNotes();
                    mAdapter.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(mMainActivity, "Failed. Check log.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getNotes() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
//                .setTimestampsInSnapshotsEnabled(true)
//                .build();
//        db.setFirestoreSettings(settings);

        CollectionReference notesCollectionRef = db
                .collection("notes");

        Query notesQuery = null;
        if(mLastQueriedDocument != null){
            notesQuery = notesCollectionRef
                    .whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .startAfter(mLastQueriedDocument);
        }
        else{
            notesQuery = notesCollectionRef
                    .whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .orderBy("timestamp", Query.Direction.ASCENDING);
        }

        notesQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d(TAG, "onComplete: query task completed");
                if(task.isSuccessful()){

                    for(QueryDocumentSnapshot document: task.getResult()){
                        Note note = document.toObject(Note.class);
                        mNotes.add(note);
                        Log.d(TAG, "onComplete: got a new note. Position: " + (mNotes.size() - 1));
                        Log.d(TAG, "onComplete: note: " + note.toString());
                    }

                    if(task.getResult().size() != 0){
                        mLastQueriedDocument = task.getResult().getDocuments()
                                .get(task.getResult().size() -1);
                    }

                    mAdapter.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(mMainActivity, "Query Failed. Check Logs.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateNote(final Note mNote) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference noteRef = db
                .collection("notes")
                .document(mNote.getNote_id());

        noteRef.update(
                "title", mNote.getTitle(),
                "content", mNote.getContent()
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(mMainActivity, "Updated note", Toast.LENGTH_SHORT).show();
                    mAdapter.updateNote(mNote);
                }
                else{
                    Toast.makeText(mMainActivity, "Failed. Check log.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.optionSignOut:
                signOut();
                return true;
            case R.id.crash1:
                makeCrash(1);
                return true;
            case R.id.crash2:
                makeCrash(2);
                return true;
            case R.id.crash3:
                makeCrash(3);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void makeCrash(int crashNum) {
        Log.d(TAG, "makeCrash: making crash !!");
        Toast.makeText(mMainActivity, "making crash!!!", Toast.LENGTH_SHORT).show();
        switch (crashNum) {
            case 1:
                Crashlytics.getInstance().crash();
        }
    }

    public void deleteNote(final Note mNote) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference noteRef = db
                .collection("notes")
                .document(mNote.getNote_id());

        noteRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(mMainActivity, "Deleted note", Toast.LENGTH_SHORT).show();
                    mAdapter.removeNote(mNote);
                }
                else{
                    Toast.makeText(mMainActivity, "Failed. Check log.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.fab:{
                //create a new note
                NewNoteDialog dialog = new NewNoteDialog();
                dialog.show(getSupportFragmentManager(), getString(R.string.dialog_new_note));
                break;
            }
        }
    }

    @Override
    public void onRefresh() {
        getNotes();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void signOut(){
        Log.d(TAG, "signOut: signing out");
        FirebaseAuth.getInstance().signOut();
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }
}
