package com.wind.demo.natives;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;

import com.wind.demo.R;


public class NativeAdActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native);
        bindButton(R.id.native_ad_button, NativeAdUnifiedActivity.class);
        bindButton(R.id.bid_native_ad_button, BidNativeAdUnifiedActivity.class);
        bindButton(R.id.native_ad_list_button, NativeAdUnifiedListActivity.class);
        bindButton(R.id.bid_native_ad_list_button, NativeAdUnifiedListActivity.class);
        bindButton(R.id.native_ad_recycle_button, NativeAdUnifiedRecycleActivity.class);
    }

    private void bindButton(@IdRes final int id, final Class clz) {
        this.findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NativeAdActivity.this, clz);

                if (R.id.bid_native_ad_list_button == id){
                    intent.putExtra("bidType",1);
                }

                startActivity(intent);


            }
        });
    }
}