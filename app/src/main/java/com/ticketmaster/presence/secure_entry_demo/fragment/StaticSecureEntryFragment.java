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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ticketmaster.presence.SecureEntryView;
import com.ticketmaster.presence.secure_entry_demo.R;

public class StaticSecureEntryFragment extends Fragment {

    static final String STATIC_TOKEN = "eyJiIjogIjE5NzM3OTA2OTQzNDc3OTlhIiwidCI6ICIiLCJjayI6ICIiLCAiZWsiOiAiIn07";
    static final String BAD_TOKEN = "junk data";

    public static StaticSecureEntryFragment newInstance() {

        return new StaticSecureEntryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_static, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SecureEntryView secureEntryView = view.findViewById(R.id.secureEntryView);
        secureEntryView.setToken(STATIC_TOKEN);

        SecureEntryView secureEntryViewSized = view.findViewById(R.id.secureEntryViewSized);
        secureEntryViewSized.setToken(STATIC_TOKEN);

        SecureEntryView secureEntryViewFullWidth = view.findViewById(R.id.secureEntryViewFullWidth);
        secureEntryViewFullWidth.setToken(STATIC_TOKEN);

        SecureEntryView secureEntryViewNoToken = view.findViewById(R.id.secureEntryViewNoToken);
        // don't set it here

        SecureEntryView secureEntryViewBadToken = view.findViewById(R.id.secureEntryViewBadToken);
        secureEntryViewBadToken.setToken(BAD_TOKEN);
    }
}
