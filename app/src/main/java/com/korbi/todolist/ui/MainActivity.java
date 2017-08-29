package com.korbi.todolist.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.korbi.todolist.logic.Task;
import com.korbi.todolist.database.TaskDbHelper;
import com.korbi.todolist.logic.ToDoListRecyclerAdapter;
import com.korbi.todolist.widget.TaskWidgetProvider;
import com.korbi.todolist.widget.ToDoListWidget;
import com.korbi.todolist.todolist.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener

{
    public static TaskDbHelper db;
    public static List<Task> taskItems;
    public static List<Task> undoTaskItems;
    public static List<String> categories;
    public  ToDoListRecyclerAdapter adapter;
    public RecyclerView rv;
    private TextView emptylist;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String currentCategory;

    public static SharedPreferences settings;
    public Snackbar undoDeleteSnack;

    @Override
    protected void onCreate(Bundle savedInstanceState)
            //TODO rework settings
            //TODO interactive notifications
    {


        super.onCreate(savedInstanceState);

        settings = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);


        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new TaskDbHelper(this);
        categories = db.getAllCategories();
        setUpNavView();

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            if (bundle.containsKey(Settings.CURRENT_CATEGORY)) {
                setCurrentCategory(bundle.getString(Settings.CURRENT_CATEGORY)); //if app is launched through widget
            }
        }
        else setCurrentCategory(settings.getString(Settings.CURRENT_CATEGORY, categories.get(0))); //if app is launched otherwise

        emptylist = (TextView) findViewById(R.id.emptylistMessage);

        undoTaskItems = new ArrayList<>();

        rv = (RecyclerView) findViewById(R.id.TaskRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        rv.setItemAnimator(new ToDoListAnimator());

        setUpItemSwipe();
        setUpSnackbar();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent OpenAddTask = new Intent(MainActivity.this, AddEditTask.class);
                OpenAddTask.putExtra(Settings.CURRENT_CATEGORY, currentCategory);
                startActivity(OpenAddTask);
            }
        });

        if (settings.getBoolean(Settings.INCLUDE_TIME_SETTING, false))
        {
            for (Task t : taskItems)
            {
                if (t.getTimeIsSet() == Task.DATE_AND_TIME) t.setTimeIsSet(Task.JUST_DATE);
            }
        }

        updateWidget();
    }

    @Override
    public void onResume()
    {

        if (settings.getBoolean(Settings.INCLUDE_TIME_SETTING, false))
        {
            for (Task t : taskItems)
            {
                if (t.getTimeIsSet() == Task.DATE_AND_TIME) t.setTimeIsSet(Task.JUST_DATE);
            }
        }

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            if (bundle.containsKey(Settings.CURRENT_CATEGORY)) {
                setCurrentCategory(bundle.getString(Settings.CURRENT_CATEGORY)); //if app is launched through widget
            }
        }

        updateView();

        updateWidget();
        super.onResume();
    }

    @Override
    public void onStop() {
        updateWidget();
        checkIfToShowEmptyListView();
        super.onStop();
    }

    @Override
    public void onPause() {
        updateWidget();
        checkIfToShowEmptyListView();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_completed_tasks:

                if (db.getUncompletedTasksByCategory(currentCategory).size() != taskItems.size() && taskItems.size() != 0)
                    createConfirmDialogue();

                else
                    Toast.makeText(getApplicationContext(), getString(R.string.nothing_to_delete),
                                    Toast.LENGTH_LONG).show();

                return true;

            case R.id.about:

                Intent openAboutApp = new Intent(MainActivity.this, AboutTheApp.class);
                startActivity(openAboutApp);

                return true;

            case R.id.settings:

                Intent openSettings = new Intent(MainActivity.this, Settings.class);
                startActivity(openSettings);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void createConfirmDialogue() //creates confirm Dialog for clearing completed tasks
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setMessage(getString(R.string.confirm_message))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int size = taskItems.size();
                        adapter.clear();
                        adapter.notifyItemRangeRemoved(0, size);
                        checkIfToShowEmptyListView();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void setUpItemSwipe()
    {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.
                SimpleCallback(0, ItemTouchHelper.LEFT) {

            Drawable background;
            Drawable deleteIcon;
            int deleteIconMargin;
            boolean initiated;

            private void init()
            {
                background = new ColorDrawable(Color.RED);
                deleteIcon = ContextCompat.getDrawable(getApplicationContext(),
                        R.drawable.ic_delete_sweep_white_24px);
                deleteIconMargin = (int) getResources().getDimension(R.dimen.ic_delete_margin);
                initiated = true;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) //Only for Drag and Drop
            {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
            {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir)
            {
                Task task = taskItems.get(viewHolder.getAdapterPosition());
                ToDoListRecyclerAdapter adapter = (ToDoListRecyclerAdapter) rv.getAdapter();
                taskItems.remove(task);
                undoTaskItems.add(task);
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                undoDeleteSnack.show();
                setDeleteSnackbarText();



            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive)
            {
                View itemView = viewHolder.itemView;

                if (viewHolder.getAdapterPosition() == -1) return;

                if (!initiated) init();

                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),
                        itemView.getRight(), itemView.getBottom());
                background.draw(c);

                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = deleteIcon.getIntrinsicWidth();
                int intrinsicHeight = deleteIcon.getIntrinsicHeight();

                int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
                int deleteIconRight = itemView.getRight() - deleteIconMargin;
                int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int deleteIconBottom = deleteIconTop + intrinsicHeight;
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);

                deleteIcon.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(rv);
    }

    public void updateWidget()
    {
        Intent intent = new Intent(this, ToDoListWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ToDoListWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);
    }

    private void setDeleteSnackbarText()
    {
        if (undoTaskItems.size() == 1)
        {
            undoDeleteSnack.setText(R.string.one_task_deleted);
        }
        else
        {
            undoDeleteSnack.setText(String.format(getString(R.string.multiple_tasks_deleted),
                                                            undoTaskItems.size()));
        }
    }

    private void checkIfToShowEmptyListView() {
        if(taskItems.size() == 0) emptylist.setVisibility(View.VISIBLE);
        else emptylist.setVisibility(View.GONE);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_new_category) createAddEditCategoryDialogue(null);
        else if (id == R.id.edit_categories) createChooseCategoryDialogue();
        else
        {
            navigationView.setCheckedItem(item.getItemId());
            setCurrentCategory(item.getTitle().toString());
            updateView();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createAddEditCategoryDialogue(final String oldCategory)
    {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View dialogueView = li.inflate(R.layout.add_edit_category_dialogue, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(dialogueView);

        final EditText newCategoryName = (EditText) dialogueView.findViewById(R.id.edit_dialog_input);
        final TextView dialogTitle = (TextView) dialogueView.findViewById(R.id.add_category_dialog_message);

        if (oldCategory != null){
            newCategoryName.setText(oldCategory);
            dialogTitle.setText(R.string.edit_category_dialog_title);
        }

        alertDialogBuilder.setCancelable(true)
                .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                        String categoryName = newCategoryName.getText().toString();
                        if (categoryName.trim().length() > 0)
                        {
                            if (oldCategory != null)
                            {
                                db.updateCategory(oldCategory, categoryName);
                                //categories.get(categories.indexOf(oldCategory))
                                  //      .replace(oldCategory, categoryName);
                                navigationView.getMenu().findItem(categories.indexOf(oldCategory))
                                        .setTitle(categoryName);
                                categories = db.getAllCategories();
                                setCurrentCategory(categoryName);
                                updateWidgetTitle(oldCategory, categoryName);
                            }
                            else
                            {
                                if (!categories.contains(categoryName))
                                {
                                    categories.add(categoryName);
                                    createCategoryEntry(categoryName);
                                    db.addCategory(categoryName);
                                    setCurrentCategory(categoryName);
                                    updateView();
                                    navigationView.setCheckedItem(categories.indexOf(0));//Somehow the right menu entry doesn't get selected without this!?
                                    navigationView.setCheckedItem(categories.indexOf(currentCategory));
                                }
                                else Toast.makeText(getApplicationContext(), getString(R.string.category_already_exists), Toast.LENGTH_LONG).show();

                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.empty_category_name_error),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                })

                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void createChooseCategoryDialogue()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);


        String[] categoryArray = new String[categories.size()];
        categoryArray = categories.toArray(categoryArray);

        alertDialogBuilder.setSingleChoiceItems(categoryArray, -1, null);

        alertDialogBuilder.setCancelable(true)
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        createCategoryDeleteConfirmDialogue(db.getAllCategories().get(position));
                    }
                })
                .setNegativeButton(getString(R.string.edit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position = ((AlertDialog)dialog).getListView().getCheckedItemPosition();

                        if(position > -1)
                        {
                            createAddEditCategoryDialogue(categories.get(position));
                        }

                    }
                })
                .setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void createCategoryDeleteConfirmDialogue(final String category)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setCancelable(true)
                .setTitle(getString(R.string.category_delete_confirm_dialogue))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(categories.size() > 1) {
                            db.deleteCategory(category);
                            final Menu menu = navigationView.getMenu();
                            menu.removeItem(categories.indexOf(category));
                            categories.remove(category);
                            Log.d("test", categories.get(0));
                            updateWidgetTitle(category, categories.get(0));
                            setCurrentCategory(categories.get(0));
                            updateView();
                        }
                        else
                        {Toast.makeText(getApplicationContext(), getString(R.string.delete_last_category_error), Toast.LENGTH_LONG).show();}
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void createCategoryEntry(String categoryName)
    {
        final Menu menu = navigationView.getMenu();
        menu.add(R.id.nav_view_items, categories.indexOf(categoryName), Menu.FLAG_APPEND_TO_GROUP, categoryName);
        menu.findItem(categories.indexOf(categoryName)).setCheckable(true);
    }


    private void setUpNavView()
    {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        for (String s : categories)
        {
            createCategoryEntry(s);
        }
    }

    private void setUpSnackbar()
    {
        undoDeleteSnack = Snackbar
                .make(findViewById(R.id.coordinatorlayout), "", Snackbar.LENGTH_LONG)
                .setAction(R.string.undo_delete, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Toast.makeText(getApplicationContext(), getString(R.string.undo_pressed), Toast.LENGTH_LONG).show();
                        for (Task t : undoTaskItems)
                        {
                            taskItems.add(t);
                        }
                        adapter.sort();
                        adapter.notifyItemInserted(0);//fixes strange
                        adapter.notifyItemRangeChanged(0, taskItems.size());
                        undoTaskItems.clear();
                    }
                });
        undoDeleteSnack.setActionTextColor(Color.YELLOW);
        undoDeleteSnack.addCallback(new Snackbar.Callback()
        {

            @Override
            public void onDismissed(Snackbar snackbar, int event)
            {
                for (Task t : undoTaskItems)
                {
                    db.deleteTask(t.getId());
                    updateWidget();
                    checkIfToShowEmptyListView();
                }
                undoTaskItems.clear();
            }
        });
    }

    public void updateView()
    {
        taskItems = db.getTasksByCategory(currentCategory);
        adapter = new ToDoListRecyclerAdapter(taskItems, getApplicationContext());
        rv.setAdapter(adapter);
        adapter.sort();

        checkIfToShowEmptyListView();
    }

    private void setCurrentCategory(String currentCategory)
    {
        if (categories.contains(currentCategory) == true)
        {
            this.currentCategory = currentCategory;
        }
        else this.currentCategory = categories.get(0);
        settings.edit().putString(Settings.CURRENT_CATEGORY, currentCategory).apply();
        navigationView.setCheckedItem(categories.indexOf(currentCategory));
        setTitle(currentCategory);
    }

    private void updateWidgetTitle(String oldCategory, String newCategory)
    {
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ToDoListWidget.class));

        for(int id : ids)
        {
            if (settings.getString(Settings.WIDGET_CATEGORY+String.valueOf(id), "fail").equals(oldCategory))
            {
                settings.edit().putString(Settings.WIDGET_CATEGORY+String.valueOf(id), newCategory).apply();
            }
        }
    }
}


