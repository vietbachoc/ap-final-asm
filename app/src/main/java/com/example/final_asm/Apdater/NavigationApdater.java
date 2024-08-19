package com.example.final_asm.Apdater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.final_asm.fragments.BudgetFragment;
import com.example.final_asm.DAO.DBManager;
import com.example.final_asm.R;
import com.example.final_asm.fragments.HomeFragment;
import com.example.final_asm.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
public class NavigationApdater extends AppCompatActivity {
    private DBManager DB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_manager);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        DB = new DBManager(this);
        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    if (item.getItemId() == R.id.nav_home) {
                        selectedFragment = new HomeFragment();
                    } else if (item.getItemId() == R.id.nav_budget) {
                        selectedFragment = new BudgetFragment();
                    } else if (item.getItemId() == R.id.nav_profile) {
                        selectedFragment = new ProfileFragment();
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    return true;
                }
            };


}


