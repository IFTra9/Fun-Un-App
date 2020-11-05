package com.example.funun.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.funun.Fragment.AboutFragment;
import com.example.funun.Fragment.AllEventsFragment;
import com.example.funun.Fragment.EventDetailsFragment;
import com.example.funun.Fragment.ProfileFragment;
import com.example.funun.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**This Class HomeActivity - initialize a bottom navigation bar menu for application as follows:

 * */

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private Fragment active;

    final FragmentManager fm = getSupportFragmentManager();
    final Fragment fragment1 = new ProfileFragment();
    final Fragment fragment2 = new AllEventsFragment();
    final Fragment fragment3 = new EventDetailsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }
        active = fragment1;
        fm.beginTransaction().add(R.id.home_frame, fragment3, "3").hide(fragment3).commit();
        fm.beginTransaction().add(R.id.home_frame, fragment2, "2").hide(fragment2).commit();
        fm.beginTransaction().add(R.id.home_frame,fragment1, "1").commit();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(selectedListener);
    }

    //listener for handling selection events on bottom navigation items.
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {

                        case R.id.action_all_events:

                            fm.beginTransaction().hide(active).show(fragment2).commit();
                            active=fragment2;
                            fm.popBackStack();
                            if(getSharedPreferences("Event",MODE_PRIVATE).getAll().size()!=0)
                                getSharedPreferences("Event",MODE_PRIVATE).edit().clear().commit();
                            return true;

                        case R.id.action_event_details:

                            if(getSharedPreferences("Event",MODE_PRIVATE).getAll().size()!=0) {
                                fm.beginTransaction().hide(active).show(fragment3).commit();
                                active = fragment3;
                            } else {
                                fm.beginTransaction().hide(active).show(fragment3).commit();
                                active = fragment3;
                                fm.popBackStack();
                            }
                            return true;

                        case R.id.action_profile:
                            fm.beginTransaction().hide(active).show(fragment1).commit();
                            active = fragment1;
                            fm.popBackStack();
                            if(getSharedPreferences("Event",MODE_PRIVATE).getAll().size()!=0)
                                getSharedPreferences("Event",MODE_PRIVATE).edit().clear().commit();
                            return true;
                    }
                    return false;
                }
            };


/**This Method onBackPressed - called only when the user taped on the back key in his app. */
    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle("Exit Pop-Up")
                .setMessage("Are You Sure You Want To Leave?")
                .setIcon(R.drawable.ic_exit_back)
                .setPositiveButton("Stay",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(HomeActivity.this, "Lets Plan Funun Events", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("Leave And Sign Out", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        //Finish this activity as well as all activities immediately
                        finishAffinity();
                        startActivity(intent);
                        finish();
                    }
                }).create().show();
    }
}