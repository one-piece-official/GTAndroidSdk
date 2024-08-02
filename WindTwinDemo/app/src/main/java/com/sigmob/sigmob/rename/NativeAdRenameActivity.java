package com.sigmob.sigmob.rename;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.sigmob.sigmob.R;

public class NativeAdRenameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_rename);
        bindButton(R.id.unified_native_ad_button, NativeAdUnifiedRenameActivity.class);
        bindButton(R.id.unified_native_ad_list_button, NativeAdUnifiedListRenameActivity.class);
        bindButton(R.id.unified_native_ad_recycle_button, NativeAdUnifiedRecycleRenameActivity.class);
    }

    private void bindButton(@IdRes int id, final Class clz) {
        this.findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NativeAdRenameActivity.this, clz);
                startActivity(intent);
            }
        });
    }
}