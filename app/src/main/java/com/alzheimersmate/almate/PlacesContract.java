package com.alzheimersmate.almate;

import android.provider.BaseColumns;

public class PlacesContract {
    private PlacesContract() {}

    public static final class PlacesEntry implements BaseColumns {
        public static final String TABLE_NAME = "placesList";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_PLACE_NAME = "name";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
    }
}
