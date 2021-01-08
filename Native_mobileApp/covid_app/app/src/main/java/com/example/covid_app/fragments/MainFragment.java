package com.example.covid_app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.covid_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainFragment extends Fragment {


    AppCompatActivity activity;
    Context context;


    BottomNavigationView bottomNavigationView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        activity = (AppCompatActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
        firebaseAuthentification();
        fragmentManager = activity.getSupportFragmentManager();
        if (activity.findViewById(R.id.fragment) != null) {
            if (savedInstanceState != null) {
                return;
            }
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
                                FragmentTransaction ft = fragmentManager.beginTransaction();
                                if (currentUser != null) {
                                    //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                                    ft.replace(R.id.fragment, new ConnectedFragment());
                                } else {
                                    //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                                    ft.replace(R.id.fragment, new AccountFragment());
                                }
                                ft.commit();
                            }
                            return true;
                        }
                    });
        }
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        if (currentUser != null) {
            bottomNavigationView.setSelectedItemId(R.id.accountFragment);
            //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            ft.replace(R.id.fragment, new ConnectedFragment());
        } else {
            bottomNavigationView.setSelectedItemId(R.id.homeFragment);
            //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            ft.replace(R.id.fragment, new HomeFragment());
        }
        ft.commit();
    }


    void firebaseAuthentification(){
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
    }

    void initViews(){
        bottomNavigationView = activity.findViewById(R.id.bottomNavView);
    }

}