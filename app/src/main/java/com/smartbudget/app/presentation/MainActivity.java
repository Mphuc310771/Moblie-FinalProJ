package com.smartbudget.app.presentation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.smartbudget.app.R;
import com.smartbudget.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupFab();

        setupFab();

        // Seed database with sample data if empty, but wait for Auth to be ready
        com.google.firebase.auth.FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null) {
                com.smartbudget.app.data.local.DatabaseSeeder.seed(MainActivity.this);
            }
        });
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
            
            // Custom listener to ensure Dashboard navigation works from any screen
            binding.bottomNavigation.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.dashboardFragment) {
                    // Force pop to start destination (Overview) if we are deep in stack
                    // This fixes issue where navigating from secondary screens (like Savings) failed
                    boolean popped = navController.popBackStack(R.id.dashboardFragment, false);
                    if (!popped) {
                        navController.navigate(R.id.dashboardFragment);
                    }
                    return true;
                }
                // Default behavior for other items
                return NavigationUI.onNavDestinationSelected(item, navController);
            });
        }
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            if (navController != null) {
                navController.navigate(R.id.addExpenseFragment);
            }
        });

        // Hide FAB when on add expense screen
        if (navController != null) {
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.addExpenseFragment) {
                    binding.fabAdd.hide();
                } else {
                    binding.fabAdd.show();
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
