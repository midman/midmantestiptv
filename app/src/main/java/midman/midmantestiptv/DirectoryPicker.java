package midman.midmantestiptv;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import midman.midmantestiptv.adapter.DirListAdapter;
import midman.midmantestiptv.adapter.holder.DirItemHolder;

public class DirectoryPicker extends ListActivity {

    public static final String START_DIR = "startDir";
    public static final String ONLY_DIRS = "onlyDirs";
    public static final String SHOW_HIDDEN = "showHidden";
    public static final String CHOSEN_DIRECTORY = "chosenDir";
    public static final int PICK_DIRECTORY = 1099;
    private File dir;
    private boolean showHidden = false;
    private boolean onlyDirs = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Bundle extras = getIntent().getExtras();
        dir = Environment.getExternalStorageDirectory();
        if (extras != null) {
            //SharedPreferences mySharedPreferences = MainActivity.instance.getPref();
            String preferredStartDir = extras.getString(START_DIR);
            showHidden = extras.getBoolean(SHOW_HIDDEN, false);
            onlyDirs = extras.getBoolean(ONLY_DIRS, true);
            if (preferredStartDir != null) {
                File startDir = new File(preferredStartDir);
                if (startDir.isDirectory()) {
                    dir = startDir;
                }
            }
        }

        setContentView(R.layout.activity_directory_picker);
        setTitle(dir.getAbsolutePath());
        Button btnChoose = findViewById(R.id.btnChoose);
        TextView tvDir = findViewById(R.id.tvDirRuntime);
        String name = dir.getName();
        if (name.length() == 0)
            name = "/";
        //String btnTxt = getString(R.string.choose) + " [ " + name + " ]";
        tvDir.setText(dir.getAbsolutePath());
        //String btnTxt = getString(R.string.choose) + " [ " + dir.getAbsolutePath() + " ]";
        //btnChoose.setText(btnTxt);
        btnChoose.setOnClickListener(v -> returnDir(dir.getAbsolutePath()));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        if (!dir.canRead()) {
            Context context = getApplicationContext();
            String msg = "Could not read folder contents.";
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        final ArrayList<File> files = filterAndOrder(dir.listFiles(), onlyDirs, showHidden);
        DirItemHolder[] names = names(dir.getAbsolutePath(), files, onlyDirs, showHidden);
        setListAdapter(new DirListAdapter(this, R.layout.item_dir_pick, names));

        lv.setOnItemClickListener((parent, view, position, id) -> {
            if (!files.get(position).isDirectory())
                return;
            String path = files.get(position).getAbsolutePath();
            Intent intent = new Intent(DirectoryPicker.this, DirectoryPicker.class);
            intent.putExtra(DirectoryPicker.START_DIR, path);
            intent.putExtra(DirectoryPicker.SHOW_HIDDEN, showHidden);
            intent.putExtra(DirectoryPicker.ONLY_DIRS, onlyDirs);
            startActivityForResult(intent, PICK_DIRECTORY);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_DIRECTORY && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            String path = (String) extras.get(DirectoryPicker.CHOSEN_DIRECTORY);
            returnDir(path);
        }
    }

    private void returnDir(String path) {
        //String base = Environment.getExternalStorageDirectory().getAbsolutePath();
        //if (path.contains(base)) {
            //path = path.substring(base.length(), path.length());
        //}
        Intent result = new Intent();
        result.putExtra(CHOSEN_DIRECTORY, path);
        setResult(RESULT_OK, result);
        Log.d("opengappsdownloader", "chose: " + path);

        Context context = getApplicationContext();
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEdit = mySharedPreferences.edit();
        prefEdit.remove("prefDirectory");
        prefEdit.putString("prefDirectory", path);
        prefEdit.apply();
        finish();
    }

    public ArrayList<File> filterAndOrder(File[] file_list, boolean onlyDirs, boolean showHidden) {
        ArrayList<File> files = new ArrayList<>();
        ArrayList<File> listDir = new ArrayList<File>();
        ArrayList<File> listFile = new ArrayList<File>();
        for (File file : file_list) {

            if (!showHidden && file.isHidden())
                continue;

            if (file.isDirectory()) {
                listDir.add(file);
            } else if (!onlyDirs) {
                listFile.add(file);
            }

            //files.add(file);
        }

        for (File dir : listDir) {
            files.add(dir);
        }
        for (File f : listFile) {
            files.add(f);
        }
        return files;
    }

    public DirItemHolder[] names(String path, ArrayList<File> files, boolean onlyDirs, boolean showHidden) {
        DirItemHolder[] names = new DirItemHolder[files.size()];
        int i = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                names[i] = new DirItemHolder(path, file.getName(), false);
            } else if (!onlyDirs) {
                names[i] = new DirItemHolder(path, file.getName(), true);
            }
            i++;
        }
        return names;
    }
}
