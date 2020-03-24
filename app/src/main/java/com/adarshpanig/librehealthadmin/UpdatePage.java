package com.adarshpanig.librehealthadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class UpdatePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_page);
        SearchView searchView = findViewById(R.id.search_bar2);
        searchView.setFocusable(false);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Intent i = new Intent(UpdatePage.this,SearchHospital.class);
                    startActivity(i);
                } else {

                }
            }
        });
    }
}
