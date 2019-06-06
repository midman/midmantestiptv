package midman.midmantestiptv;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import midman.midmantestiptv.adapter.FileItem;
import midman.midmantestiptv.adapter.FileListAdapter;
import midman.midmantestiptv.listener.OnPlayerListener;
import midman.midmantestiptv.m3u.fedorenkoalex.M3UParser;
import midman.midmantestiptv.m3u.fedorenkoalex.M3UPlaylist;
import midman.midmantestiptv.player.PlayerException;
import midman.midmantestiptv.player.PlayerManager;
import midman.midmantestiptv.provider.MMTilProvider;

//import com.BoardiesITSolutions.FileDirectoryPicker.DirectoryPicker;

//import android.support.annotation.NonNull;

public class MainActivity extends AppCompatActivity implements OnPlayerListener {//, DirectoryChooserFragment.OnFragmentInteractionListener {

    final static int CHOOSE_FILE = 1;
    final static int CHOOSE_DIR = 2;
    final static int WRITE_TMPFILE_REQUEST_CODE = 43;
    private TextView mDirectoryTextView;
    String fileSelected = "";
    //private DirectoryChooserFragment mDialog;
    private TextView tvNumCompleted;
    private TextView tvNumTot;
    private TextView tvNumOK;
    private TextView tvNumKO;

    int numChannelTest = 1;
    int numOK = 0;
    int numKO = 0;
    PlayerManager pManager;
    ProgressBar progressBar;
    ProgressBar pbCircle;
    private View playerView;
    private boolean stopped = false;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView recyclerView;
    private FileListAdapter mAdapter;
    private List<FileItem> listaFile;
    private List<FileItem> listaFileOK;
    private List<FileItem> listaFileKO;
    private List<FileItem> listaRuntimeUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /**
         final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
         .newDirectoryName("DialogSample")
         .build();
         mDialog = DirectoryChooserFragment.newInstance(config);

         findViewById(R.id.btnChoose)
         .setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
        mDialog.show(getFragmentManager(), null);
        }
        });
         **/


        mDirectoryTextView = (TextView) findViewById(R.id.tvPath);
        tvNumTot = (TextView) findViewById(R.id.tvNumTot);
        tvNumOK = (TextView) findViewById(R.id.tvNumOK);
        tvNumKO = (TextView) findViewById(R.id.tvNumKO);
        tvNumCompleted = (TextView) findViewById(R.id.tvNumCompleted);

        progressBar = (ProgressBar) findViewById(R.id.pb);
        pbCircle = (ProgressBar) findViewById(R.id.pbCircle);

        mLayoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.rvListFile);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);
        playerView = findViewById(R.id.player_view);
        listaFile = new ArrayList<FileItem>();
        listaFileOK = new ArrayList<FileItem>();
        listaFileKO = new ArrayList<FileItem>();
        listaRuntimeUpdate = new ArrayList<FileItem>();

        pManager = getNewPlayer();

        findViewById(R.id.btnFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePath(CHOOSE_FILE);
            }
        });

        findViewById(R.id.tvPath).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePath(CHOOSE_DIR);
            }
        });

        findViewById(R.id.btnStartAndStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btnStartStop = (Button) v;
                String btn = (String) btnStartStop.getText();
                if (getString(R.string.start).equalsIgnoreCase(btn)) {
                    start();
                } else {
                    stop();
                }
            }
        });

        findViewById(R.id.btnStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopped = true;
                showToast("Stopping test!", true);
            }
        });

        tvNumTot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listaFile != null && listaFile.size() > 0) {
                    mAdapter.update(listaFile);
                }
            }
        });

        tvNumOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listaFileOK != null && listaFileOK.size() > 0) {
                    mAdapter.update(listaFileOK);
                }
            }
        });

        tvNumKO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listaFileKO != null && listaFileKO.size() > 0) {
                    mAdapter.update(listaFileKO);
                }
            }
        });

        checkPermission();
    }

    private void start() {

        stopped = false;
        if (mDirectoryTextView.getText() == null || "".equalsIgnoreCase(((String) mDirectoryTextView.getText()).trim())) {
            showErrorToast(getString(R.string.msgDirMandatory), true);
            return;
        }

        if (listaFile.size() > 0) {
            progressBar.setMax(listaFile.size());
            progressBar.setVisibility(View.INVISIBLE);
            pbCircle.setVisibility(View.VISIBLE);
            tvNumCompleted.setText("");
            numOK = 0;
            numKO = 0;
            tvNumTot.setText(String.valueOf(listaFile.size()));
            tvNumOK.setText(String.valueOf(numOK));
            tvNumKO.setText(String.valueOf(numKO));

            showToast("Starting test dir: " + mDirectoryTextView.getText(), true);
            if (listaRuntimeUpdate.size() > 0) {
                listaRuntimeUpdate.clear();
            }
            mAdapter.update(listaRuntimeUpdate);
            openStreaming(0);
        } else {
            showErrorToast(getString(R.string.msgNoFile), true);
        }
        ((Button) findViewById(R.id.btnStartAndStop)).setText(getString(R.string.stop));
        ((Button) findViewById(R.id.btnStartAndStop)).setBackground(getResources().getDrawable(R.color.stop));

    }

    private void stop() {
        stopped = true;
        showToast("Finishing...", true);
        ((Button) findViewById(R.id.btnStartAndStop)).setText(getString(R.string.start));
        ((Button) findViewById(R.id.btnStartAndStop)).setBackground(getResources().getDrawable(R.color.colorPrimary));

    }

    private PlayerManager getNewPlayer() {
        return PlayerManager.createNewPlayerManager(this, playerView, this);
    }

    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void choosePath(int choose) {

        Intent intent = new Intent(this, DirectoryPicker.class);
        intent.putExtra(DirectoryPicker.ONLY_DIRS, false);
        intent.putExtra(DirectoryPicker.SHOW_HIDDEN, true);
        intent.putExtra(DirectoryPicker.START_DIR, Environment.getExternalStorageDirectory().getAbsolutePath());
        startActivityForResult(intent, choose);

        /**
         Intent intent = new Intent(this, DirectoryPicker.class);
         startActivityForResult(intent, choose);
         **/

        /**
         final Intent chooserIntent = new Intent(this, DirectoryChooserActivity.class);

         final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
         .newDirectoryName("NewFolder")
         .allowReadOnlyDirectory(true)
         .allowNewDirectoryNameModification(true)
         .initialDirectory("/sdcard")
         .build();

         chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
         **/

        /*
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, choose);
        */
    }

    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CHOOSE_DIR && data != null) {

            Bundle extras = data.getExtras();
            String path = (String) extras.get(DirectoryPicker.CHOSEN_DIRECTORY);
            mDirectoryTextView.setText(path);
            setListFiles(path);

            /*
            String path = data.getStringExtra(DirectoryPicker.BUNDLE_CHOSEN_DIRECTORY);
            mDirectoryTextView.setText(path);
            setListFiles(path);
            */

            /**
             if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
             //handleDirectoryChoice(data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
             } else {
             // Nothing selected
             }
             **/

            /**
             Uri uri = data.getData();
             String path = DocumentsContract.getTreeDocumentId(uri).split(":")[1];
             mDirectoryTextView.setText(path);
             setListFiles(path);
             **/
        }

        if (requestCode == WRITE_TMPFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = data.getData();
            File fileIn = new File(mDirectoryTextView.getText() + File.separator + fileSelected);
            File fileOut = new File(uri.getPath());

            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(fileIn);
                os = new FileOutputStream(fileOut);

                try {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;

                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }

                    os.flush();
                } finally {
                    if (os!=null) os.close();
                    if (is!=null) is.close();
                }
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }

            String mime = getMimeType(uri.toString());

            Intent mapIntent = new Intent(Intent.ACTION_VIEW);
            mapIntent.setDataAndType(uri, mime);
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mapIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //mapIntent.setDataAndType(uri, getMimeType(mediaFile.getPath()));

            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, PackageManager.MATCH_DEFAULT_ONLY);
            boolean isIntentSafe = activities.size() > 0;

            if (isIntentSafe) {
                {
                    for (ResolveInfo info : activities) {
                        grantUriPermission(info.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    }

                    startActivity(mapIntent);
                }

            } else {
                showToast(getString(R.string.no_app_to_open_file), true);
            }

        }
    }

    private void checkPermission() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        101);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void setListFiles(String path) {

        File directory = new File(path);
        File[] files = directory.listFiles();

        //List<FileItem> list = new ArrayList<FileItem>();
        listaFile.clear();
        listaFileOK.clear();
        listaFileKO.clear();
        listaRuntimeUpdate.clear();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory())
                continue;

            String name = files[i].getName();
            int idx = name.lastIndexOf('.');
            if (idx > 0) {
                String extension = name.substring(name.lastIndexOf("."));
                if (".m3u".equalsIgnoreCase(extension) || ".m3u8".equalsIgnoreCase(extension)) {
                    FileItem f = new FileItem(files[i].getName(), files[i].getPath(), i);
                    listaFile.add(f);
                }
            }
        }
        mAdapter = new FileListAdapter(listaFile);
        recyclerView.setAdapter(mAdapter);
        //return list;
    }

    public void openStreaming(int position) {

        /**
         // Quando avvio uno streaming, verifico se ho inizializzato il Controller,
         // altrimenti lo inizializzo per poter utilizzare sicuramente i tasti Volume!
         if (controller==null){
         controller = new Controller(getActivity());
         }
         **/

        if (position == listaFile.size()) {
            stopped = true;
            pbCircle.setVisibility(View.INVISIBLE);
            stop();
            return;
        }

        FileItem item = listaFile.get(position);
        File f = new File(item.getPath());

        M3UParser parser = new M3UParser();

        try {
            M3UPlaylist playList = parser.parseFile(new FileInputStream(f));
            //ciclo sulla lista dei canali del file per un numero di canali da testare definito...
            for (int ch = 1; ch <= numChannelTest; ch++) {
                String url = playList.getPlaylistItems().get(ch).getUrl();
                if (pManager != null) {
                    try {
                        pManager.stop();
                        pManager.addUrlStreaming(url).start(position);
                    } catch (Exception e) {
                        e.printStackTrace();
                        PlayerException pe = new PlayerException(position, e.getMessage());
                        onPlayerException(pe);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            PlayerException pe = new PlayerException(position, e.getMessage());
            onPlayerException(pe);
            //showErrorToast(e.getMessage(), false);
        } catch (Exception ex) {
            ex.printStackTrace();
            PlayerException pe = new PlayerException(position, ex.getMessage());
            onPlayerException(pe);
        }
    }

    @Override
    public void playerInitialized() {

    }

    @Override
    public void playerRelease() {

    }

    @Override
    public void changePlayerState(int playerState) {

    }

    @Override
    public void onPlayerException(PlayerException e) {
        FileItem item = listaFile.get(e.getPosition());
        Log.e("MID_UPDATE", "position = " + e.getPosition() + "  ERROR! URL - " + item.getPath());
        item.setCheck(false);
        numKO = numKO + 1;
        listaFileKO.add(item);

        updateRuntime(item, e.getPosition());
    }

    @Override
    public void onPlayerOK(int position) {
        FileItem item = listaFile.get(position);
        Log.e("MID_UPDATE", "position = " + position + " **OK**! URL - " + item.getPath());
        item.setCheck(true);
        numOK = numOK + 1;
        listaFileOK.add(item);

        updateRuntime(item, position);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    ;


    /**
     * @Override public void onSelectDirectory(@NonNull String path) {
     * mDirectoryTextView.setText(path);
     * mDialog.dismiss();
     * }
     * @Override public void onCancelChooser() {
     * mDialog.dismiss();
     * }
     **/

    private void updateRuntime(FileItem item, int position) {

        pManager.stop();
        listaRuntimeUpdate.add(item);
        mLayoutManager.scrollToPosition(listaRuntimeUpdate.size()-1);
        mAdapter.update(listaRuntimeUpdate);

        int numElaborati = position + 1;
        //showToast("Elaborati " + numElaborati + " di " + listaFile.size(), false);
        updateNumElab(String.valueOf(numElaborati), String.valueOf(listaFile.size()));
        tvNumOK.setText(String.valueOf(numOK));
        tvNumKO.setText(String.valueOf(numKO));

        int next = position + 1;
        progressBar.setProgress(next);
        if (position <= 1) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (!stopped) {
            openStreaming(next);
        } else {
            pbCircle.setVisibility(View.INVISIBLE);
            stop();
        }
    }

    public void updateNumElab(String num, String tot) {
        tvNumCompleted.setText(num + "  di  " + tot);
    }

    public void showToast(final String message, final boolean length_long) {
        //Context context = getApplicationContext();
        runOnUiThread(new Runnable() {
            public void run() {
                CharSequence text = message;
                int duration = length_long ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();
            }
        });
    }

    public void showErrorToast(final String message, final boolean length_long) {
        //Context context = getApplicationContext();
        runOnUiThread(new Runnable() {
            public void run() {
                CharSequence text = "ERROR: " + message;
                int duration = length_long ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();
            }
        });
    }

    public void openAppToLoadURL(String fileName) {

        if (mDirectoryTextView.getText() != null && !"".equals(mDirectoryTextView.getText())) {


            try {

                // Get URI and MIME type of file
                //Uri uri = Uri.fromFile(new File(mDirectoryTextView.getText() + File.separator + fileName));
                String dirPath = (String) mDirectoryTextView.getText();
                String pathFile = dirPath + File.separator + fileName;
                File mediaFile = new File(pathFile);
                //File mediaTempFile = File.createTempFile(fileName, ".m3u", dir);

                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //uri = Uri.fromFile(mediaFile);
                    //uri = Uri.parse("file://" + pathFile);
                    //uri = Uri.withAppendedPath(Uri.parse("content://" + BuildConfig.APPLICATION_ID + ".midman.midmantestiptv.provider"),pathFile.substring(1));
                    uri = MMTilProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".midman.midmantestiptv.provider", mediaFile);
                } else {
                    uri = Uri.parse("file://" + pathFile);
                }
                String mime = getMimeType(uri.toString());

                Intent mapIntent = new Intent(Intent.ACTION_VIEW);
                mapIntent.setDataAndType(uri, mime);
                mapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mapIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //mapIntent.setDataAndType(uri, getMimeType(mediaFile.getPath()));

                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, PackageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size() > 0;

                if (isIntentSafe) {
                    {
                        for (ResolveInfo info : activities) {
                            grantUriPermission(info.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        }

                        startActivity(mapIntent);
                    }

                } else {
                    showToast(getString(R.string.no_app_to_open_file), true);
                }

                /**
                 String u = ((String)mDirectoryTextView.getText())+File.separator+fileName;
                 File f = new File(u);
                 Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName(), f);
                 Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
                 PackageManager packageManager = getPackageManager();
                 List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
                 boolean isIntentSafe = activities.size() > 0;

                 if (isIntentSafe) {
                 startActivity(mapIntent);
                 }
                 **/

            } catch (Exception e) {
                e.printStackTrace();
                showErrorToast(e.getMessage(), true);
            }
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void createTmpFile(String mimeType, String fileName) {
        Intent intent = new Intent(this, MainActivity.class);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, WRITE_TMPFILE_REQUEST_CODE);
    }
}
