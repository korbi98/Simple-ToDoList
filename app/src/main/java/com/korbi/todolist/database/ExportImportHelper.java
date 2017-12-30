package com.korbi.todolist.database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.korbi.todolist.todolist.R;
import com.korbi.todolist.ui.MainActivity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by korbi on 12/27/17.
 * This Class handles the export and import from the database to and from a CSV table
 */

public class ExportImportHelper {

    private static final String validFileString = "This is a valid Simple TodoList export file. Please don't modify";
    private Context context;

    public ExportImportHelper(Context context) {
        this.context = context;
    }

    public void writeCSV(String directory) {

        Calendar cal = Calendar.getInstance();

        String year = String.valueOf(cal.get(Calendar.YEAR));
        String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

        String filename = "Tasks" + String.valueOf(year) +
                String.valueOf(month) + String.valueOf(day) + ".csv"; //filename is Tasks + current date
        String data = validFileString + "\n" + MainActivity.db.createCSVstring();
        File file;
        FileOutputStream outputStream;

        try {
            file = new File(directory, filename);
            Log.d("test", file.getAbsolutePath());
            outputStream = new FileOutputStream(file);
            outputStream.write(data.getBytes());
            outputStream.close();
            Toast.makeText(context, context.getString(R.string.export_successful), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readCSV(String directory) {

        List<String> importList = new ArrayList<>();

        File importFile = new File(directory);
        try {
            FileInputStream input = new FileInputStream(importFile);
            DataInputStream dataInputStream = new DataInputStream(input);
            BufferedReader br = new BufferedReader(new InputStreamReader(dataInputStream));

            String in;

            while ((in = br.readLine()) != null) {
                importList.add(in);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String str : importList) {
            Log.d("importList", str);
        }

        Log.d("index0", importList.get(0));
        if (importList.get(0).equals(validFileString)) {
            importList.remove(0);
            MainActivity.db.importFromCSV(importList);
            Toast.makeText(context, context.getString(R.string.import_successful), Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(context, context.getString(R.string.import_data_corrupted), Toast.LENGTH_LONG).show();
    }
}
