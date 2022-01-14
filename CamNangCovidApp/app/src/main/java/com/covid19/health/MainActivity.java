package com.covid19.health;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.design.widget.BottomNavigationView;
//import android.support.design.widget.CoordinatorLayout;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v4.view.GravityCompat;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
//                case R.id.navigationGift:
//                    return true;
//                case R.id.navigationHistory:
//                    return true;
                case R.id.navigationHome:
                    changeFragment(new InfoFragment());
                    return true;
                case  R.id.navigationRecord:
                    changeFragment(new RecordFragment());
                    return true;
//                case  R.id.navigationMenu:
//                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//                    drawer.openDrawer(GravityCompat.START);
//                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();

        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomNavigationView.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationBehavior());

        bottomNavigationView.setSelectedItemId(R.id.navigationHome);
        changeFragment(new InfoFragment());
//        setNavigationDrawer(); // call method
        requestMicrophonePermission();
        requestWriteFilePermission();
        requestReadFilePermission();
    }

//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }

    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO}, 13);
        }
    }

    private void requestWriteFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void requestReadFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }
    }

    public void changeFragment(Fragment frag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
        transaction.commit(); // commit the changes
    }

    public void enableNavigation(boolean is_enable) {
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setEnabled(is_enable);
        }
    }

//    public void removeHomeSelected() {
//        bottomNavigationView.setSelected(false);
////        bottomNavigationView.setItemIconTintList(null);
////        bottomNavigationView.setItemTextColor(null);
//    }

//    private void setNavigationDrawer() {
//        NavigationView navView = (NavigationView) findViewById(R.id.navigation); // initiate a Navigation View
//        // implement setNavigationItemSelectedListener event on NavigationView
//        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(MenuItem menuItem) {
//                Fragment frag = null; // create a Fragment Object
//                int itemId = menuItem.getItemId(); // get selected menu item's id
//                // check selected menu item's id and replace a Fragment Accordingly
//                if (itemId == R.id.first) {
//                    frag = new RecordFragment();
//                } else if (itemId == R.id.second) {
//                    frag = new FAQFragment();
//                }
//                // display a toast message with menu item's title
//                Toast.makeText(getApplicationContext(), menuItem.getTitle(), Toast.LENGTH_SHORT).show();
//                if (frag != null) {
//                    changeFragment(frag);
//                    return true;
//                }
//                return false;
//            }
//        });
//    }
}
