package com.example.ganeshr.easykeep.activity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ganeshr.easykeep.R;
import com.example.ganeshr.easykeep.model.NotesModel;
import com.example.ganeshr.easykeep.rest.RealmManger;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class Notes extends AppCompatActivity {

    EditText txtTitle, txtNote;
    NotesModel m;
    @BindView(R.id.update_btn)
    Button updateButton;
    @BindView(R.id.save_btn)
    Button saveButton;
    AlertDialog dialog;
    AlertDialog.Builder builder;
    private HashMap<EditText, Boolean> hashMap;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtTitle = (EditText) findViewById(R.id.txt_title);
        txtNote = (EditText) findViewById(R.id.txt_note);

        ButterKnife.bind(this);
        hashMap = new HashMap<>();
        updateButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.VISIBLE);
        m = new NotesModel();

    }

    // Set PDF document Properties
    public void addMetaData(Document document) {
        document.addTitle("Easy Keep");
        document.addSubject("Person Info");
        document.addKeywords("Personal,Education, Skills");
        document.addAuthor("TAG");
        document.addCreator("TAG");
    }

    public void addTitlePage(Document document) throws DocumentException {
// Font Style for Document
        Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 22, Font.BOLD
                | Font.UNDERLINE, BaseColor.GRAY);
        Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        Font normal = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);

// Start New Paragraph
        Paragraph prHead = new Paragraph();
// Set Font in this Paragraph
        prHead.setFont(titleFont);
// Add item into Paragraph
        prHead.add(txtTitle.getText().toString() + "\n");


        prHead.setFont(catFont);
        prHead.setAlignment(Element.ALIGN_CENTER);

// Add all above details into Document
        document.add(prHead);


        Paragraph prProfile = new Paragraph();
        prProfile.setFont(normal); //smallBold
        prProfile.add("\n \n " + txtNote.getText().toString() + "\n ");

        prProfile.setFont(smallBold);
        document.add(prProfile);

// Create new Page in PDF
        document.newPage();
    }


    @OnTextChanged({R.id.txt_title, R.id.txt_note})
    void OnTextChanged() {
        if (validateMandatoryFields()) {
            saveButton.setClickable(true);
        } else {
            //Utility.displayErrorMessage(edt_last_date_apply, "Please fill mandatory fields.");
            saveButton.setClickable(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.save_btn)
    public void manageSave() {
        if (validateMandatoryFields()) {
            getData();
            saveAsPdf(txtTitle.getText().toString(), txtNote.getText().toString());
            RealmManger.getInstance(Notes.this).addorUpadte(m);
            Toast.makeText(getApplicationContext(), "Successful !", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void saveAsPdf(String title, String info) {

// Destination Folder and File name
        String FILE = Environment.getExternalStorageDirectory().toString()
                + "/EasyKeep/" + title + ".pdf";

// Create New Blank Document
        Document document = new Document(PageSize.A4);

        // Create Directory in External Storage
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/EasyKeep");
        Log.d("path--", "" + myDir);
        myDir.mkdirs();
        Log.d("path--", "created");


// Create Pdf Writer for Writting into New Created Document
        try {
            PdfWriter.getInstance(document, new FileOutputStream(FILE));

            // Open Document for Writting into document
            document.open();

            // User Define Method
            addMetaData(document);
            addTitlePage(document);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Close Document after writting all content
        document.close();

        Log.d("Path-- ", "" + FILE);
    }

    public void getData() {
        m.setTitle(txtTitle.getText().toString());
        m.setNote(txtNote.getText().toString());
        m.setDate(DateFormat.getDateTimeInstance().format(new Date()));

    }

    @Override
    public void onBackPressed() {
        if (validateMandatoryFields()) {
            saveDialog();
        } else {
            super.onBackPressed();
        }
    }

    public void saveDialog() {
        builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);


        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                manageSave();

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });

        dialog = builder.create();
        dialog.setTitle("Save changes");
        dialog.setMessage("are you sure want to leave? to save chages click on save button.");
        dialog.show();

    }

    private boolean validateMandatoryFields() {
        checkEmptyFields();

        if (hashMap.size() == Collections.frequency(hashMap.values(), true)) {
            return true;
        }
        return false;
    }

    private void checkEmptyFields() {
        isEmpty(txtNote);
        isEmpty(txtTitle);

    }

    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() == 0) {
            hashMap.put(etText, false);
            return true;
        } else {
            hashMap.put(etText, true);
            return false;
        }
    }

}
