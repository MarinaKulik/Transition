package com.kulikmarina.transitions;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

public class RegisterActivity extends AppCompatActivity {
    private EditText passwd, confpasswd, email;
    private Button btnhavenewacc, btnregister;
    private ProgressBar mProgressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        passwd = findViewById(R.id.edtxt_password_reg);
        email = findViewById(R.id.edtxt_email_reg);
        confpasswd = findViewById(R.id.edtxt_password_reg);

        btnhavenewacc = findViewById(R.id.btn_haveaccount);
        btnregister = findViewById(R.id.btn_newaccount);
        mProgressBar = findViewById(R.id.progressBar_reg);

        mAuth = FirebaseAuth.getInstance();


        btnhavenewacc.setOnClickListener(n->{
            finish();

        });

        btnregister.setOnClickListener(v -> {
            String regEmail = email.getText().toString();
            String regPassword = passwd.getText().toString();
            String confpassword = confpasswd.getText().toString();

            if (!TextUtils.isEmpty(regEmail) && !TextUtils.isEmpty(regPassword) && !TextUtils.isEmpty(confpassword)) {
                if (passwd.equals(confpasswd)) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(regEmail, regPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                             Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
                             startActivity(intent);
                             finish();

                            }else{
                                Toast.makeText(RegisterActivity.this, "Error registration", Toast.LENGTH_SHORT).show();

                            } mProgressBar.setVisibility(View.INVISIBLE);


                        }
                    });

                } else {

                    Toast.makeText(RegisterActivity.this, "password does not match", Toast.LENGTH_SHORT).show();

                }

            }

        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            sendToMain();
    }

    private void sendToMain() {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
