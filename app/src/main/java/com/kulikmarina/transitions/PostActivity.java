package com.kulikmarina.transitions;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class PostActivity extends AppCompatActivity {

    private Toolbar postoolbar;
    private ImageView postImg;
    private Button btnPost;
    private EditText edPost;

    private Bitmap compressedImageFile;

    private Uri postImguri = null;
    String current_user_id;


    private StorageReference mStorageReference;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        postoolbar = findViewById(R.id.post_toolbar);

        setSupportActionBar(postoolbar);
        getSupportActionBar().setTitle("Add new post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        edPost = findViewById(R.id.editText_post);
        postImg = findViewById(R.id.img_newpost);
        btnPost = findViewById(R.id.btn_post);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        current_user_id = mAuth.getCurrentUser().getUid();

        postImg.setOnClickListener(c -> {

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMinCropResultSize(512, 512)
                    .setAspectRatio(1, 1)
                    .start(PostActivity.this);

        });


        btnPost.setOnClickListener(p -> {

            String description = edPost.getText().toString();
            if (!TextUtils.isEmpty(description) && postImguri != null) {
                String randomName = UUID.randomUUID().toString();
                StorageReference filePath = mStorageReference.child("post_img").child(randomName + ".jpg");
                filePath.putFile(postImguri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        String downloadUri = task.getResult().toString();
                        if (task.isSuccessful()) {

                            File newImgFile = new File(postImguri.getPath());
                            try {
                                compressedImageFile = new Compressor(PostActivity.this)
                                        .setMaxHeight(100)
                                        .setMaxWidth(100)
                                        .setQuality(2)
                                        .compressToBitmap(newImgFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] thumDta = baos.toByteArray();

                            UploadTask uploadTask = mStorageReference.child("post_img/thumbs").child(randomName + ".jpg").putBytes(thumDta);

                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {




                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("image_url", downloadUri);
                                    postMap.put("description", description);
                                    postMap.put("user_id", current_user_id);
                                    postMap.put("time_stamp", FieldValue.serverTimestamp());


                                    mFirebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(PostActivity.this, "Post was added", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(PostActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {

                                            }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
//Error
                                }
                            });


                                }
                            });

                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(PostActivity.this, error, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImguri = result.getUri();
                postImg.setImageURI(postImguri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
