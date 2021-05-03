package com.example.runnerjourney;


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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class JourneysActivity extends ListActivity {
    private TextView textView;
    private DatePickerDialog.OnDateSetListener dateListener;

    private ListView journeyList;
    private JourneyAdapter adapter;
    private ArrayList<JourneyItem> journeyNames;

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
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.journey_list, null);
            }

            JourneyItem item = items.get(position);
            if (item != null) {
                TextView text = view.findViewById(R.id.singleJourney);
                ImageView img = view.findViewById(R.id.journeyList_journeyImg);
                if (text != null) {
                    text.setText(item.getName());
                }
                if(img != null) {
                    String strUri = item.getStrUri();
                    if(strUri != null) {
                        try {
                            final Uri imageUri = Uri.parse(strUri);
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            img.setImageBitmap(selectedImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
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

        journeyNames = new ArrayList<JourneyItem>();
        adapter = new JourneyAdapter(this, R.layout.journey_list, journeyNames);
        setListAdapter(adapter);
        setUpDateDialogue();

        journeyList.setClickable(true);
        journeyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                JourneyItem o = (JourneyItem) journeyList.getItemAtPosition(position);
                long journeyID = o.get_id();

                Bundle b = new Bundle();
                b.putLong("journeyID", journeyID);
                Intent singleJourney = new Intent(JourneysActivity.this, SingleJourneyActivity.class);
                singleJourney.putExtras(b);
                startActivity(singleJourney);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String date = textView.getText().toString();
        if(!date.toLowerCase().equals("select date")) {
            listJourneys(date);
        }
    }

    private void setUpDateDialogue() {
        textView = findViewById(R.id.selectDateText);
        journeyList = getListView();

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year;
                int month;
                int day;

                if(textView.getText().toString().toLowerCase().equals("select date")) {
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

                DatePickerDialog dialog = new DatePickerDialog(
                        JourneysActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date;

                if(month < 10) {
                    date = day + "/0" + month + "/" + year;
                } else {
                    date = day + "/" + month + "/" + year;
                }

                if(day < 10) {
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

        Log.d("mdp", "Searching for date " + date);

        Cursor c = getContentResolver().query(JourneyProviderContract.J_URI,
                new String[] {JourneyProviderContract.J_ID + " _id", JourneyProviderContract.J_NAME, JourneyProviderContract.J_IMAGE}, JourneyProviderContract.J_DATE + " = ?", new String[] {date}, JourneyProviderContract.J_NAME + " ASC");

        Log.d("mdp", "Journeys Loaded: " + c.getCount());

        journeyNames = new ArrayList<JourneyItem>();
        adapter.notifyDataSetChanged();
        adapter.clear();
        adapter.notifyDataSetChanged();
        try {
            while(c.moveToNext()) {
                JourneyItem item = new JourneyItem();
                item.setName(c.getString(c.getColumnIndex(JourneyProviderContract.J_NAME)));
                item.setStrUri(c.getString(c.getColumnIndex(JourneyProviderContract.J_IMAGE)));
                item.set_id(c.getLong(c.getColumnIndex("_id")));
                journeyNames.add(item);
            }
        } finally {
            if(journeyNames != null && journeyNames.size() > 0) {
                adapter.notifyDataSetChanged();
                for(int i = 0; i < journeyNames.size(); i++) {
                    adapter.add(journeyNames.get(i));
                }
            }
            c.close();
            adapter.notifyDataSetChanged();
        }

    }
}
