package com.example.covid_app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.covid_app.fragments.MainFragment;
import com.facebook.drawee.backends.pipeline.Fresco;

public class MainActivity extends AppCompatActivity {

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        if (findViewById(R.id.fragmentActivity) != null) {
            if (savedInstanceState != null) {
                return;
            }

            FragmentTransaction ft = fragmentManager.beginTransaction();
            //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            ft.replace(R.id.fragmentActivity, new MainFragment());
            ft.commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        getSupportFragmentManager().popBackStack();
//        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentActivity);
//        if(f instanceof MapFragment){
//            FragmentTransaction ft = fragmentManager.beginTransaction();
//            //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
//            ft.add(R.id.fragmentActivity, new MainFragment());
//            ft.commit();
//            fragmentManager.popBackStack();
//        }
    }
}