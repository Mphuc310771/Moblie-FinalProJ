package com.smartbudget.app.presentation.addexpense;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.smartbudget.app.R;
import com.smartbudget.app.databinding.FragmentAddExpenseBinding;
import com.smartbudget.app.presentation.adapter.CategoryAdapter;
import com.smartbudget.app.presentation.scan.ScanReceiptActivity;
import com.smartbudget.app.utils.CurrencyUtils;
import com.smartbudget.app.utils.DateUtils;

import java.util.Calendar;

public class AddExpenseFragment extends Fragment {

    private FragmentAddExpenseBinding binding;
    private AddExpenseViewModel viewModel;
    private CategoryAdapter categoryAdapter;

    private long expenseIdToEdit = -1;

    // Launcher for scan receipt result
    private final ActivityResultLauncher<Intent> scanReceiptLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    double amount = data.getDoubleExtra("amount", 0);
                    String merchant = data.getStringExtra("merchant");
                    String note = data.getStringExtra("note");
                    long date = data.getLongExtra("date", 0);
                    
                    if (amount > 0) {
                        binding.etAmount.setText(String.valueOf((long) amount));
                    }
                    
                    // Use note (items list) if available, otherwise merchant
                    if (note != null && !note.isEmpty()) {
                        binding.etNote.setText(note);
                    } else if (merchant != null && !merchant.isEmpty()) {
                        binding.etNote.setText(merchant);
                    }
                    
                    if (date > 0) {
                        viewModel.setSelectedDate(date);
                    }
                    
                    Toast.makeText(requireContext(), "Đã nhập từ hóa đơn!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            expenseIdToEdit = getArguments().getLong("expenseId", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAddExpenseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AddExpenseViewModel.class);

        setupViews();
        setupCategoryRecyclerView();
        setupListeners();
        observeData();

        // Load expense for editing
        if (expenseIdToEdit > 0) {
            binding.tvTitle.setText(R.string.edit);
            viewModel.loadExpenseForEdit(expenseIdToEdit);
        } else {
            // Check for scan results
            if (getArguments() != null) {
                double initialAmount = getArguments().getDouble("initialAmount", 0);
                String initialNote = getArguments().getString("initialNote");
                long initialDate = getArguments().getLong("initialDate", 0);
                
                if (initialAmount > 0) {
                    binding.etAmount.setText(String.valueOf((long) initialAmount));
                }
                if (initialNote != null) {
                    binding.etNote.setText(initialNote);
                }
                if (initialDate > 0) {
                    viewModel.setSelectedDate(initialDate);
                }
            }
        }
    }

    private void setupViews() {
        // Set default date
        binding.etDate.setText(DateUtils.formatDate(System.currentTimeMillis()));

        // Toggle group - default to expense
        binding.toggleType.check(R.id.btn_expense);
    }

    private void setupCategoryRecyclerView() {
        categoryAdapter = new CategoryAdapter();
        categoryAdapter.setOnCategorySelectedListener(category -> {
            viewModel.setSelectedCategoryId(category.getId());
        });

        binding.rvCategories.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        // Toggle expense/income
        binding.toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                viewModel.setIsExpenseType(checkedId == R.id.btn_expense);
            }
        });

        // Date picker
        binding.etDate.setOnClickListener(v -> showDatePicker());

        // Scan receipt - NOW WORKING!
        binding.btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ScanReceiptActivity.class);
            scanReceiptLauncher.launch(intent);
        });

        // Save button
        binding.btnSave.setOnClickListener(v -> saveExpense());
    }

    private void observeData() {
        // Observe categories - uses switchMap in ViewModel, auto-switches based on type
        // NO NESTED OBSERVERS - single observation, automatic updates
        viewModel.getCurrentCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryAdapter.submitList(categories);
            }
        });

        // Observe selected date
        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                binding.etDate.setText(DateUtils.formatDate(date));
            }
        });

        // Observe save success
        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), R.string.save_success, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        // Observe error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        Long selectedDate = viewModel.getSelectedDate().getValue();
        if (selectedDate != null) {
            calendar.setTimeInMillis(selectedDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    viewModel.setSelectedDate(selected.getTimeInMillis());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void saveExpense() {
        String amountStr = binding.etAmount.getText().toString().trim();
        String note = binding.etNote.getText().toString().trim();

        double amount = CurrencyUtils.parseAmount(amountStr);
        viewModel.saveExpense(amount, note);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
