package com.korbi.todolist.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.korbi.todolist.todolist.R;

public class AboutTheApp extends AppCompatActivity {

    TextView appversion;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_the_app);

        appversion = findViewById(R.id.App_Version);

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (pInfo == null) appversion.setText("Version 1.1.3" );
        else appversion.setText("Version " +pInfo.versionName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void rateMe(View v)
    {
        try
        {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + this.getPackageName())));
        }
        catch (android.content.ActivityNotFoundException e)
        {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" +
                            this.getPackageName())));
        }
    }

    public void showOtherApps(View v)
    {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://developer?id=Korbinian+Moser")));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/developer?id=Korbinian+Moser")));
        }
    }
}
