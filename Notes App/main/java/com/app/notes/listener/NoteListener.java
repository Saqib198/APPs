package com.app.notes.listener;

import com.app.notes.entities.Note;

public interface NoteListener {

    void onNoteClicked(Note note, int position);
}
