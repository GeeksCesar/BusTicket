package com.smartgeeks.busticket.Menu;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartgeeks.busticket.Modelo.TarifaParadero;
import com.smartgeeks.busticket.R;

import java.util.List;

class AdapterPrecios extends RecyclerView.Adapter<AdapterPrecios.ViewHolder> {

    private List<TarifaParadero> tarifaParaderos;
    private Context context;
    private ItemClickListener mClickListener;

    public AdapterPrecios(Context context, List<TarifaParadero> tarifaParaderos) {
        this.tarifaParaderos = tarifaParaderos;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_price_pasaje, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvPrecioPasaje.setText("" + tarifaParaderos.get(position).getMonto());
    }

    @Override
    public int getItemCount() {
        return tarifaParaderos.size();
    }

    // convenience method for getting data at click position
    int getItem(int position) {
        return tarifaParaderos.get(position).getMonto();
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvPrecioPasaje;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPrecioPasaje = itemView.findViewById(R.id.tvPrecioPasaje);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

}
