package com.ticketmaster.presence.secure_entry_demo.fragment;
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import com.ticketmaster.presence.SecureEntryView;
import com.ticketmaster.presence.secure_entry_demo.R;

public class RotatingSecureEntryFragment extends Fragment {

    static final String ROTATING_TOKEN = "eyJiIjoiOTY0NTM3MjgzNDIxIiwidCI6IlRNOjowMzo6MjAxeXRmbmllN2tpZmxzZ2hncHQ5ZDR4N2JudTljaG4zYWNwdzdocjdkOWZzc3MxcyIsImNrIjoiMzRkNmQyNTNiYjNkZTIxOTFlZDkzMGY2MmFkOGQ0ZDM4NGVhZTVmNSJ9";
    static final String BAD_TOKEN = "baddata12318437";


    static final int [] COLORS  = {
            R.color.red,
            R.color.pink,
            R.color.purple,
            R.color.deep_purple,
            R.color.indigo,
            R.color.blue,
            R.color.light_blue,
            R.color.cyan,
            R.color.teal,
            R.color.green,
            R.color.light_green,
            R.color.lime,
            R.color.yellow,
            R.color.amber,
            R.color.orange,
            R.color.deep_orange,
            R.color.brown,
            R.color.grey,
            R.color.blue_grey
    };

    public static RotatingSecureEntryFragment newInstance() {
        return new RotatingSecureEntryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rotating, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SecureEntryView secureEntryView = view.findViewById(R.id.secureEntryView);
        secureEntryView.setToken(ROTATING_TOKEN);

        final String [] colorOptions = getResources().getStringArray(R.array.colors);
        ImageButton buttonColorPicker = view.findViewById(R.id.buttonPickColor);
        buttonColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(v.getContext())
                        .setItems(colorOptions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                secureEntryView.setBrandingColor(getResources().getColor(COLORS[which]));
                                dialog.dismiss();
                            }
                        })
                        .setTitle(getString(R.string.branding_color))
                        .create();
                alertDialog.show();

            }
        });

        SecureEntryView secureEntryViewSized = view.findViewById(R.id.secureEntryViewSized);
        secureEntryViewSized.setToken(ROTATING_TOKEN);

        SecureEntryView secureEntryViewFullWidth = view.findViewById(R.id.secureEntryViewFullWidth);
        secureEntryViewFullWidth.setToken(ROTATING_TOKEN);

        // don't set it here
        SecureEntryView secureEntryViewNoToken = view.findViewById(R.id.secureEntryViewNoToken);

        SecureEntryView secureEntryViewBadToken = view.findViewById(R.id.secureEntryViewBadToken);
        secureEntryViewBadToken.setToken(BAD_TOKEN);

    }
}
