/*
 * Copyright 2019 Korbinian Moser
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.korbi.todolist.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.korbi.todolist.database.ExportImportHelper;
import com.korbi.todolist.todolist.R;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final String CURRENT_CATEGORY = "currentcategory";
    public static final String PREVIOUS_PRIORITY = "previouspriority"; //for add edit activity to get last priority used
    public static final String WIDGET_CATEGORY = "widgetcategory";

    public static ExportImportHelper csvHelper;
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(String.valueOf(newValue));

                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(String.valueOf(newValue));
            }
            return true;
        }

    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MainPreferenceFragement()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MainPreferenceFragement extends PreferenceFragment {
        // creates the data corrupted Toast for the case that an invalid file is chosen for Importing
        public static void createImportErrorToast(Context context, String errorMessage) {

        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            csvHelper = new ExportImportHelper(getActivity());

            bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_default_priority_key)));

            Preference export_tasks = findPreference(getString(R.string.settings_backup_save_key));
            export_tasks.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    DirectoryChooserDialog directoryChooserDialog =
                            new DirectoryChooserDialog(getActivity(),
                                    new DirectoryChooserDialog.ChosenDirectoryListener() {
                                        @Override
                                        public void onChosenDir(String chosenDir) {
                                            csvHelper.writeCSV(chosenDir);
                                        }
                                    });
                    directoryChooserDialog.setNewFolderEnabled(true);
                    directoryChooserDialog.chooseDirectory();
                    return false;
                }
            });

            Preference import_tasks = findPreference(getString(R.string.settings_backup_load_key));
            import_tasks.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    DirectoryChooserDialog directoryChooserDialog =
                            new DirectoryChooserDialog(getActivity(),
                                    new DirectoryChooserDialog.ChosenDirectoryListener() {
                                        @Override
                                        public void onChosenDir(String chosenDir) {
                                            //check if file is a csv file
                                            if (chosenDir.substring(chosenDir.length() - 3).equals("csv")) {
                                                csvHelper.readCSV(chosenDir);
                                            } else
                                                Toast.makeText(getActivity(), getString(R.string.no_csv_file_error), Toast.LENGTH_LONG).show();
                                        }
                                    });
                    directoryChooserDialog.setNewFolderEnabled(false);
                    directoryChooserDialog.chooseDirectory();
                    return false;
                }
            });

            Preference resetDatabase = findPreference(getString(R.string.settings_backup_delete_key));
            resetDatabase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    AlertDialog dialog;
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setTitle(R.string.settings_backup_delete_message);
                    dialogBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.db.deleteAllTasks();
                            MainActivity.db.deleteAllCategories();
                            MainActivity.db.addCategory("MyTasks");
                        }
                    });

                    dialog = dialogBuilder.create();
                    dialog.show();

                    return false;
                }
            });

            Preference aboutApp = findPreference(getString(R.string.settings_about_key));
            aboutApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent intent = new Intent(getActivity(), AboutTheApp.class);
                    startActivity(intent);
                    return false;
                }
            });
        }
    }
}

