package com.example.alexc.firebase_demo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.alexc.firebase_demo.models.Note;

import java.text.SimpleDateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NoteViewAdapter extends ArrayAdapter<Note> {

    private static final String TAG = "NoteViewAdapter";

    private List<Note> dataset;
    private final LayoutInflater inflater;
    private final int layoutResource;
    private int mSelectedNoteIndex;


    public NoteViewAdapter(@NonNull Context context, int resource, @NonNull List<Note> objects) {
        super(context, resource, objects);
        dataset = objects;
        inflater = LayoutInflater.from(context);
        layoutResource = resource;
    }

    public int getmSelectedNoteIndex() {
        return mSelectedNoteIndex;
    }

    public void setmSelectedNoteIndex(int mSelectedNoteIndex) {
        this.mSelectedNoteIndex = mSelectedNoteIndex;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        NotesViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(layoutResource, parent, false);
            holder = new NotesViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (NotesViewHolder) convertView.getTag();
        }

        Note note = dataset.get(position);
        holder.titleTextView.setText(note.getTitle());
        SimpleDateFormat spf = new SimpleDateFormat("MMM dd, yyyy");
        String date = spf.format(note.getTimestamp());
        holder.timestampTextView.setText(date);

        return convertView;
    }

    public List<Note> getDataset() {
        return dataset;
    }

    public void setDataset(List<Note> dataset) {
        this.dataset = dataset;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dataset.size();
    }

    public void updateNote(Note mNote) {
        dataset.get(mSelectedNoteIndex).setTitle(mNote.getTitle());
        dataset.get(mSelectedNoteIndex).setContent(mNote.getContent());
        notifyDataSetChanged();
    }

    public void removeNote(Note mNote){
        dataset.remove(mNote);
        notifyDataSetChanged();
    }

    static class NotesViewHolder{

        public TextView titleTextView;
        public TextView timestampTextView;

        public NotesViewHolder(View itemView) {
            titleTextView = itemView.findViewById(R.id.title);
            timestampTextView = itemView.findViewById(R.id.timestamp);
        }
    }

}
