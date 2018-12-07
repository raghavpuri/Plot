package com.alzheimersmate.almate;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder> {
    private Context mContext;
    private Cursor mCursor;

    public PlacesAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder {

        public TextView placenameText;
        public Button buttonNavig;

        public PlacesViewHolder(@NonNull View itemView) {
            super(itemView);

            placenameText = itemView.findViewById(R.id.textview_placename_item);
            buttonNavig = itemView.findViewById(R.id.navigate_btn);
            buttonNavig.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String latitude, longitude, placebtnTag;
                            int indexofspaceplace;
                            placebtnTag = buttonNavig.getTag().toString();
                            indexofspaceplace = placebtnTag.indexOf(" ");
                            latitude = placebtnTag.substring(0,indexofspaceplace);
                            longitude = placebtnTag.substring(indexofspaceplace+1);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q="+ latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            mContext.startActivity(mapIntent);
                        }
                    }
            );
        }
    }

    @NonNull
    @Override
    public PlacesAdapter.PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.place_item, viewGroup, false);
        return new PlacesAdapter.PlacesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlacesAdapter.PlacesViewHolder placeViewHolder, int i) {
        if(!mCursor.moveToPosition(i)) {
            return;
        }
        String name = mCursor.getString(mCursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_PLACE_NAME));
        String latitude = mCursor.getString(mCursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_LATITUDE));
        String longitude = mCursor.getString(mCursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_LONGITUDE));
        long id = mCursor.getLong(mCursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_ID));

        placeViewHolder.placenameText.setText(name);
        placeViewHolder.buttonNavig.setTag(latitude + " " + longitude);
        placeViewHolder.itemView.setTag(id);
        placeViewHolder.itemView.setElevation(id+5);
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
