package com.gospelware.liquildbutton.Example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.gospelware.liquidbutton.LiquidButton;
import com.gospelware.liquildbutton.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LiquidButton liquidCheckView=(LiquidButton)findViewById(R.id.button);

        if(liquidCheckView!=null){

            liquidCheckView.setFillAfter(true);

            liquidCheckView.setPourFinishListener(new LiquidButton.PourFinishListener() {
                @Override
                public void onPourFinish() {
                    Toast.makeText(MainActivity.this, "Finish", Toast.LENGTH_SHORT).show();
                }
            });
            liquidCheckView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LiquidButton btn=(LiquidButton)v;
                    btn.startPour();
                }
            });
        }

    }
}
