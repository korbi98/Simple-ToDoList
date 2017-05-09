package com.example.korbi.todolist;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class ADDtask extends AppCompatActivity {

    EditText newTaskEntry;
    Button confirmNewTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtask);

        newTaskEntry = (EditText) findViewById(R.id.NewTaskName);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        confirmNewTask = (Button) findViewById(R.id.ConfirmTask);

        confirmNewTask.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
           sendTask();
            }

        });

        newTaskEntry.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    sendTask();
                    return true;
                }

                return false;
            }
        });
    }

    public void sendTask()
    {
        Intent sendNewTask = new Intent();

        if (newTaskEntry.getText().toString().trim().length() > 0)
        {
            String newtaskname = newTaskEntry.getText().toString();
            sendNewTask.putExtra("NewTaskName", newtaskname);
            setResult(1, sendNewTask);
        }
        else
        {
            setResult(0, sendNewTask);
        }
        finish();
    }
}
