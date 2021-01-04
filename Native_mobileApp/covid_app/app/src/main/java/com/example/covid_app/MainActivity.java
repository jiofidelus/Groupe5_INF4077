package com.example.covid_app;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.covid_app.fragments.AccountFragment;
import com.example.covid_app.fragments.ConnectedFragment;
import com.example.covid_app.fragments.HomeFragment;
import com.example.covid_app.fragments.StatisticsFragment;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        initViews();
        firebaseAuthentification();
        fragmentManager = getSupportFragmentManager();
        if (findViewById(R.id.fragment) != null) {
            if (savedInstanceState != null) {
                return;
            }

            FragmentTransaction ft = fragmentManager.beginTransaction();
            //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            ft.replace(R.id.fragment, new HomeFragment());
            ft.commit();
            bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.homeFragment) {
                            FragmentTransaction ft = fragmentManager.beginTransaction();
                            //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                            ft.replace(R.id.fragment, new HomeFragment());
                            ft.commit();
                        } else if (item.getItemId() == R.id.statisticsFragment) {
                            FragmentTransaction ft = fragmentManager.beginTransaction();
                            //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                            ft.replace(R.id.fragment, new StatisticsFragment());
                            ft.commit();
                        } else if (item.getItemId() == R.id.accountFragment) {
                            if (currentUser != null) {
                                FragmentTransaction ft = fragmentManager.beginTransaction();
                                //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                                ft.replace(R.id.fragment, new ConnectedFragment());
                                ft.commit();
                            } else {
                                FragmentTransaction ft = fragmentManager.beginTransaction();
                                //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                                ft.replace(R.id.fragment, new AccountFragment());
                                ft.commit();
                            }
                        }
                        return true;
                    }
                });
        }
    }

    @Override
    public void onBackPressed() {
        if(fragmentManager.getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate();
        else
            super.onBackPressed();
    }

    void firebaseAuthentification(){
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
    }

    void initViews(){
        bottomNavigationView = findViewById(R.id.bottomNavView);
    }
}