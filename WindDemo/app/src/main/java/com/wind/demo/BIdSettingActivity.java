package com.wind.demo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.sigmob.windad.WindAdBiddingLossReason;

import java.util.Arrays;
import java.util.List;


public class BIdSettingActivity extends Activity {
    private EditText editText1, editText2, editText3;
    private Spinner spinner1, spinner2, spinner3;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bid_setting);

        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);
        editor = sharedPreferences.edit();

        editText1 = findViewById(R.id.edit_dijia);
        editText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable edit) {
                //edit  输入结束呈现在输入框中的信息
                Log.d("lance", "------editText1------" + editText1.getText().toString());
                if (!TextUtils.isEmpty(editText1.getText().toString())) {
                    editor.putString(Constants.BID_DJ, editText1.getText().toString());
                    editor.apply();
                }
            }
        });
        editText2 = findViewById(R.id.edit_jsjg);
        editText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable edit) {
                //edit  输入结束呈现在输入框中的信息
                Log.d("lance", "------editText2------" + editText2.getText().toString());
                if (!TextUtils.isEmpty(editText2.getText().toString())) {
                    editor.putString(Constants.BID_JS, editText2.getText().toString());
                    editor.apply();
                }
            }
        });
        editText3 = findViewById(R.id.edit_cgj);
        editText3.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable edit) {
                //edit  输入结束呈现在输入框中的信息
                Log.d("lance", "------editText3------" + editText3.getText().toString());
                if (!TextUtils.isEmpty(editText3.getText().toString())) {
                    editor.putString(Constants.BID_CGJ, editText3.getText().toString());
                    editor.apply();
                }
            }
        });

        String BID_DJ = sharedPreferences.getString(Constants.BID_DJ, "-1");
        String AUCTION_PRICE = sharedPreferences.getString(Constants.BID_JS, "-1");
        String HIGHEST_LOSS_PRICE = sharedPreferences.getString(Constants.BID_CGJ, "-1");

        if (!BID_DJ.equals("-1")) {
            editText1.setText(BID_DJ);
        }
        if (!AUCTION_PRICE.equals("-1")) {
            editText2.setText(AUCTION_PRICE);
        }
        if (!HIGHEST_LOSS_PRICE.equals("-1")) {
            editText3.setText(HIGHEST_LOSS_PRICE);
        }

        spinner1 = findViewById(R.id.id_spinner_bz);
        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.bz_adapter));
        arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(arrayAdapter1);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String[] stringArray = getResources().getStringArray(R.array.bz_adapter);
                String bz = stringArray[position];
                Log.d("lance", "------onItemSelected------" + position + ":" + bz);
                editor.putString(Constants.BID_BZ, bz);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        spinner2 = findViewById(R.id.id_spinner_sbyy);
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.sb_adapter));
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(arrayAdapter2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String bz = null;
                switch (position) {
                    case 0:
                        bz = String.valueOf(WindAdBiddingLossReason.LOSS_REASON_LOW_PRICE.getCode());
                        break;
                    case 1:
                        bz = String.valueOf(WindAdBiddingLossReason.LOSS_REASON_RETURN_TIMEOUT.getCode());
                        break;
                    case 2:
                        bz = String.valueOf(WindAdBiddingLossReason.LOSS_REASON_RETURN_ERROR.getCode());
                        break;
                }
                Log.d("lance", "------onItemSelected------" + position + ":" + bz);
                editor.putString(Constants.BID_SB, bz);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        spinner3 = findViewById(R.id.id_spinner_jjscf);
        ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.part_adapter));
        arrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(arrayAdapter3);
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String[] stringArray = getResources().getStringArray(R.array.part_id_value);
                String bz = stringArray[position];
                Log.d("lance", "------onItemSelected------" + position + ":" + bz);
                editor.putString(Constants.BID_JSF, bz);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        String CURRENCY = sharedPreferences.getString(Constants.BID_BZ, "CNY");
        String LOSS_REASON = sharedPreferences.getString(Constants.BID_SB, "-1");
        String ADN_ID = sharedPreferences.getString(Constants.BID_JSF, "-1");

        if (!TextUtils.isEmpty(CURRENCY)) {
            String[] stringArray = getResources().getStringArray(R.array.bz_adapter);
            List<String> strings = Arrays.asList(stringArray);
            spinner1.setSelection(strings.indexOf(CURRENCY), true);
        }

        if (!LOSS_REASON.equals("-1")) {
            String[] stringArray = getResources().getStringArray(R.array.sb_id_value);
            List<String> strings = Arrays.asList(stringArray);
            spinner2.setSelection(strings.indexOf(LOSS_REASON), true);
        }

        if (!ADN_ID.equals("-1")) {
            String[] stringArray = getResources().getStringArray(R.array.part_id_value);
            List<String> strings = Arrays.asList(stringArray);
            spinner3.setSelection(strings.indexOf(ADN_ID), true);
        }

    }
}