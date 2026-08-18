package com.vishnu.ezfit.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vishnu.ezfit.R;
import com.vishnu.ezfit.database.WorkoutSplitDbHelper;
import com.vishnu.ezfit.fragments.HomeFragment;
import com.vishnu.ezfit.fragments.ProgressFragment;
import com.vishnu.ezfit.fragments.WorkoutFragment;

public class MainActivity extends AppCompatActivity {
    private WorkoutSplitDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new WorkoutSplitDbHelper(this);
        setupNavigation();
        loadDefaultFragment();
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            }  else if (item.getItemId() == R.id.nav_progress) {
                loadFragment(new ProgressFragment());
                return true;
            }
            return false;
        });
    }

    private void loadDefaultFragment() {
        loadFragment(new HomeFragment());
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}