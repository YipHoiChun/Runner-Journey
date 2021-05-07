package com.example.runnerjourney.view;


import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.runnerjourney.JourneyProviderContract;
import com.example.runnerjourney.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class JourneysActivity extends ListActivity {
    private TextView textView;
    private DatePickerDialog.OnDateSetListener dateListener;

    private ListView listView;
    private JourneyAdapter journeyAdapter;
    private ArrayList<JourneyItem> itemArrayList;
    //ListView should display the name of the journey next to the custom image uploaded by the user
    private class JourneyAdapter extends ArrayAdapter<JourneyItem> {
        private ArrayList<JourneyItem> items;

        public JourneyAdapter(Context context, int textViewResourceId, ArrayList<JourneyItem> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.journey_list, null);
            }

            JourneyItem item = items.get(position);
            if (item != null) {
                TextView text = view.findViewById(R.id.list_text);
                ImageView img = view.findViewById(R.id.list_image);
                if (text != null) {
                    text.setText(item.getName());
                }
                if (img != null) {
                    String strUri = item.getStrUri();
                    if (strUri != null) {
                        try {
                            final Uri imageUri = Uri.parse(strUri);
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            img.setImageBitmap(selectedImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        img.setImageDrawable(getResources().getDrawable(R.drawable.running));
                    }
                }
            }
            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journeys);

        itemArrayList = new ArrayList<JourneyItem>();
        journeyAdapter = new JourneyAdapter(this, R.layout.journey_list, itemArrayList);
        setListAdapter(journeyAdapter);
        setUpDateDialogue();

        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                JourneyItem o = (JourneyItem) listView.getItemAtPosition(position);
                long journeyID = o.get_id();

                Bundle b = new Bundle();
                b.putLong("journeyID", journeyID);
                Intent singleJourney = new Intent(JourneysActivity.this, SingleJourneyActivity.class);
                singleJourney.putExtras(b);
                startActivity(singleJourney);
            }
        });
    }
    // Update the view in case of title or image changes
    @Override
    public void onResume() {
        super.onResume();
        String date = textView.getText().toString();
        if (!date.toLowerCase().equals("select date")) {
            listJourneys(date);
        }
    }

    private void setUpDateDialogue() {
        textView = findViewById(R.id.selectDateText);
        listView = getListView();

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year;
                int month;
                int day;
                // Select the current date if it is the first time you select a date, otherwise select the last selected date
                if (textView.getText().toString().toLowerCase().equals("select date")) {
                    Calendar calender = Calendar.getInstance();
                    year = calender.get(Calendar.YEAR);
                    month = calender.get(Calendar.MONTH);
                    day = calender.get(Calendar.DAY_OF_MONTH);
                } else {
                    String[] dateParts = textView.getText().toString().split("/");
                    year = Integer.parseInt(dateParts[2]);
                    month = Integer.parseInt(dateParts[1]) - 1;
                    day = Integer.parseInt(dateParts[0]);
                }

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        JourneysActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateListener,
                        year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date;

                if (month < 10) {
                    date = day + "/0" + month + "/" + year;
                } else {
                    date = day + "/" + month + "/" + year;
                }

                if (day < 10) {
                    date = "0" + date;
                }

                textView.setText(date);

                listJourneys(date);
            }
        };
    }

    private void listJourneys(String date) {
        String[] dateParts = date.split("/");
        date = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];

        Cursor cursor = getContentResolver().query(JourneyProviderContract.J_URI,
                new String[]{JourneyProviderContract.J_ID + " _id", JourneyProviderContract.J_NAME, JourneyProviderContract.J_IMAGE}, JourneyProviderContract.J_DATE + " = ?", new String[]{date}, JourneyProviderContract.J_NAME + " ASC");

        itemArrayList = new ArrayList<JourneyItem>();
        journeyAdapter.notifyDataSetChanged();
        journeyAdapter.clear();
        journeyAdapter.notifyDataSetChanged();
        try {
            while (cursor.moveToNext()) {
                JourneyItem item = new JourneyItem();
                item.setName(cursor.getString(cursor.getColumnIndex(JourneyProviderContract.J_NAME)));
                item.setStrUri(cursor.getString(cursor.getColumnIndex(JourneyProviderContract.J_IMAGE)));
                item.set_id(cursor.getLong(cursor.getColumnIndex("_id")));
                itemArrayList.add(item);
            }
        } finally {
            if (itemArrayList != null && itemArrayList.size() > 0) {
                journeyAdapter.notifyDataSetChanged();
                for (int i = 0; i < itemArrayList.size(); i++) {
                    journeyAdapter.add(itemArrayList.get(i));
                }
            }
            cursor.close();
            journeyAdapter.notifyDataSetChanged();
        }

    }
}
