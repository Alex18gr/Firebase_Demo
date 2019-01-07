package com.example.alexc.firebase_demo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexc.firebase_demo.models.Note;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Created by User on 5/14/2018.
 */

public class ViewNoteDialog extends DialogFragment implements View.OnClickListener{

    private static final String TAG = "ViewNoteDialog";

    //widgets
    private EditText mTitle, mContent;
    private TextView mSave, mDelete;

    //vars
    private MainActivity mIMainActivity;
    private Note mNote;

    public static ViewNoteDialog newInstance(Note note) {
        ViewNoteDialog dialog = new ViewNoteDialog();

        Bundle args = new Bundle();
        args.putParcelable("note", note);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int style = DialogFragment.STYLE_NORMAL;
        int theme = android.R.style.Theme_Holo_Light_Dialog;
        setStyle(style, theme);

        mNote = getArguments().getParcelable("note");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_view_note, container, false);
        mTitle = view.findViewById(R.id.note_title);
        mContent = view.findViewById(R.id.note_content);
        mSave = view.findViewById(R.id.save);
        mDelete = view.findViewById(R.id.delete);

        mSave.setOnClickListener(this);
        mDelete.setOnClickListener(this);

        getDialog().setTitle("Edit Note");

        setInitialProperties();

        return view;
    }

    private void setInitialProperties(){
        mTitle.setText(mNote.getTitle());
        mContent.setText(mNote.getContent());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.save:{

                String title = mTitle.getText().toString();
                String content = mContent.getText().toString();

                if(!title.equals("")){

                    mNote.setTitle(title);
                    mNote.setContent(content);

                    mIMainActivity.updateNote(mNote);
                    getDialog().dismiss();
                }
                else{
                    Toast.makeText(getActivity(), "Enter a title", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.delete:{
                mIMainActivity.deleteNote(mNote);
                getDialog().dismiss();
                break;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIMainActivity = (MainActivity) getActivity();
    }
}





















