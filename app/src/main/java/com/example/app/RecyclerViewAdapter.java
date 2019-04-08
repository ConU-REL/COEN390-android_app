package com.example.app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";


    private ArrayList<String> adddataInputsListText;
    SharedPreferencesHelper sharedPreferencesHelper;
    private OnItemListener onItemListener;
    private Context context;
    public RecyclerViewAdapter(ArrayList<String> adddataInputsListText, OnItemListener onItemListener) {
        this.adddataInputsListText = adddataInputsListText;
        this.onItemListener=onItemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem,parent,false);
        ViewHolder holder=new ViewHolder(view,onItemListener);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        viewHolder.image_name.setText(adddataInputsListText.get(i));



    }

    @Override
    public int getItemCount() {
        return adddataInputsListText.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView image_name;
        RelativeLayout parentLayout;
        OnItemListener onItemListener;
        public ViewHolder(@NonNull View itemView,OnItemListener onItemListener) {
            super(itemView);
            image_name=itemView.findViewById(R.id.image_name);
            parentLayout=itemView.findViewById(R.id.parent_layout);
            this.onItemListener=onItemListener;
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onItemListener.onNoteClick(getAdapterPosition());

        }
    }
    public interface OnItemListener{

        void onNoteClick(int position);
    }
}
