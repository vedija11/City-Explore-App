package com.example.group22_hw07;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EditTripLocationAdapter extends RecyclerView.Adapter<EditTripLocationAdapter.EditTripLocationViewHolder>{
    ArrayList<String> places = new ArrayList<>();

    public EditTripLocationAdapter(ArrayList<String> places) {
        this.places = places;
    }

    @NonNull
    @Override
    public EditTripLocationAdapter.EditTripLocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_location_recycler_view, parent, false);
        return new EditTripLocationAdapter.EditTripLocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EditTripLocationAdapter.EditTripLocationViewHolder holder, final int position) {
        holder.placeName.setText(places.get(position));
        holder.remove_Place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                places.remove(position);
                CreateTripActivity.locationAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public static class EditTripLocationViewHolder extends RecyclerView.ViewHolder {
        TextView placeName;
        ImageButton remove_Place;

        public EditTripLocationViewHolder(View itemView) {
            super(itemView);
            placeName = (TextView) itemView.findViewById(R.id.tv_PlaceName);
            remove_Place = (ImageButton) itemView.findViewById(R.id.btn_RemovePlace);
        }
    }
}
