package covid19.detection;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    DrawerLayout dLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        // implement setNavigationOnClickListener event
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dLayout.openDrawer(Gravity.LEFT);
            }
        });
        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout); // initiate a DrawerLayout
        changeFragment(new RecordFragment());
        setNavigationDrawer(); // call method
        requestMicrophonePermission();
        requestWriteFilePermission();
        requestReadFilePermission();
    }

    private void requestMicrophonePermission() {
        requestPermissions(
                new String[] {android.Manifest.permission.RECORD_AUDIO}, 13);
    }

    private void requestWriteFilePermission() {
        requestPermissions(
                new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    private void requestReadFilePermission() {
        requestPermissions(
                new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
    }

    private void changeFragment(Fragment frag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
        transaction.commit(); // commit the changes
        dLayout.closeDrawers(); // close the all open Drawer Views
    }

    private void setNavigationDrawer() {
        NavigationView navView = (NavigationView) findViewById(R.id.navigation); // initiate a Navigation View
        // implement setNavigationItemSelectedListener event on NavigationView
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Fragment frag = null; // create a Fragment Object
                int itemId = menuItem.getItemId(); // get selected menu item's id
                // check selected menu item's id and replace a Fragment Accordingly
                if (itemId == R.id.first) {
                    frag = new RecordFragment();
                } else if (itemId == R.id.second) {
                    frag = new FAQFragment();
                }
                // display a toast message with menu item's title
                Toast.makeText(getApplicationContext(), menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                if (frag != null) {
                    changeFragment(frag);
                    return true;
                }
                return false;
            }
        });
    }
}