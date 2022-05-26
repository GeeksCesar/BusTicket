package com.smartgeeks.busticket.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.smartgeeks.busticket.Modelo.TipoUsuario;
import com.smartgeeks.busticket.R;

import java.util.List;

public class AdapterTarifas extends  RecyclerView.Adapter<AdapterTarifas.viewHolder>{

    private List<TipoUsuario> tarifaList;
    private Context context;

    public AdapterTarifas(Context context, List<TipoUsuario> tarifaList) {
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
        TipoUsuario tarifa = tarifaList.get(position);

        holder.tvNameTarifa.setText(tarifa.getNombre());

        if (position % 3 == 0){
            holder.tvNameTarifa.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_tarifa_blue));
        }else if (position % 3 == 1){
            holder.tvNameTarifa.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_tarifa_cyan));
        }else if (position % 3 == 2){
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
