package com.project.scheduleasy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private List<TimetableMeta> timetables;
    private final OnTimetableChangeListener listener;
    private final String userType;
    private final Context context;
    private final boolean isFaculty;

    public interface OnTimetableChangeListener {
        void onTimetableDeleted(TimetableMeta timetable);

        void onTimetableClick(TimetableMeta timetable);
    }

    public TimetableAdapter(List<TimetableMeta> timetables, OnTimetableChangeListener listener, String userType, Context context) {
        this.timetables = new ArrayList<>(timetables);
        this.listener = listener;
        this.userType = userType;
        this.context = context;
        this.isFaculty = "FACULTY".equals(userType);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView dateText;
        View deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.timetableTitle);
            dateText = itemView.findViewById(R.id.timetableDate);
            deleteButton = itemView.findViewById(R.id.btnDelete);

            deleteButton.setVisibility(isFaculty ? View.VISIBLE : View.GONE);

            //normal click opens timetable
            itemView.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION){
                    ((TimetableAdapter)getBindingAdapter()).onItemClick(getAdapterPosition());
                }
            });

            //long press delete (for faculty)
            if(isFaculty){
                itemView.setOnLongClickListener(v -> {
                    ((TimetableAdapter) getBindingAdapter()).showDeleteDialog(getAdapterPosition());
                    return true;
                });
            }
        }
    }

    private void onItemClick(int position) {
        if (listener != null) {
            listener.onTimetableClick(timetables.get(position));
        }
    }

    private void showDeleteDialog(int position) {
        if (!userType.equals("FACULTY")) return;

        new AlertDialog.Builder(context)
                .setTitle("Delete Timetable")
                .setMessage("Are you sure you want to delete this timetable?")
                .setPositiveButton("Delete", (dialog, which) -> deleteItem(position))
                .setNegativeButton("Cancel", null)
                .show();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timetable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimetableMeta timetable = timetables.get(position);
        holder.titleText.setText(timetable.getTitle());
        holder.dateText.setText(timetable.getFormattedDate());

        // Set click listener for delete button
        if (userType.equals("FACULTY")) {
            holder.deleteButton.setOnClickListener(v -> showDeleteDialog(position));
        }
    }

    @Override
    public int getItemCount() {
        return timetables.size();
    }

    public void deleteItem(int position) {
        TimetableMeta deletedItem = timetables.remove(position);
        notifyItemRemoved(position);
        if (listener != null) {
            listener.onTimetableDeleted(deletedItem);
        }
    }

    public void updateTimetables(List<TimetableMeta> newTimetables) {
        timetables = new ArrayList<>(newTimetables);
        notifyDataSetChanged();
    }

    public void addTimetable(TimetableMeta timetable) {
        timetables.add(timetable);
        notifyItemInserted(timetables.size() - 1);
    }

    // Add this method to restore deleted items (for undo)
    public void restoreItem(int position, TimetableMeta timetable) {
        timetables.add(position, timetable);
        notifyItemInserted(position);
    }

    // Add this method to get all timetables
    public List<TimetableMeta> getTimetables() {
        return new ArrayList<>(timetables);
    }
}