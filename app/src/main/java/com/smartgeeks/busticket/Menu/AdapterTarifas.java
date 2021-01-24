package com.smartgeeks.busticket.Menu;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartgeeks.busticket.Objcect.Tarifa;
import com.smartgeeks.busticket.R;

import java.util.List;

public class AdapterTarifas extends  RecyclerView.Adapter<AdapterTarifas.viewHolder>{

    private List<Tarifa> tarifaList;
    private Context context;

    public AdapterTarifas(Context context, List<Tarifa> tarifaList) {
        this.tarifaList = tarifaList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_tarifa, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(viewHolder holder, int position) {
        Tarifa tarifa = tarifaList.get(position);

        holder.tvNameTarifa.setText(tarifa.getNombreTarifa());

        if (tarifa.getModule() == 0){
            holder.tvNameTarifa.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_tarifa_blue));
        }else if (tarifa.getModule() == 1){
            holder.tvNameTarifa.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_tarifa_cyan));
        }else if (tarifa.getModule() == 2){
            holder.tvNameTarifa.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_tarifa_green));
        }
    }

    @Override
    public int getItemCount() {
        return tarifaList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        TextView tvNameTarifa;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameTarifa = itemView.findViewById(R.id.tvNameTarifa);
        }
    }
}
