package com.niharkoli.smarteyeglassesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class InfoActivity extends AppCompatActivity {
    Calendar calendar;
    DatePickerDialog dpd;
    private static final String KEY_FNAME = "Fname";
    private static final String KEY_LNAME = "Lname";
    private static final String KEY_EMAIL = "Email";
    private static final String KEY_DOB = "DOB";
    private static final String KEY_AGE = "Age";
    private static final String KEY_HOME_ADDRESS = "HomeAddress";
    private static final String KEY_OFFICE_ADDRESS = "OfficeAddress";
    private static final String KEY_SEX = "Sex";
    private static final String KEY_UID = "UID";
    private static final String KEY_PHONE = "PhoneNumber";
    private TextInputEditText mFnameEditText;
    private TextInputEditText mLnameEditText;
    private TextInputEditText mDOBEditText;
    private TextInputEditText mEmailEditText;
    private TextInputEditText mAgeEditText;
    private TextInputEditText mAddress1EditText;
    private TextInputEditText mAddress2EditText;

    private String UID;
    private String DOB;
    private Spinner mSex;
    private String phoneNumber;

    FirebaseAuth mAuth;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);


        progressDialog = new ProgressDialog(this);
        hideProgressDialogWithTitle();

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        mSex = findViewById(R.id.sexSpinner);
        mFnameEditText = findViewById(R.id.FNameEditText);
        mLnameEditText = findViewById(R.id.LNameEditText);
        mDOBEditText = findViewById(R.id.DOBEditText);
        mEmailEditText = findViewById(R.id.EmailEditText);
        mAgeEditText = findViewById(R.id.AgeEditText);
        mAddress1EditText = findViewById(R.id.Address1EditText);
        mAddress2EditText = findViewById(R.id.Address2EditText);

        mAuth = FirebaseAuth.getInstance();

        Log.i("phonrNumber - ", mAuth.getCurrentUser().getPhoneNumber());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sex_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSex.setAdapter(adapter);

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Toast.makeText(this, "" + currentFirebaseUser.getUid(), Toast.LENGTH_SHORT).show();
        UID = currentFirebaseUser.getUid();

        mDOBEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogCalender();
            }
        });

        mDOBEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                openDialogCalender();
            }
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void openDialogCalender() {
        mDOBEditText.clearFocus();

        calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        dpd = new DatePickerDialog(InfoActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int mYear, int mMonth, int mDay) {
                mDOBEditText.setText(mDay + "/" + (mMonth + 1) + "/" + mYear);
            }
        }, year, month, day);
        dpd.show();
    }

    public void saveInfo(View view) {


        String fname = mFnameEditText.getText().toString();
        String lname = mLnameEditText.getText().toString();
        String email = mEmailEditText.getText().toString();
        String sex = mSex.getSelectedItem().toString();
        DOB = mDOBEditText.getText().toString();
        String age = mAgeEditText.getText().toString();
        String homeAdd = mAddress1EditText.getText().toString();
        String offAdd = mAddress2EditText.getText().toString();
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        if (fname.trim().isEmpty() || lname.trim().isEmpty() || email.trim().isEmpty() || sex.trim().isEmpty() || DOB.trim().isEmpty() || age.trim().isEmpty() || homeAdd.trim().isEmpty() || offAdd.trim().isEmpty()) {
            if (fname.trim().isEmpty()) {
                mFnameEditText.setError("Enter a Value");
            }
            if (lname.trim().isEmpty()) {
                mLnameEditText.setError("Enter a Value");
            }
            if (email.trim().isEmpty()) {
                mEmailEditText.setError("Enter a Value");
            }
            if (DOB.trim().isEmpty()) {
                mDOBEditText.setError("Enter a Value");
            }
            if (age.trim().isEmpty()) {
                mAgeEditText.setError("Enter a Value");
            }
            if (homeAdd.trim().isEmpty()) {
                mAddress1EditText.setError("Enter a Value");
            }
            if (offAdd.trim().isEmpty()) {
                mAddress2EditText.setError("Enter a Value");
            }
            return;
        }

        if (!isValidEmail(email)) {
            mEmailEditText.setError("Not a Valid Email!");
            return;
        }

        if (Integer.parseInt(age) < 18) {
            mAgeEditText.setError("Age should be greater than 18");
            return;
        }

        showProgressDialogWithTitle("Registering User!", "Hold on...");
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);


        Map<String, Object> data = new HashMap<>();
        data.put(KEY_UID, UID);
        data.put(KEY_FNAME, fname);
        data.put(KEY_LNAME, lname);
        data.put(KEY_EMAIL, email);
        data.put(KEY_DOB, DOB);
        data.put(KEY_SEX, sex);
        data.put(KEY_AGE, age);
        data.put(KEY_HOME_ADDRESS, homeAdd);
        data.put(KEY_OFFICE_ADDRESS, offAdd);
        data.put(KEY_PHONE, phoneNumber);

        //firestore
        db.collection("users").document(UID).set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(InfoActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(InfoActivity.this, HomeActivity.class);
                        intent.putExtra("phoneNumber", phoneNumber);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                });

    }

    private void showProgressDialogWithTitle(String title, String substring) {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //Without this user can hide loader by tapping outside screen
        progressDialog.setCancelable(false);
        //Setting Title
        progressDialog.setTitle(title);
        progressDialog.setMessage(substring);
        progressDialog.show();

    }

    // Method to hide/ dismiss Progress bar
    private void hideProgressDialogWithTitle() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.dismiss();
    }
}
