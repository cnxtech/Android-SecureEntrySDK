package com.ticketmaster.presence.secure_entry_demo;
/*
    Copyright 2019 Ticketmaster

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ticketmaster.presence.secure_entry_demo.fragment.TicketsFragment;
import com.ticketmaster.presence.secure_entry_demo.fragment.RotatingSecureEntryFragment;
import com.ticketmaster.presence.secure_entry_demo.fragment.StaticSecureEntryFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabs = findViewById(R.id.tabs);
        ViewPager viewPager = findViewById(R.id.viewPager);
        tabs.setupWithViewPager(viewPager);

        SimplePagerAdapter adapter = new SimplePagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);
    }


    private class SimplePagerAdapter extends FragmentPagerAdapter {

        final String[] titles;

        SimplePagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            titles = context.getResources().getStringArray(R.array.titles);
        }


        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return RotatingSecureEntryFragment.newInstance();
            } else if (i == 1) {
                return StaticSecureEntryFragment.newInstance();
            } else if(i == 2){
                return TicketsFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
