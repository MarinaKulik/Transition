package com.kulikmarina.transitions;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";


    private ProgressBar mProgressBar;
    private EditText mEdTxttEmail, mEdTxtPassword;
    private Button mBtnLogIn, mBtnRegistration;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mProgressBar = findViewById(R.id.progressBar_login);
        mEdTxtPassword = findViewById(R.id.edtxt_password);
        mEdTxttEmail = findViewById(R.id.edtxt_email);
        mBtnLogIn = findViewById(R.id.btn_login);
        mBtnRegistration = findViewById(R.id.btn_newlogo);

        mBtnRegistration .setOnClickListener(c -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });


        mAuth = FirebaseAuth.getInstance();


        mBtnLogIn.setOnClickListener(v -> {

            String loginEmail = mEdTxttEmail.getText().toString();
            String loginPassword = mEdTxtPassword.getText().toString();

            if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)) {
                mProgressBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    sendToMainActivity();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();

                                }
                                mProgressBar.setVisibility(View.INVISIBLE);

                            }
                        });

            }

        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in

            sendToMainActivity();


        } else {
            // No user is signed in
        }
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
