package com.ticketmaster.presence.secure_entry_demo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ticketmaster.presence.SecureEntryView;
import com.ticketmaster.presence.secure_entry_demo.R;

public class TicketsViewFragment extends Fragment {

    static String[] listItems = {
            "eyJiIjoiOTY0NTM3MjgzNDIxIiwidCI6IlRNOjowMzo6MjAxeXRmbmllN2tpZmxzZ2hncHQ5ZDR4N2JudTljaG4zYWNwdzdocjdkOWZzc3MxcyIsImNrIjoiMzRkNmQyNTNiYjNkZTIxOTFlZDkzMGY2MmFkOGQ0ZDM4NGVhZTVmNSJ9",
            "eyJiIjogIjE5NzM3OTA2OTQzNDc3OTlhIiwidCI6ICIiLCJjayI6ICIiLCAiZWsiOiAiIn07",
            "eyJiIjoiOTY0NTM3MjgzNDIxIiwidCI6IlRNOjowMzo6MjAxeXRmbmllN2tpZmxzZ2hncHQ5ZDR4N2JudTljaG4zYWNwdzdocjdkOWZzc3MxcyIsImNrIjoiMzRkNmQyNTNiYjNkZTIxOTFlZDkzMGY2MmFkOGQ0ZDM4NGVhZTVmNSJ9",
            "eyJiIjoiOTY0NTM3MjgzNDIxIiwidCI6IlRNOjowMzo6MjAxeXRmbmllN2tpZmxzZ2hncHQ5ZDR4N2JudTljaG4zYWNwdzdocjdkOWZzc3MxcyIsImNrIjoiMzRkNmQyNTNiYjNkZTIxOTFlZDkzMGY2MmFkOGQ0ZDM4NGVhZTVmNSJ9",
            "eyJiIjogIjE5NzM3OTA2OTQzNDc3OTlhIiwidCI6ICIiLCJjayI6ICIiLCAiZWsiOiAiIn07",
            "eyJiIjoiOTY0NTM3MjgzNDIxIiwidCI6IlRNOjowMzo6MjAxeXRmbmllN2tpZmxzZ2hncHQ5ZDR4N2JudTljaG4zYWNwdzdocjdkOWZzc3MxcyIsImNrIjoiMzRkNmQyNTNiYjNkZTIxOTFlZDkzMGY2MmFkOGQ0ZDM4NGVhZTVmNSJ9",
            "eyJiIjoiOTY0NTM3MjgzNDIxIiwidCI6IlRNOjowMzo6MjAxeXRmbmllN2tpZmxzZ2hncHQ5ZDR4N2JudTljaG4zYWNwdzdocjdkOWZzc3MxcyIsImNrIjoiMzRkNmQyNTNiYjNkZTIxOTFlZDkzMGY2MmFkOGQ0ZDM4NGVhZTVmNSJ9",
            "eyJiIjogIjE5NzM3OTA2OTQzNDc3OTlhIiwidCI6ICIiLCJjayI6ICIiLCAiZWsiOiAiIn07",
            "eyJiIjogIjE5NzM3OTA2OTQzNDc3OTlhIiwidCI6ICIiLCJjayI6ICIiLCAiZWsiOiAiIn07",
            "eyJiIjoiOTY0NTM3MjgzNDIxIiwidCI6IlRNOjowMzo6MjAxeXRmbmllN2tpZmxzZ2hncHQ5ZDR4N2JudTljaG4zYWNwdzdocjdkOWZzc3MxcyIsImNrIjoiMzRkNmQyNTNiYjNkZTIxOTFlZDkzMGY2MmFkOGQ0ZDM4NGVhZTVmNSJ9",
            "eyJiIjogIjE5NzM3OTA2OTQzNDc3OTlhIiwidCI6ICIiLCJjayI6ICIiLCAiZWsiOiAiIn07",
            "eyJiIjogIjE5NzM3OTA2OTQzNDc3OTlhIiwidCI6ICIiLCJjayI6ICIiLCAiZWsiOiAiIn07",
            "eyJiIjoiOTY0NTM3MjgzNDIxIiwidCI6IlRNOjowMzo6MjAxeXRmbmllN2tpZmxzZ2hncHQ5ZDR4N2JudTljaG4zYWNwdzdocjdkOWZzc3MxcyIsImNrIjoiMzRkNmQyNTNiYjNkZTIxOTFlZDkzMGY2MmFkOGQ0ZDM4NGVhZTVmNSJ9"
    };

    private RecyclerView recyclerViewBarCodes;

    public static TicketsViewFragment newInstance() {
        return new TicketsViewFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tickets, container, false);
        recyclerViewBarCodes = view.findViewById(R.id.recyclerViewBarCodes);
        recyclerViewBarCodes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerViewBarCodes);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewBarCodes.setAdapter(new ItemAdapter());
    }


    public static class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_tickets_row, viewGroup, false);
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


    static class ViewHolder extends RecyclerView.ViewHolder {
        SecureEntryView secureEntryView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            secureEntryView = itemView.findViewById(R.id.secureEntryView);
        }
    }
}
