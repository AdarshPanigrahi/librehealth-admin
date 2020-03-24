package com.adarshpanig.librehealthadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{

    List<String> hList;

    public final static String hName="";
    public RecyclerAdapter(List<String> diseaseList) {
        this.hList = diseaseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv.setText(hList.get(position));
    }

    @Override
    public int getItemCount() {
        return hList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv=itemView.findViewById(R.id.tv);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent is = new Intent(v.getContext(),HospitalsResult.class);
            is.putExtra(hName,hList.get(getAdapterPosition()));
            v.getContext().startActivity(is);
        }
    }
}
