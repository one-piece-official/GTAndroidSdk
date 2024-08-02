package com.windmill.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.windmill.demo.utils.PxUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingIDActivity extends AppCompatActivity {

    private LinearLayout groupLayout;
    private LinearLayout addLayout;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Spinner spinner;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> adSlotIds = new ArrayList<>();
    private String curPlacementId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_id);
        groupLayout = findViewById(R.id.group_layout);
        addLayout = findViewById(R.id.add_group);
        spinner = findViewById(R.id.id_spinner);
        sharedPreferences = this.getSharedPreferences("setting", 0);
        editor = sharedPreferences.edit();

        String configJson = sharedPreferences.getString(Constants.CONF_JSON, "");
        if (!TextUtils.isEmpty(configJson)) {
            try {
                JSONObject jsonObject = new JSONObject(configJson);
                JSONObject dataJson = jsonObject.getJSONObject("data");
                JSONArray array = dataJson.optJSONArray("slotIds");
                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject slotId = array.getJSONObject(i);
                        if (slotId != null) {
                            String adSlotId = slotId.optString("adSlotId");
                            adSlotIds.add(adSlotId);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, adSlotIds);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                curPlacementId = adSlotIds.get(position);
                Log.d("lance", "------onItemSelected------" + position + ":" + curPlacementId);
                if (!TextUtils.isEmpty(curPlacementId)) {
                    groupLayout.removeAllViews();
                    Set<String> groupList = sharedPreferences.getStringSet(curPlacementId, null);
                    if (groupList != null && groupList.size() > 0) {
                        Log.d("lance", "------groupList------" + groupList);
                        Iterator<String> iterator = groupList.iterator();
                        while (iterator.hasNext()) {
                            String next = iterator.next();
                            if (!TextUtils.isEmpty(next)) {
                                createLayout(next);
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("lance", "------onNothingSelected------");
            }
        });

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

        Log.d("lance", curPlacementId + "------onKeyDown------" + groupList);
        if (!TextUtils.isEmpty(curPlacementId) && groupList.size() > 0) {
            editor.putStringSet(curPlacementId, groupList);
            editor.apply();
        }
        return super.onKeyDown(keyCode, event);
    }
}