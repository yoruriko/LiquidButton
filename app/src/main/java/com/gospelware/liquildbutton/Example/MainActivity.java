package com.gospelware.liquildbutton.Example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.gospelware.liquidbutton.LiquidButton;
import com.gospelware.liquildbutton.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private float progress;


    @Bind(R.id.liquid_button)
    LiquidButton liquidButton;
    @Bind(R.id.progress_tv)
    TextView textView;

    @Bind(R.id.fillAfter_switch)
    Switch fillAfterSwitch;

    @Bind(R.id.autoPlay_switch)
    Switch autoPlaySwitch;

    @OnClick(R.id.updateProgress_btn)
    void onUpdateProgress() {
        progress += 0.1f;
        liquidButton.changeProgress(progress);
    }

    @OnClick(R.id.finish_btn)
    void onFinish() {
        progress = 0;
        liquidButton.finishPour();
    }

    @OnClick(R.id.liquid_button)
    void onBtnStart() {
        liquidButton.startPour();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        fillAfterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                liquidButton.setFillAfter(isChecked);
            }
        });

        autoPlaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                liquidButton.setAutoPlay(isChecked);
            }
        });

        liquidButton.setPourFinishListener(new LiquidButton.PourFinishListener() {
            @Override
            public void onPourFinish() {
                Toast.makeText(MainActivity.this, "Finish", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgressUpdate(float progress) {
                textView.setText(String.format("%.2f", progress * 100) + "%");
            }
        });

    }

}
