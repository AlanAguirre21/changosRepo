package com.example.parkgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {

    private List<Parking> parkingList;
    private boolean deleteMode = false;  // indica si mostrar checkboxes
    private Set<String> selectedIds = new HashSet<>(); // IDs seleccionados para eliminar

    private OnSelectionChangedListener selectionChangedListener;

    public ParkingAdapter(List<Parking> parkingList, boolean b) {
        this.parkingList = parkingList;
    }

    // Setter para activar/desactivar modo eliminar y limpiar selección
    public void setDeleteMode(boolean deleteMode) {
        this.deleteMode = deleteMode;
        if (!deleteMode) {
            selectedIds.clear();
            notifyDataSetChanged();
        }
    }

    // Listener para informar cambios en selección
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking_checkbox, parent, false);
        return new ParkingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        Parking parking = parkingList.get(position);
        holder.textName.setText(parking.getName());
        holder.textAddress.setText(parking.getAddress());

        if (deleteMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(selectedIds.contains(parking.getId()));

            holder.checkBox.setOnClickListener(v -> {
                if (holder.checkBox.isChecked()) {
                    selectedIds.add(parking.getId());
                } else {
                    selectedIds.remove(parking.getId());
                }
                if (selectionChangedListener != null) {
                    selectionChangedListener.onSelectionChanged(selectedIds);
                }
            });
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.checkBox.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return parkingList.size();
    }

    static class ParkingViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textAddress;
        CheckBox checkBox;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textAddress = itemView.findViewById(R.id.textAddress);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(Set<String> selectedIds);
    }
}
