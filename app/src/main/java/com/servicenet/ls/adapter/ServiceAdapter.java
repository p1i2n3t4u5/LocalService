package com.servicenet.ls.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.servicenet.ls.R;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {


    private LayoutInflater layoutInflater;

    private List<String> data;

    private Context context;


    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{

        TextView textTitle,textDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.item_title_id);
            textDescription = itemView.findViewById(R.id.item_description_id);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d("LocalService", "ViewHolder Item clicked:" +getAdapterPosition());
        }
    }

    public ServiceAdapter(Context context, List<String> data){
        this.layoutInflater= LayoutInflater.from(context);
        this.data=data;
        this.context=context;
    }


    @NonNull
    @Override
    public ServiceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view=layoutInflater.inflate(R.layout.service_item,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceAdapter.ViewHolder viewHolder, int position) {

        String title= data.get(position);
        viewHolder.textTitle.setText(title);

        //all data binding should happen here

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateList(List<String> newData){
        data=new ArrayList<>();
        data.addAll(newData);
        notifyDataSetChanged();

    }

}
