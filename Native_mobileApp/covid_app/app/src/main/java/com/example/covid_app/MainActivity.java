package com.example.covid_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.covid_app.fragments.AccountFragment;
import com.example.covid_app.fragments.HomeFragment;
import com.example.covid_app.fragments.NewsFragment;
import com.example.covid_app.fragments.StatisticsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getViews();
        fragmentManager = getSupportFragmentManager();
        if (findViewById(R.id.fragment) != null) {
            if (savedInstanceState != null) {
                return;
            }
            fragmentManager.beginTransaction().replace(R.id.fragment, new HomeFragment(), null).commit();
            bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.homeFragment) {
                            fragmentManager.beginTransaction().replace(R.id.fragment, new HomeFragment(), null).commit();
                        } else if (item.getItemId() == R.id.statisticsFragment) {
                            fragmentManager.beginTransaction().replace(R.id.fragment, new StatisticsFragment(), null).commit();
                        } else if (item.getItemId() == R.id.newsFragment) {
                            fragmentManager.beginTransaction().replace(R.id.fragment, new NewsFragment(), null).commit();
                        } else if (item.getItemId() == R.id.accountFragment) {
                            fragmentManager.beginTransaction().replace(R.id.fragment, new AccountFragment(), null).commit();
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

    void getViews(){
        bottomNavigationView = findViewById(R.id.bottomNavView);
    }
}