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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ticketmaster.presence.SecureEntryView;
import com.ticketmaster.presence.secure_entry_demo.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

public class TicketsFragment extends Fragment {

  private static String[] listItems = {
      "eyJiIjoiNDg2ODg2OTg3Nzc1MTAwOWEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBrbXVtOGRsY3A2IiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiODY3OTU4MjU2NDk0MTU0NWEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBrMHhsMm1hdzRuIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiNTUwODkzNDc1NjYyMzQyOGEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBraGM3dTVtanZqIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiNDg2ODg2OTg3Nzc1MTAwOWEifQ==",
      "eyJiIjoiODY3OTU4MjU2NDk0MTU0NWEifQ==",
      "eyJiIjoiMDQ0MjQyODEwNTA2OTM4NmEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBqYnV3eXJxM2RrIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiNTUwODkzNDc1NjYyMzQyOGEifQ==",
      "eyJiIjoiMjQxMDI0NTE3NDYyNjg1MGEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBpcThwd3hiNzUyIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiNTU1MTU1OTIwNTAzMDkxNWEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBrMDhqdHpkMmZ1IiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiMDQ0MjQyODEwNTA2OTM4NmEifQ==",
      "eyJiIjoiNzA0MTk0ODU3MDY2NDUwMWEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBqanUxZWw0M285IiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiMjQxMDI0NTE3NDYyNjg1MGEifQ==",
      "eyJiIjoiMTczMDE3Njg5OTM5MTczOGEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBpb2s3OG1ibzhxIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiNTU1MTU1OTIwNTAzMDkxNWEifQ==",
      "eyJiIjoiNzA0MTk0ODU3MDY2NDUwMWEifQ==",
      "eyJiIjoiMjE5MDIxMzE1NDY0OTMxOGEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBpdXFhdWtndjVhIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiNjAzNjMyODI0ODUxOTkxNmEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBrcWZseTZha2VnIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiMTczMDE3Njg5OTM5MTczOGEifQ==",
      "eyJiIjoiMjE5MDIxMzE1NDY0OTMxOGEifQ==",
      "eyJiIjoiNjAzNjMyODI0ODUxOTkxNmEifQ=="
  };

  private RecyclerView recyclerViewBarCodes;

  public static TicketsFragment newInstance() {
    return new TicketsFragment();
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_tickets, container, false);
    recyclerViewBarCodes = view.findViewById(R.id.recyclerViewBarCodes);
    recyclerViewBarCodes.setLayoutManager(
        new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    SnapHelper snapHelper = new LinearSnapHelper();
    snapHelper.attachToRecyclerView(recyclerViewBarCodes);
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    recyclerViewBarCodes.setAdapter(new ItemAdapter());
  }


  private static class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
      View view = LayoutInflater.from(viewGroup.getContext())
          .inflate(R.layout.layout_tickets_row, viewGroup, false);
      return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
      viewHolder.secureEntryView.setToken(listItems[i]);
    }

    @Override
    public int getItemCount() {
      return listItems.length;
    }
  }


  private static class ViewHolder extends RecyclerView.ViewHolder {

    SecureEntryView secureEntryView;

    ViewHolder(@NonNull View itemView) {
      super(itemView);
      secureEntryView = itemView.findViewById(R.id.secureEntryView);
    }
  }
}
