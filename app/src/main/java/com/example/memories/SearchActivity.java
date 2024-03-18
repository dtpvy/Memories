package com.example.memories;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

public class SearchActivity extends AppCompatActivity {

    EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Ánh xạ EditText từ layout
        searchEditText = findViewById(R.id.searchEditText);

        // Thêm sự kiện TextChangedListener để lắng nghe thay đổi trong EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần thực hiện gì trước khi text thay đổi
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Thực hiện tìm kiếm và hiển thị kết quả tại đây
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần thực hiện gì sau khi text thay đổi
            }
        });
    }

    // Hàm thực hiện tìm kiếm và hiển thị kết quả
    private void performSearch(String query) {
        // Hiển thị kết quả tìm kiếm tại đây, có thể sử dụng RecyclerView hoặc ListView để hiển thị danh sách kết quả
        Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
    }
}
