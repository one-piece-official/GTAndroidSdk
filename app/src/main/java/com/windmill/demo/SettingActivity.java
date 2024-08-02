package com.windmill.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.windmill.demo.utils.PxUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SettingActivity extends AppCompatActivity {

    private LinearLayout groupLayout;
    private LinearLayout addLayout;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        groupLayout = findViewById(R.id.group_layout);
        addLayout = findViewById(R.id.add_group);
        sharedPreferences = this.getSharedPreferences("setting", 0);
        editor = sharedPreferences.edit();
        Set<String> groupList = sharedPreferences.getStringSet(Constants.CONF_GROUP, null);
        if (groupList != null) {
            Log.d("lance", "------groupList------" + groupList.toString());
            Iterator<String> iterator = groupList.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (!TextUtils.isEmpty(next)) {
                    createLayout(next);
                }
            }
        }

        addLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createLayout("");
            }
        });
    }

    private void createLayout(String text) {

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        groupLayout.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PxUtils.dpToPx(this, 40)));

        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.mipmap.jianhao);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(PxUtils.dpToPx(this, 20), PxUtils.dpToPx(this, 20));
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        imageView.setLayoutParams(layoutParams);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupLayout.removeView(linearLayout);
            }
        });

        linearLayout.addView(imageView);

        EditText editText = new EditText(this);
        if (!TextUtils.isEmpty(text)) {
            editText.setText(text);
        }
        editText.setHint("key-value");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(PxUtils.dpToPx(this, 5), 0, 0, 0);
        editText.setLayoutParams(params);

        linearLayout.addView(editText);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Set<String> groupList = new HashSet<>();

        for (int i = 0; i < groupLayout.getChildCount(); i++) {
            ViewGroup childAt = (ViewGroup) groupLayout.getChildAt(i);
            if (childAt != addLayout) {
                EditText editText = (EditText) childAt.getChildAt(1);
                groupList.add(editText.getText().toString());
            }
        }
        Log.d("lance", "------onKeyDown------" + groupList.toString());

        editor.putStringSet(Constants.CONF_GROUP, groupList);
        editor.apply();
        return super.onKeyDown(keyCode, event);
    }
}