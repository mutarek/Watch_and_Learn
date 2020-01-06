package com.bdsoftwaresolution.watchandlearn.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bdsoftwaresolution.watchandlearn.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class SIgnUpActivity extends AppCompatActivity {

    private EditText email, pass;
    private Button signUp;
    private FirebaseAuth firebaseAuth;
    static final int Per = 500;
    String IMEINumber;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference emiref = db.collection("IMEI");
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        casting();
        checkPermissionforEMI();
        checkEMIExistornot();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String semail = email.getText().toString();
                String spass = pass.getText().toString();
                if (semail.isEmpty()) {
                    email.setError("Email is Required");
                    email.setFocusable(true);
                } else if (spass.isEmpty()) {
                    pass.setError("Password is required");
                    pass.setFocusable(true);
                } else {
                    signupwith(semail, spass);
                    progressDialog.show();
                }
            }
        });


    }

    private void checkEMIExistornot() {

    }

    private void signupwith(String semail, String spass) {
        firebaseAuth.createUserWithEmailAndPassword(semail, spass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toasty.success(SIgnUpActivity.this, "Successfully Account Created", Toasty.LENGTH_SHORT).show();
                    uploadtoDB(IMEINumber);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toasty.error(SIgnUpActivity.this, "" + e.getMessage(), Toasty.LENGTH_SHORT).show();
            }
        });
    }


    private void casting() {
        firebaseAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailET);
        pass = findViewById(R.id.passET);
        signUp = findViewById(R.id.signUpBtn);
        progressDialog = new ProgressDialog(this );
        progressDialog.setTitle("Please Wait.........");
    }

    private void checkPermissionforEMI() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, Per);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Per:
                if (grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getEMI();
                } else {
                    Toast.makeText(this, "We can't procced before permission", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void getEMI() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

        }
        IMEINumber = tm.getDeviceId();
    }

    private void uploadtoDB(String imeiNumber) {
        HashMap hashMap = new HashMap();
        hashMap.put("Imie_number", imeiNumber);
        emiref.document("INIE_NUMBER").set(emiref).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
        /*db.document().set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toasty.success(SIgnUpActivity.this,"Success added",Toasty.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toasty.success(SIgnUpActivity.this,"error"+e.getMessage(),Toasty.LENGTH_SHORT).show();
            }
        });*/


    }

}
