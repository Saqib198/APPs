package com.app.notes.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.notes.DataBase.NoteDatabase;

import com.app.notes.entities.Note;
import com.app.notes.R;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNewNotes extends AppCompatActivity {

    private EditText NoteTitle,NoteSubTitle,NoteContent;
    private TextView NoteDate;
    private ImageView back,save;
    private View SubtitleIndicator;
    private String SelectedNoteColor;
    private ImageView ImageNote;
    private String selectedImagePath;
    private  TextView textWebUrl;
    private LinearLayout webLayout;

    private static final int REQUEST_CODE_PERMISSION_STORAGE = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private Note alreadyAvailable;
    private AlertDialog dialogAddUrl;
    private AlertDialog dialogDeleteNote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_notes);
        back= findViewById(R.id.back);
        save= findViewById(R.id.done);
        SubtitleIndicator= findViewById(R.id.SubtitleIndicator);
        ImageNote= findViewById(R.id.ImageNote);

        textWebUrl= findViewById(R.id.textWebURL);
        webLayout= findViewById(R.id.LayoutWeb);

        NoteTitle= findViewById(R.id.NoteTitle);
        NoteSubTitle= findViewById(R.id.NoteSubtitle);
        NoteContent= findViewById(R.id.NoteContent);
        SelectedNoteColor="#373737";
        selectedImagePath="";


        NoteDate= findViewById(R.id.textDateTime);
        NoteDate.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm:ss", Locale.getDefault())
                        .
                        format(new Date())
        );


        back.setOnClickListener(v -> {
            onBackPressed();
        });

        save.setOnClickListener(v -> {
          saveNote();
        });

        if(getIntent().getBooleanExtra("IsViewOrUpdate", false)){
            alreadyAvailable= (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
        findViewById(R.id.deleteweb).setOnClickListener(v -> {
            textWebUrl.setText(null);
            webLayout.setVisibility(View.GONE);

        });
        findViewById(R.id.deleteImage).setOnClickListener(v -> {
            ImageNote.setImageBitmap(null);
            ImageNote.setVisibility(View.GONE);
            findViewById(R.id.deleteImage).setVisibility(View.GONE);
            selectedImagePath="";
        });

        if(getIntent().getBooleanExtra("isFromQuickAction",false)){
            String type= getIntent().getStringExtra("QuickActionType");
            if(type!=null){
                if(type.equals("image")){
                    selectedImagePath= getIntent().getStringExtra("imagePath");
                    ImageNote.setVisibility(View.VISIBLE);
                    ImageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    findViewById(R.id.deleteImage).setVisibility(View.VISIBLE);
                }
                else if(type.equals("URL")){
                    textWebUrl.setText(getIntent().getStringExtra("URL"));
                    webLayout.setVisibility(View.VISIBLE);
                }
            }
        }
        initMiscallenous();
        setSubtitleIndicatorColor();
    }

    private void setViewOrUpdateNote()
    {
        NoteTitle.setText(alreadyAvailable.getTitle());
        NoteSubTitle.setText(alreadyAvailable.getSubtitle());
        NoteContent.setText(alreadyAvailable.getDescription());
        NoteDate.setText(alreadyAvailable.getDateTime());

        if(alreadyAvailable.getImagePath()!=null && !alreadyAvailable.getImagePath().trim().isEmpty())
        {
            ImageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailable.getImagePath()));
            ImageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.deleteImage).setVisibility(View.VISIBLE);
            selectedImagePath=alreadyAvailable.getImagePath();
        }
        if(alreadyAvailable.getWebLink()!=null && !alreadyAvailable.getWebLink().trim().isEmpty())
        {
            textWebUrl.setText(alreadyAvailable.getWebLink());
            webLayout.setVisibility(View.VISIBLE);
        }


    }

    private void saveNote(){

        if(NoteTitle.getText().toString().isEmpty()){
            Toast.makeText(this,"Title is required",Toast.LENGTH_SHORT).show();
            return;
        }
        else if(NoteContent.getText().toString().isEmpty() && NoteSubTitle.getText().toString().isEmpty()){
            Toast.makeText(this,"Content is required",Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note= new Note();
        String title= NoteTitle.getText().toString();
        String subtitle= NoteSubTitle.getText().toString();
        String content= NoteContent.getText().toString();
        String date= NoteDate.getText().toString();

        note.setTitle(title);
        note.setSubtitle(subtitle);
        note.setDescription(content);
        note.setDateTime(date);
        note.setColor(SelectedNoteColor);
        note.setImagePath(selectedImagePath);

        if(webLayout.getVisibility()==View.VISIBLE){
            note.setWebLink(textWebUrl.getText().toString());
        }

        if(alreadyAvailable!=null){
            note.setId(alreadyAvailable.getId());
        }

        class SaveNoteTask extends AsyncTask<Void,Void,Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NoteDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent= new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }

         new SaveNoteTask().execute();
    }

    private void initMiscallenous()
    {
        final LinearLayout layoutMiscellenous= findViewById(R.id.layoutMiscellenous);

        final ImageView color1= findViewById(R.id.imageColor1);
        final ImageView color2= findViewById(R.id.imageColor2);
        final ImageView color3= findViewById(R.id.imageColor3);
        final ImageView color4= findViewById(R.id.imageColor4);
        final ImageView color5= findViewById(R.id.imageColor5);
        layoutMiscellenous.findViewById(R.id.imageColor1).setOnClickListener(v -> {
            SelectedNoteColor="#373737";
            color1.setImageResource(R.drawable.ic_done);
            color2.setImageResource(0);
            color3.setImageResource(0);
            color4.setImageResource(0);
            color5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        layoutMiscellenous.findViewById(R.id.imageColor2).setOnClickListener(v -> {
            SelectedNoteColor="#FFC107";
            color1.setImageResource(0);
            color2.setImageResource(R.drawable.ic_done);
            color3.setImageResource(0);
            color4.setImageResource(0);
            color5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        layoutMiscellenous.findViewById(R.id.imageColor3).setOnClickListener(v -> {
            SelectedNoteColor="#3F51B5";
            color1.setImageResource(0);
            color2.setImageResource(0);
            color3.setImageResource(R.drawable.ic_done);
            color4.setImageResource(0);
            color5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        layoutMiscellenous.findViewById(R.id.imageColor4).setOnClickListener(v -> {
            SelectedNoteColor="#F82D1E";
            color1.setImageResource(0);
            color2.setImageResource(0);
            color3.setImageResource(0);
            color4.setImageResource(R.drawable.ic_done);
            color5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        layoutMiscellenous.findViewById(R.id.imageColor5).setOnClickListener(v -> {
            SelectedNoteColor="#4CAF50";
            color1.setImageResource(0);
            color2.setImageResource(0);
            color3.setImageResource(0);
            color4.setImageResource(0);
            color5.setImageResource(R.drawable.ic_done);
            setSubtitleIndicatorColor();
        });

        if(alreadyAvailable!=null && alreadyAvailable.getColor()!=null && !alreadyAvailable.getColor().trim().isEmpty()){
            switch (alreadyAvailable.getColor()){
                case "#373737":
                    layoutMiscellenous.findViewById(R.id.imageColor1).performClick();
                    break;
                case "#FFC107":
                    layoutMiscellenous.findViewById(R.id.imageColor2).performClick();
                    break;
                case "#3F51B5":
                    layoutMiscellenous.findViewById(R.id.imageColor3).performClick();
                    break;
                case "#F82D1E":
                    layoutMiscellenous.findViewById(R.id.imageColor4).performClick();
                    break;
                case "#4CAF50":
                    layoutMiscellenous.findViewById(R.id.imageColor5).performClick();
                    break;
            }
        }
        layoutMiscellenous.findViewById(R.id.ImageLayout).setOnClickListener(v -> {


            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            },
                            1
                    );

            }
            else{
               selectImage();
            }
        });



        LinearLayout url;
        url=layoutMiscellenous.findViewById(R.id.URLLayout);
        url.setOnClickListener(v -> {
           showAddUrlDialog();
        });

        if(alreadyAvailable!=null){
            layoutMiscellenous.findViewById(R.id.DeleteNoteLayout).setVisibility(View.VISIBLE);
            layoutMiscellenous.findViewById(R.id.DeleteNoteLayout).setOnClickListener(v -> {
                showDeleteNoteDialog();
            });

        }

    }

    private void showDeleteNoteDialog() {

        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewNotes.this);
            View view = LayoutInflater.from(this).
                    inflate(R.layout.delete_container_layout,
                            (ViewGroup) findViewById(R.id.LayoutDeleteContainer));
            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.DeleteNOTEButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NoteDatabase.getDatabase(
                                    getApplicationContext()).noteDao().deleteNote(alreadyAvailable);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }

            });

            view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });
            dialogDeleteNote.show();
        }

    }

    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable= (GradientDrawable) SubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(SelectedNoteColor));
    }

    private void selectImage()
    {
        Intent intent= new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_SELECT_IMAGE && resultCode==RESULT_OK ){
            if(data!=null){
            Uri selectedImage= data.getData();
            if(selectedImage!=null){
                try {
                    InputStream inputStream= getContentResolver().openInputStream(selectedImage);
                    Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                    ImageNote.setImageBitmap(bitmap);
                    ImageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.deleteImage).setVisibility(View.VISIBLE);
                    selectedImagePath= getPathFromUri(selectedImage);

                } catch (Exception exception) {
                    Toast toast= Toast.makeText(this,exception.getMessage(),Toast.LENGTH_SHORT);
                }}
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

    private void showAddUrlDialog()
    {
     if(dialogAddUrl==null)
     {
         AlertDialog.Builder builder= new AlertDialog.Builder(this);
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
                        Toast.makeText(CreateNewNotes.this,"Please Enter URL",Toast.LENGTH_SHORT).show();
                    }else if(!Patterns.WEB_URL.matcher(inputUrl.getText().toString().trim()).matches()){
                        Toast.makeText(CreateNewNotes.this,"ENTER VALID URL",Toast.LENGTH_SHORT).show();
                        }
                    else{
                        textWebUrl.setText(inputUrl.getText().toString().trim());
                        webLayout.setVisibility(View.VISIBLE);
                        dialogAddUrl.dismiss();

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