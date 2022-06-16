package com.app.notes.adapter;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import com.app.notes.entities.Note;
import com.app.notes.listener.NoteListener;
import com.app.notes.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private NoteListener noteListener;
    private List<Note> notes;
    private Timer timer;
    private List<Note> noteSource;


    public NotesAdapter(List<Note> notes, NoteListener noteListener) {
        this.notes = notes;
        this.noteListener = noteListener;
        noteSource=notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_container_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.setNote(notes.get(position));
        holder.LayoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteListener.onNoteClicked(notes.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView noteTitle,subTitle,datetime;
        LinearLayout LayoutNote;
        ImageView noteimage;
        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.title);
            subTitle = itemView.findViewById(R.id.subtitle);
            datetime = itemView.findViewById(R.id.dateandtime);
            LayoutNote = itemView.findViewById(R.id.LayoutNotes);
            noteimage = itemView.findViewById(R.id.ImageNote);
        }

        void setNote(Note note) {
            noteTitle.setText(note.getTitle());
            if (note.getSubtitle() != null) {
                subTitle.setText(note.getSubtitle());
            } else {
                subTitle.setVisibility(View.GONE);
            }
            datetime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) LayoutNote.getBackground();
            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }else{
            gradientDrawable.setColor(Color.parseColor("#373737"));
            }
            if(note.getImagePath()!=null){
                noteimage.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                noteimage.setVisibility(View.VISIBLE);
            }else{
                noteimage.setVisibility(View.GONE);
            }
        }
        }

        //Search Note by Title
            public void SearchNote( final String searchKeyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyword.trim().isEmpty()){
                    notes=noteSource;
                }
                else{
                    ArrayList<Note> temp = new ArrayList<>();
                    for(Note note:noteSource){
                        if(note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase()) ||
                                note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getDateTime().toLowerCase().contains(searchKeyword.toLowerCase())){
                            temp.add(note);
                        }
                    }
                    notes=temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
        }

        public void cancelTimer() {
            if (timer != null) {
                timer.cancel();
            }
        }
}
