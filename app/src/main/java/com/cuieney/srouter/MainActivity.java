package com.cuieney.srouter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cuieney.srouter_annotation.annotation.Route;
import com.cuieney.srouter_api.launcher.SRouter;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SRouter.openDebug();
        SRouter.openLog();
        SRouter.init(getApplication());
        findViewById(R.id.txt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SRouter.getInstance().build("/moduleHome/Main").withString("id","1234").navigation();
            }
        });
    }
}
