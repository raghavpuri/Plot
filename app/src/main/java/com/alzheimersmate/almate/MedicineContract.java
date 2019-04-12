package com.alzheimersmate.almate;

import android.provider.BaseColumns;

public class MedicineContract   {

    private MedicineContract() {}

    public static final class MedicineEntry implements BaseColumns {
        public static final String TABLE_NAME = "medicineList";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_MED_NAME = "name";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_DOCTOR = "doctor";
    }
}
