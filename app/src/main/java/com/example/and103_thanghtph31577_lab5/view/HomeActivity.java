package com.example.and103_thanghtph31577_lab5.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.and103_thanghtph31577_lab5.adapter.FruitAdapter;
import com.example.and103_thanghtph31577_lab5.databinding.ActivityHomeBinding;
import com.example.and103_thanghtph31577_lab5.model.Fruit;
import com.example.and103_thanghtph31577_lab5.model.Response;
import com.example.and103_thanghtph31577_lab5.services.HttpRequest;

import java.util.ArrayList;

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

        // Khởi tạo adapter
        fruitAdapter = new FruitAdapter(this, list, this);
        binding.rcvFruit.setAdapter(fruitAdapter);

        userListener();
        loadFruitData();
    }

    private void userListener() {
        binding.btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddFruitActivity.class);
            startActivity(intent);
        });

        binding.btnLoc.setOnClickListener(v -> {
            list.clear();
            loadFruitData();
        });
    }

    private void loadFruitData() {
        httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(new Callback<Response<ArrayList<Fruit>>>() {
            @Override
            public void onResponse(Call<Response<ArrayList<Fruit>>> call, retrofit2.Response<Response<ArrayList<Fruit>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 200) {
                    list.clear();
                    list.addAll(response.body().getData());
                    fruitAdapter.notifyDataSetChanged();
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

    Callback<Response<Fruit>> responseFruitAPI = new Callback<Response<Fruit>>() {
        @Override
        public void onResponse(Call<Response<Fruit>> call, retrofit2.Response<Response<Fruit>> response) {
            if (response.isSuccessful() && response.body().getStatus() == 200) {
                Toast.makeText(HomeActivity.this, response.body().getMessenger(), Toast.LENGTH_SHORT).show();
                loadFruitData(); // refresh lại danh sách
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
        list.clear();
        loadFruitData();
    }
}
