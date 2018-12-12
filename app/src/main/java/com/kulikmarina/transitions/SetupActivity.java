package com.kulikmarina.transitions;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private CircleImageView setupImgView;

    private EditText setupName;
    private Button btnsetup;
    private Uri resultUri = null;
    private boolean ischanched;

    private StorageReference mStorageReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private String user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setupImgView = findViewById(R.id.circleimg);
        setupName = findViewById(R.id.ed_setup_name);
        btnsetup = findViewById(R.id.btn_setup);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        user_id = mFirebaseAuth.getCurrentUser().getUid();

        Toolbar setuptoolbar = findViewById(R.id.setuptoolbar);
        setSupportActionBar(setuptoolbar);
        getSupportActionBar().setTitle("Setup");

        btnsetup.setEnabled(false);

        mFirebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        //Toast.makeText(SetupActivity.this, "Data exists", Toast.LENGTH_SHORT).show();
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        resultUri = Uri.parse(image);
                        setupName.setText(name);


                        RequestOptions placeholder = new RequestOptions();
                        placeholder.placeholder(R.drawable.ic_search);

                        Glide.with(SetupActivity.this)
                                .setDefaultRequestOptions(placeholder)
                                .load(image).into(setupImgView);


                    } else {
                        Toast.makeText(SetupActivity.this, "Data does not exist", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore retrieve error   " + error, Toast.LENGTH_SHORT).show();
                }
                btnsetup.setEnabled(true);
            }
        });

        btnsetup.setOnClickListener(z -> {

            String user_name = setupName.getText().toString();
            if (!TextUtils.isEmpty(user_name) && resultUri != null) {
                if (ischanched) {

                    String user_id = mFirebaseAuth.getCurrentUser().getUid();

                    StorageReference image_path = mStorageReference.child("profile_img").child(user_id + ".png");
                    image_path.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return image_path.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {

                                firestoreSAVE(task, user_name);

                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, error, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                } else {
                    firestoreSAVE(null, user_name);

                }
            }
        });


        setupImgView.setOnClickListener(a -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SetupActivity.this, "Permissions denied", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                } else {
                    imagePicker();

                }
            } else {
                imagePicker();

            }


        });
    }

    private void firestoreSAVE(Task<Uri> task, String user_name) {
        Uri downloadUri;
        if (task != null) {
            downloadUri = task.getResult();
        } else {
            downloadUri = resultUri;

        }
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", user_name);
        userMap.put("image", downloadUri.toString());


        mFirebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SetupActivity.this, "All is good in firestore", Toast.LENGTH_SHORT).show();


                    Intent mainint = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainint);
                } else {
                    Toast.makeText(SetupActivity.this, "Firebasestore error", Toast.LENGTH_SHORT).show();
                }

            }
        });
        Toast.makeText(SetupActivity.this, "Loading is succeed", Toast.LENGTH_SHORT).show();
    }

    private void imagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                setupImgView.setImageURI(resultUri);
                ischanched = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
