package com.cuieney.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.cuieney.srouter_annotation.annotation.Route;

@Route(path = "/moduleHome/Main")
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Bundle extras = getIntent().getExtras();
        String id = extras.getString("id");
        ((TextView) findViewById(R.id.txt)).setText(id);
    }
}
