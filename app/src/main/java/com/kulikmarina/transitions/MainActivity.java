package com.kulikmarina.transitions;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private Toolbar maintoolbar;
    private FloatingActionButton addPostBtn;
    private BottomNavigationView mainBotNav;

    private HomeFragment mHomeFragment;
    private AccountFragment mAccountFragment;
    private NotificationFragment mNotificationFragment;


    private String current_user_id;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        maintoolbar = findViewById(R.id.main_toolbar);
        addPostBtn = findViewById(R.id.add_post);
        mainBotNav = findViewById(R.id.bot_nav_view);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        setSupportActionBar(maintoolbar);
        getSupportActionBar().setTitle("Photo Blog");

        if(mAuth.getCurrentUser()!=null) {

            mHomeFragment = new HomeFragment();
            mAccountFragment = new AccountFragment();
            mNotificationFragment = new NotificationFragment();

            replaceFragment(mHomeFragment);
            mainBotNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {

                        case R.id.bottom_home:
                            replaceFragment(mHomeFragment);
                            return true;
                        case R.id.bottom_account:
                            replaceFragment(mAccountFragment);
                            return true;
                        case R.id.bottom_notif:
                            replaceFragment(mNotificationFragment);
                            return true;

                        default:
                            return false;

                    }

                }
            });


            addPostBtn.setOnClickListener(b -> {

                Intent intent = new Intent(MainActivity.this, PostActivity.class);
                startActivity(intent);

            });
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is signed in
            sendToLogin();


        } else {
            current_user_id = mAuth.getCurrentUser().getUid();
            mFirebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        if (!task.getResult().exists()) {
                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();

                        }

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();

                    }

                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(intent);
                return true;

            default:
                return false;

        }

    }

    private void logout() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();


    }
}
