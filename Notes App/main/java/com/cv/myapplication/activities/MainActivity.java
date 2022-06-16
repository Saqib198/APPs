package com.cv.myapplication.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.cv.myapplication.DataBase.NoteDatabase;
import com.cv.myapplication.R;
import com.cv.myapplication.adapter.NotesAdapter;
import com.cv.myapplication.dao.NoteDao;
import com.cv.myapplication.entities.Note;
import com.cv.myapplication.listener.NoteListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NoteListener {


    private static final int REQUEST_CODE_CREATE_NOTE = 1;
    private static final int REQUEST_CODE_UPDATE_NOTE = 2;
    private static final int REQUEST_CODE_SHOW_NOTE = 3;
    private static final int REQUEST_CODE_SELECT_IMAGE = 4;
    private static final int REQUEST_CODE_PERMISSION_STORAGE = 5;


    private List<Note> noteslist;
    private NotesAdapter noteAdapter;
    private RecyclerView notesRecyclerView;

    private int noteClickedPosition=-1;
    private AlertDialog dialogAddUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ImageView NewNote=findViewById(R.id.CreateNewNote);
        NewNote.setOnClickListener(v -> {
                   startActivityForResult(
                           new Intent(getApplicationContext(),CreateNewNotes.class),
                            REQUEST_CODE_CREATE_NOTE
                   );
                });
        notesRecyclerView = findViewById(R.id.itemRecyclerView);
        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));

        noteslist=new ArrayList<>();
        noteAdapter=new NotesAdapter(noteslist,this);
        notesRecyclerView.setAdapter(noteAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTE,false);

        EditText search=findViewById(R.id.search_text);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteslist.size()>0) {
                    noteAdapter.SearchNote(s.toString());
                }
            }
        });

        findViewById(R.id.ImageaddNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(),CreateNewNotes.class),
                        REQUEST_CODE_CREATE_NOTE
                );
            }
        });
        findViewById(R.id.imageadd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            },
                            1
                    );

                }
                else{
                    selectImage();
                }
            }
        });

        findViewById(R.id.webadd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUrlDialog();
            }
        });
    }

    private void selectImage()
    {
        Intent intent= new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE_PERMISSION_STORAGE && grantResults.length>0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            }
            else{

                Toast.makeText(this,"Permission Denied :",Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver().
                query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
    public void onNoteClicked(Note note, int position) {

        noteClickedPosition=position;
        Intent intent = new Intent(getApplicationContext(), CreateNewNotes.class);
        intent.putExtra("IsViewOrUpdate",true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }



    private void getNotes(final int  requestCode, final boolean isNoteDeleted) {
        class getNoteTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {

                return NoteDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @SuppressLint("NotifyDataSetChanged")
            protected void onPostExecute(List <Note> notes){
                super.onPostExecute(notes);
              if(requestCode==REQUEST_CODE_SHOW_NOTE){
                  noteslist.addAll(notes);
                  noteAdapter.notifyDataSetChanged();
              }else if(requestCode==REQUEST_CODE_CREATE_NOTE) {
                  noteslist.add(0, notes.get(0));
                  noteAdapter.notifyItemInserted(0);
              notesRecyclerView.smoothScrollToPosition(0);
              }else if(requestCode==REQUEST_CODE_UPDATE_NOTE){
                  noteslist.remove(noteClickedPosition);
                  if(isNoteDeleted){
                      noteAdapter.notifyItemRemoved(noteClickedPosition);
                  }
                  else{
                      noteslist.add(noteClickedPosition,notes.get(noteClickedPosition));
                      noteAdapter.notifyItemChanged(noteClickedPosition);
                  }
              }


            }

        }
        new getNoteTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_CREATE_NOTE && resultCode==RESULT_OK){
            getNotes(REQUEST_CODE_CREATE_NOTE,false);
        }
        else if(requestCode==REQUEST_CODE_UPDATE_NOTE && resultCode==RESULT_OK){
            if(data!=null){

                getNotes(REQUEST_CODE_UPDATE_NOTE,data.getBooleanExtra("isNoteDeleted",false));

            }
        }
        else if(requestCode==REQUEST_CODE_SELECT_IMAGE && resultCode==RESULT_OK){
          if(data!=null){
              Uri selectedImageUri=data.getData();
              if(selectedImageUri!=null){
                  try{
                      String selectedImagePath=getPathFromUri(selectedImageUri);
                        Intent intent=new Intent(getApplicationContext(),CreateNewNotes.class);
                        intent.putExtra("isFromQuickAction",true);
                        intent.putExtra("QuickActionType","image");
                        intent.putExtra("imagePath",selectedImagePath);
                        startActivityForResult(intent,REQUEST_CODE_CREATE_NOTE);


                  }catch (Exception exception){
                     Toast.makeText(this,exception.getMessage(),Toast.LENGTH_SHORT).show();
                  }
              }
          }
        }
    }
    private void showAddUrlDialog()
    {
        if(dialogAddUrl==null)
        {
            AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
            View view= LayoutInflater.from(this).inflate(R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer));

            builder.setView(view);
            dialogAddUrl= builder.create();
            if(dialogAddUrl.getWindow()!=null)
            {
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));

            }
            EditText inputUrl=view.findViewById(R.id.inputURL);
            inputUrl.requestFocus();

            view.findViewById(R.id.addDone).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(inputUrl.getText().toString().trim().isEmpty()){
                        Toast.makeText(MainActivity.this,"Please Enter URL",Toast.LENGTH_SHORT).show();
                    }else if(!Patterns.WEB_URL.matcher(inputUrl.getText().toString().trim()).matches()){
                        Toast.makeText(MainActivity.this,"ENTER VALID URL",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        dialogAddUrl.dismiss();
                        Intent intent=new Intent(getApplicationContext(),CreateNewNotes.class);
                        intent.putExtra("isFromQuickAction",true);
                        intent.putExtra("QuickActionType","URL");
                        intent.putExtra("URL",inputUrl.getText().toString());
                        startActivityForResult(intent,REQUEST_CODE_CREATE_NOTE);

                    }

                }

            });
            view.findViewById(R.id.cancelDone).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddUrl.dismiss();
                }
            });
        }
        dialogAddUrl.show();
    }

}