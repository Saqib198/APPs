package com.cv.myapplication.listener;

import com.cv.myapplication.entities.Note;

public interface NoteListener {

    void onNoteClicked(Note note, int position);
}
