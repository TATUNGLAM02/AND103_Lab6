package com.example.and103_thanghtph31577_lab5.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.and103_thanghtph31577_lab5.adapter.FruitAdapter;
import com.example.and103_thanghtph31577_lab5.databinding.ActivityHomeBinding;
import com.example.and103_thanghtph31577_lab5.model.Fruit;
import com.example.and103_thanghtph31577_lab5.model.Response;
import com.example.and103_thanghtph31577_lab5.services.HttpRequest;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;

public class HomeActivity extends AppCompatActivity implements FruitAdapter.FruitClick {

    ActivityHomeBinding binding;
    private HttpRequest httpRequest;
    private SharedPreferences sharedPreferences;
    private String token;
    private FruitAdapter fruitAdapter;
    private ArrayList<Fruit> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("INFO", MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");
        httpRequest = new HttpRequest(token);

        fruitAdapter = new FruitAdapter(this, list, this);
        binding.rcvFruit.setAdapter(fruitAdapter);

        // Spinner sort
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Giá tăng dần", "Giá giảm dần"});
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSort.setAdapter(sortAdapter);

        setListeners();
        loadFruitData();
    }

    private void setListeners() {
        binding.btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddFruitActivity.class);
            startActivity(intent);
        });

        binding.btnLoc.setOnClickListener(v -> filterFruits());
    }

    private void loadFruitData() {
        httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(new Callback<Response<ArrayList<Fruit>>>() {
            @Override
            public void onResponse(Call<Response<ArrayList<Fruit>>> call, retrofit2.Response<Response<ArrayList<Fruit>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 200) {
                    list.clear();
                    list.addAll(response.body().getData());
                    fruitAdapter.setData(new ArrayList<>(list));
                } else {
                    Toast.makeText(HomeActivity.this, "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<ArrayList<Fruit>>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi: " + t.getMessage());
            }
        });
    }

    private void filterFruits() {
        String name = binding.edSearchName.getText().toString().trim();
        String priceStr = binding.edSearchMoney.getText().toString().trim();
        int priceFilter = priceStr.isEmpty() ? 0 : Integer.parseInt(priceStr);
        String sort = binding.spinnerSort.getSelectedItem().toString();

        ArrayList<Fruit> filteredList = new ArrayList<>();
        for (Fruit fruit : list) {
            boolean matchName = name.isEmpty() || fruit.getName().toLowerCase().contains(name.toLowerCase());

            int price = 0;
            try {
                price = Integer.parseInt(fruit.getPrice()); // chuyển từ String sang int
            } catch (Exception e) {
                continue;
            }

            boolean matchPrice = price >= priceFilter;

            if (matchName && matchPrice) {
                filteredList.add(fruit);
            }
        }

        if (sort.equals("Giá tăng dần")) {
            Collections.sort(filteredList, (f1, f2) -> {
                int p1 = Integer.parseInt(f1.getPrice());
                int p2 = Integer.parseInt(f2.getPrice());
                return Integer.compare(p1, p2);
            });
        } else {
            Collections.sort(filteredList, (f1, f2) -> {
                int p1 = Integer.parseInt(f1.getPrice());
                int p2 = Integer.parseInt(f2.getPrice());
                return Integer.compare(p2, p1);
            });
        }

        fruitAdapter.setData(filteredList);
    }

    Callback<Response<Fruit>> responseFruitAPI = new Callback<Response<Fruit>>() {
        @Override
        public void onResponse(Call<Response<Fruit>> call, retrofit2.Response<Response<Fruit>> response) {
            if (response.isSuccessful() && response.body().getStatus() == 200) {
                Toast.makeText(HomeActivity.this, response.body().getMessenger(), Toast.LENGTH_SHORT).show();
                loadFruitData();
            }
        }

        @Override
        public void onFailure(Call<Response<Fruit>> call, Throwable t) {
            Log.e("DELETE_FRUIT", "onFailure: " + t.getMessage());
        }
    };

    @Override
    public void delete(Fruit fruit) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    httpRequest.callAPI().deleteFruits(fruit.get_id()).enqueue(responseFruitAPI);
                })
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void edit(Fruit fruit) {
        Intent intent = new Intent(HomeActivity.this, UpdateFruitActivity.class);
        intent.putExtra("fruit", fruit);
        startActivity(intent);
    }

    @Override
    public void showDetail(Fruit fruit) {
        Intent intent = new Intent(HomeActivity.this, FruitDetailActivity.class);
        intent.putExtra("fruit", fruit);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFruitData();
    }
}
