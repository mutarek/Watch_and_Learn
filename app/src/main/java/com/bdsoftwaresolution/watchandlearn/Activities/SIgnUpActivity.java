package com.bdsoftwaresolution.watchandlearn.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        casting();
        checkPermissionforEMI();
        getEMI();
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
        databaseReference.child("EMI").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue().toString();
                Toast.makeText(SIgnUpActivity.this, ""+data, Toast.LENGTH_SHORT).show();
                if (data.contains(IMEINumber))
                {
                    Toast.makeText(SIgnUpActivity.this, "ime found", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(SIgnUpActivity.this, "data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(SIgnUpActivity.this,"Error"+databaseError.getMessage(),Toasty.LENGTH_SHORT).show();
            }
        });
        emiref.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toasty.error(SIgnUpActivity.this, "Error" + e.getMessage(), Toasty.LENGTH_SHORT).show();
                    return;
                }

                List<String> cities = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc.get("No") != null) {
                        String data = doc.getData().toString();
                        if (data.contains(IMEINumber)) {
                            Toast.makeText(SIgnUpActivity.this, "Data Matched", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SIgnUpActivity.this, "Data Not Matched", Toast.LENGTH_SHORT).show();
                        }
                        cities.add(doc.getString("name"));
                        Toast.makeText(SIgnUpActivity.this, "" + doc.getData(), Toast.LENGTH_SHORT).show();
                    }
                }
                //Log.d(TAG, "Current cites in CA: " + cities);
            }
        });
    }

    private void signupwith(String semail, String spass) {
        firebaseAuth.createUserWithEmailAndPassword(semail, spass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toasty.success(SIgnUpActivity.this, "Successfully Account Created", Toasty.LENGTH_SHORT).show();
                    String currentUser = firebaseAuth.getCurrentUser().getUid();
                    uploadtoDB(currentUser);
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

    private void uploadtoDB(String currentUser) {
        HashMap hashMap = new HashMap();
        hashMap.put("No", IMEINumber);
        databaseReference.child("EMI").child(currentUser).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                updateUI();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(SIgnUpActivity.this, "Error" + e.getMessage(), Toasty.LENGTH_LONG).show();
            }
        });
    }


    private void casting() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        email = findViewById(R.id.emailET);
        pass = findViewById(R.id.passET);
        signUp = findViewById(R.id.signUpBtn);
        progressDialog = new ProgressDialog(this);
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


    private void updateUI() {
        startActivity(new Intent(SIgnUpActivity.this, CompleProfileActivity.class));
        finish();
    }

}
