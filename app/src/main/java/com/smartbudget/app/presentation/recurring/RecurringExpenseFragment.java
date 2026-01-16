package com.smartbudget.app.presentation.recurring;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartbudget.app.R;
import com.smartbudget.app.data.local.AppDatabase;
import com.smartbudget.app.data.local.entity.RecurringExpenseEntity;
import com.smartbudget.app.databinding.FragmentRecurringExpenseBinding;
import com.smartbudget.app.utils.CurrencyUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class RecurringExpenseFragment extends Fragment {

    private FragmentRecurringExpenseBinding binding;
    private AppDatabase database;
    private RecurringExpenseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentRecurringExpenseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        database = AppDatabase.getInstance(requireContext());
        
        setupRecyclerView();
        setupListeners();
        loadData();
    }

    private void setupRecyclerView() {
        adapter = new RecurringExpenseAdapter();
        adapter.setOnItemClickListener(this::showEditDialog);
        adapter.setOnDeleteClickListener(this::confirmDelete);
        
        binding.rvRecurring.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecurring.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.fabAddRecurring.setOnClickListener(v -> showAddDialog());
    }

    private void loadData() {
        database.recurringExpenseDao().getAllRecurring().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                binding.rvRecurring.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.rvRecurring.setVisibility(View.VISIBLE);
                binding.layoutEmpty.setVisibility(View.GONE);
                adapter.submitList(list);
            }
            
            updateSummary(list);
        });
        
        database.recurringExpenseDao().getMonthlyRecurringTotal().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                binding.tvMonthlyTotal.setText(formatter.format(total) + " ₫");
            }
        });
        
        database.recurringExpenseDao().getActiveCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvActiveCount.setText(count + " khoản đang hoạt động");
        });
    }

    private void updateSummary(List<RecurringExpenseEntity> list) {
        double total = 0;
        int activeCount = 0;
        
        if (list != null) {
            for (RecurringExpenseEntity item : list) {
                if (item.isActive()) {
                    total += item.getAmount();
                    activeCount++;
                }
            }
        }
        
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        binding.tvMonthlyTotal.setText(formatter.format(total) + " ₫");
        binding.tvActiveCount.setText(activeCount + " khoản đang hoạt động");
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_recurring, null);
        
        EditText etName = dialogView.findViewById(R.id.et_recurring_name);
        EditText etAmount = dialogView.findViewById(R.id.et_recurring_amount);
        EditText etDay = dialogView.findViewById(R.id.et_recurring_day);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("➕ Thêm chi tiêu định kỳ")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String amountStr = etAmount.getText().toString().trim();
                    String dayStr = etDay.getText().toString().trim();
                    
                    if (name.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    double amount = CurrencyUtils.parseAmount(amountStr);
                    int day = dayStr.isEmpty() ? 1 : Integer.parseInt(dayStr);
                    day = Math.max(1, Math.min(28, day)); // Limit to 1-28
                    
                    RecurringExpenseEntity entity = new RecurringExpenseEntity(name, amount, day);
                    
                    // Calculate next due date
                    Calendar nextDue = Calendar.getInstance();
                    nextDue.set(Calendar.DAY_OF_MONTH, day);
                    if (nextDue.before(Calendar.getInstance())) {
                        nextDue.add(Calendar.MONTH, 1);
                    }
                    entity.setNextDueDate(nextDue.getTimeInMillis());
                    
                    Executors.newSingleThreadExecutor().execute(() -> {
                        database.recurringExpenseDao().insert(entity);
                        requireActivity().runOnUiThread(() -> 
                            Toast.makeText(requireContext(), "Đã thêm!", Toast.LENGTH_SHORT).show()
                        );
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditDialog(RecurringExpenseEntity entity) {
        new AlertDialog.Builder(requireContext())
                .setTitle(entity.getName())
                .setMessage(String.format("Số tiền: %,.0f₫\nNgày thu: %d hàng tháng\nTrạng thái: %s",
                        entity.getAmount(),
                        entity.getDayOfMonth(),
                        entity.isActive() ? "Đang hoạt động" : "Tạm dừng"))
                .setPositiveButton(entity.isActive() ? "Tạm dừng" : "Kích hoạt", (d, w) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        database.recurringExpenseDao().setActive(entity.getId(), !entity.isActive());
                    });
                })
                .setNegativeButton("Đóng", null)
                .setNeutralButton("Xóa", (d, w) -> confirmDelete(entity))
                .show();
    }

    private void confirmDelete(RecurringExpenseEntity entity) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa \"" + entity.getName() + "\"?")
                .setPositiveButton("Xóa", (d, w) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        database.recurringExpenseDao().delete(entity);
                        requireActivity().runOnUiThread(() -> 
                            Toast.makeText(requireContext(), "Đã xóa!", Toast.LENGTH_SHORT).show()
                        );
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
