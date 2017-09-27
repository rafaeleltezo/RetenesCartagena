package com.app.master.retenescartagena.Vista;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.app.master.retenescartagena.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PicoyPlaca extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picoy_placa);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());

    }
}
