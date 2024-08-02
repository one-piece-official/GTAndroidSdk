package com.windmill.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InitSettingActivity extends AppCompatActivity {

    {
        map.put("Mtg", 1);
        map.put("Vungle", 4);
        map.put("AppLovin", 5);
        map.put("Unity", 6);
        map.put("Ironsource", 7);
        map.put("Sigmob", 9);
        map.put("Admob", 11);
        map.put("Csj", 13);
        map.put("Gdt", 16);
        map.put("KuaiShou", 19);
        map.put("Klevin", 20);
        map.put("BaiDu", 21);
        map.put("Gromore", 22);
        map.put("Oppo", 23);
        map.put("Vivo", 24);
        map.put("HuaWei", 25);
        map.put("MiMo", 26);
        map.put("AdScope", 27);
        map.put("QuMeng", 28);
        map.put("TapTap", 29);
        map.put("Pangle", 30);
        map.put("ApplovinMax", 31);
        map.put("Reklamup", 33);
        map.put("Admate", 35);
    }

    private Spinner spinner;
    private ArrayAdapter<String> arrayAdapter;
    private static Map<String, Integer> map = new HashMap();
    private String curName;
    private int curId;
    private EditText editAppId;
    private EditText editAppKey;
    private JSONObject jsonObject;
    private LinearLayout adnGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_setting);
        spinner = findViewById(R.id.id_spinner);
        editAppId = findViewById(R.id.editAppId);
        editAppKey = findViewById(R.id.editAppKey);
        adnGroup = findViewById(R.id.adn_container);

        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

        String string = sharedPreferences.getString(Constants.INIT_SETTING, "");
        if (!TextUtils.isEmpty(string)) {
            try {
                jsonObject = new JSONObject(string);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            jsonObject = new JSONObject();
        }

        createView();

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, map.keySet().toArray());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                curName = (String) map.keySet().toArray()[position];
                curId = (int) map.values().toArray()[position];
                Log.d("lance", "------onItemSelected------" + position + ":" + curName + ":" + curId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void ButtonClick(View view) {
        if (view.getId() == R.id.bt_commit) {
            try {
                JSONObject object = new JSONObject();
                if (editAppId.getText().length() <= 0 && editAppKey.getText().length() <= 0) {//过滤渠道
                    object.put("curName", new String(curName));
                    object.put("curId", new Integer(curId));
                    jsonObject.put(String.valueOf(curId), object);
                    Toast.makeText(this, "appId or appKey must not be null", Toast.LENGTH_SHORT).show();
                } else {
                    object.put("appId", new String(editAppId.getText().toString()));
                    object.put("appKey", new String(editAppKey.getText().toString()));
                    object.put("curName", new String(curName));
                    object.put("curId", new Integer(curId));
                    jsonObject.put(String.valueOf(curId), object);
                    Log.d("lance", "------ButtonClick----提交----" + jsonObject.toString());
                }
                SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.INIT_SETTING, jsonObject.toString());
                editor.commit();

                createView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (view.getId() == R.id.bt_remove) {
            if (jsonObject != null) {
                jsonObject.remove(String.valueOf(curId));
                Log.d("lance", "------ButtonClick----清除----" + jsonObject.toString());
                SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.INIT_SETTING, jsonObject.toString());
                editor.commit();

                createView();
            }
        }
    }

    private void createView() {
        if (jsonObject != null) {
            adnGroup.removeAllViews();
            Iterator<String> it = jsonObject.keys();
            while (it.hasNext()) {
                try {
                    String key = it.next();
                    JSONObject object = jsonObject.getJSONObject(key);
                    View view = new View(this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                    layoutParams.topMargin = 10;
                    layoutParams.bottomMargin = 10;
                    view.setLayoutParams(layoutParams);
                    view.setBackgroundColor(Color.BLACK);
                    adnGroup.addView(view);

                    TextView textView = new TextView(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    textView.setLayoutParams(params);
                    StringBuilder builder = new StringBuilder();
                    builder.append("渠道：");
                    builder.append(object.optString("curName"));
                    builder.append("\n");
                    builder.append("渠道ID：");
                    builder.append(object.optString("curId"));
                    builder.append("\n");
                    builder.append("AppId：");
                    builder.append(object.optString("appId"));
                    builder.append("\n");
                    builder.append("AppKey：");
                    builder.append(object.optString("appKey"));
                    textView.setLineSpacing(5F, 1);
                    textView.setText(builder.toString());
                    adnGroup.addView(textView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}