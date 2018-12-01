package com.alzheimersmate.almate;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    public MedicineAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public class MedicineViewHolder extends RecyclerView.ViewHolder {

        public TextView nameText;
        public TextView timeText;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.textview_name_item);
            timeText = itemView.findViewById(R.id.textview_time_item);
        }
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.medicine_item, viewGroup, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder medicineViewHolder, int i) {
        if(!mCursor.moveToPosition(i)) {
            return;
        }
        String name = mCursor.getString(mCursor.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_MED_NAME));
        String time = mCursor.getString(mCursor.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_TIME));
        long id = mCursor.getLong(mCursor.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_ID));

        medicineViewHolder.nameText.setText(name);
        medicineViewHolder.timeText.setText(time);
        medicineViewHolder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if(mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if(newCursor != null) {
            notifyDataSetChanged();
        }
    }
}
