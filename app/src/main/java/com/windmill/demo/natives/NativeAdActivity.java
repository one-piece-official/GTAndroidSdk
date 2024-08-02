package com.windmill.demo.natives;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.windmill.demo.R;


public class NativeAdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native);
        bindButton(R.id.native_ad_button, NativeAdUnifiedActivity.class);
        bindButton(R.id.native_ad_list_button, NativeAdUnifiedListActivity.class);
        bindButton(R.id.native_ad_recycle_button, NativeAdUnifiedRecycleActivity.class);
        bindButton(R.id.native_draw_recycle_button, NativeAdDrawActivity.class);
    }

    private void bindButton(@IdRes int id, final Class clz) {
        this.findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NativeAdActivity.this, clz);
                startActivity(intent);
            }
        });
    }
}