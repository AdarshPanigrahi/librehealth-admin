package com.adarshpanig.librehealthadmin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class DashBoard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
    }

    public void GotoSettings(View view) {
    }

    public void NotifyUsers(View view) {
    }

    public void Gotodatabase(View view) {
    }

    public void GotoUpdate(View view) {
        Intent intent = new Intent(DashBoard.this, UpdatePage.class);
        startActivity(intent);
    }
}
