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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import com.ticketmaster.presence.SecureEntryView;
import com.ticketmaster.presence.secure_entry_demo.R;
import com.ticketmaster.presence.secure_entry_demo.TokenUtils;

import java.util.ArrayList;
import java.util.List;

public class PlaygroundFragment extends Fragment implements View.OnClickListener {

  private TokenAdapter tokenAdapter;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.fragment_playground, container, false);
    final RecyclerView recyclerViewTickets = view.findViewById(R.id.recyclerViewTickets);
    final LinearLayoutManager llManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,
        false);
    recyclerViewTickets.setLayoutManager(llManager);
    tokenAdapter = new TokenAdapter(TokenUtils.getEmptyTokens());
    final SnapHelper snapHelper = new LinearSnapHelper();
    recyclerViewTickets.setAdapter(tokenAdapter);
    snapHelper.attachToRecyclerView(recyclerViewTickets);
    view.findViewById(R.id.rotatingButton).setOnClickListener(this);
    view.findViewById(R.id.qrButton).setOnClickListener(this);
    view.findViewById(R.id.staticPdfButton).setOnClickListener(this);
    view.findViewById(R.id.errorButton).setOnClickListener(this);
    return view;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.rotatingButton:
        tokenAdapter.swapData(TokenUtils.getRotatingTokens());
        break;
      case R.id.qrButton:
        tokenAdapter.swapData(TokenUtils.getQrCodeTokens());
        break;
      case R.id.staticPdfButton:
        tokenAdapter.swapData(TokenUtils.getStaticTokens());
        break;
      case R.id.errorButton:
        tokenAdapter.swapData(TokenUtils.getNullTokens());
        break;
    }
  }

  private static class TokenAdapter extends RecyclerView.Adapter<PlaygroundFragment.ViewHolder> {

    private List<TokenUtils.TokenData> tokens;

    TokenAdapter(List<TokenUtils.TokenData> tokens) {
      this.tokens = new ArrayList<>(tokens);
    }

    void swapData(@Nullable List<TokenUtils.TokenData> tokens) {
      this.tokens.clear();
      if (tokens != null) {
        this.tokens.addAll(tokens);
      }
      notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaygroundFragment.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
      final View view = LayoutInflater.from(viewGroup.getContext())
          .inflate(R.layout.layout_tickets_row, viewGroup, false);
      return new PlaygroundFragment.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaygroundFragment.ViewHolder viewHolder, int i) {

      final TokenUtils.TokenData tokenData = tokens.get(i);
      final Bitmap errorIcon = BitmapFactory
          .decodeResource(viewHolder.itemView.getResources(), R.drawable.ic_error);
      if (tokenData.token == null) {
        viewHolder.secureEntryView.showError("Invalid Barcode", errorIcon);
      } else if (TextUtils.isEmpty(tokenData.token)) {
        // do nothing to simulate loading
      } else {
        viewHolder.secureEntryView.setToken(tokenData.token);
        final int color = viewHolder.itemView.getResources()
            .getColor(TokenUtils.COLORS[tokenData.colorIndex]);
        viewHolder.secureEntryView.setBrandingColor(color);
      }
    }

    @Override
    public int getItemCount() {
      return tokens.isEmpty() ? 0 : tokens.size();
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
