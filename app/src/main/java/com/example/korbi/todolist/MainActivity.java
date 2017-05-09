package com.example.korbi.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import android.util.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity
{

    private static final String TAG = MainActivity.class.getSimpleName();

    public static TaskDbHelper db;
    List<Task> taskItems;
    ToDoListAdapter adapter;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new TaskDbHelper(this);

        lv = (ListView) findViewById(R.id.TaskList);
        taskItems = db.getAllTasks();
        adapter = new ToDoListAdapter(taskItems, this);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                Intent OpenAddTask = new Intent(MainActivity.this, ADDtask.class);
                startActivityForResult(OpenAddTask, 1);
            }
        });

    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1)
        {
            int id = adapter.getCount();
            Task NewTask = new Task(id, data.getStringExtra("NewTaskName"), false);

            db.addTask(NewTask);
            taskItems.add(NewTask);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        int position = (int) info.id;

        switch (item.getItemId())
        {
            case R.id.context_delete:
                Log.d(TAG, taskItems.get(position).getTaskname());
                db.deleteTask(taskItems.get(position).getTaskname());
                this.taskItems.remove(position);
                this.adapter.notifyDataSetChanged();
                break;

            case R.id.context_edit:
                createEditDialogue(taskItems.get(position));
                break;
        }

        return super.onContextItemSelected(item);
    }

    public void createEditDialogue(final Task task)
    {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View dialogueView = li.inflate(R.layout.context_dialog_edit, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setView(dialogueView);

        final EditText newTaskName = (EditText) dialogueView.findViewById(R.id.edit_dialog_input);
        newTaskName.setText(task.getTaskname());

        alertDialogBuilder.setCancelable(true)

                .setPositiveButton("Save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        task.setTaskname(newTaskName.getText().toString());
                        adapter.notifyDataSetChanged();
                        db.updateTask(task);
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
