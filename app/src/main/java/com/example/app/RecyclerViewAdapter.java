package com.example.app;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;

/*
 * Adapter for the recycler view
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ArrayList<String> adddataInputsListText;
    private OnItemListener onItemListener;

    RecyclerViewAdapter(ArrayList<String> addDataInputsListText, OnItemListener onItemListener) {
        this.adddataInputsListText = addDataInputsListText;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new ViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        viewHolder.image_name.setText(adddataInputsListText.get(i));
    }

    @Override
    public int getItemCount() {
        return adddataInputsListText.size();
    }

    public interface OnItemListener {
        // attach listener
        void onNoteClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView image_name;
        RelativeLayout parentLayout;
        OnItemListener onItemListener;

        ViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            image_name = itemView.findViewById(R.id.username);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListener.onNoteClick(getAdapterPosition());
        }
    }
}
