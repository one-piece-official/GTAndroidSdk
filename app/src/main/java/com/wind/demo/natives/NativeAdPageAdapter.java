package com.wind.demo.natives;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class NativeAdPageAdapter extends FragmentStateAdapter {
    

    public NativeAdPageAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: {
                return new NativeAdUnifiedFragment();
            }
            case 1: {
                return new MyFragment();
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
