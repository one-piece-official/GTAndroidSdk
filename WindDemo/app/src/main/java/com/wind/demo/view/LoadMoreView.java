package com.wind.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wind.demo.R;


/**
 * Create by hanweiwei on 11/07/2018
 */
@SuppressWarnings("SpellCheckingInspection")
public class LoadMoreView extends FrameLayout {
    private ProgressBar mProgressBar;
    private TextView mTextView;

    public LoadMoreView(Context context) {
        super(context);
        init(context);
    }

    public LoadMoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressWarnings("RedundantCast")
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.load_more_view, this, true);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_load_more_progress);
        mTextView = (TextView) findViewById(R.id.tv_load_more_tip);
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public TextView getTextView() {
        return mTextView;
    }

    @SuppressWarnings("unused")
    public void setText(String tip) {
        if (mTextView != null) {
            mTextView.setText(String.valueOf(tip));
        }
    }
}
