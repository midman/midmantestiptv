package midman.midmantestiptv.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.content.CursorLoader;
import android.util.TypedValue;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class MMUtil {

    public static FileLoad getFileFromURI(Context context, Uri uri) {
        //String[] proj = { MediaStore.Images.Media.DATA };

        //This method was deprecated in API level 11
        //Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        FileLoad fileL = new FileLoad();
        if (uri.getScheme().equals("content")) {
            CursorLoader cursorLoader = null;
            Cursor cursor = null;
            try {
                cursorLoader = new CursorLoader(context, uri, null, null, null, null);
                cursor = cursorLoader.loadInBackground();
                int index_path = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int index_name = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cursor.moveToFirst();
                fileL.setPath(cursor.getString(index_path));
                fileL.setName(cursor.getString(index_name));
            } finally {
                if (cursor!=null) cursor.close();
            }
        }

        if (fileL.getPath()==null || "".equals(fileL.getPath().trim()))
            return null;

        return fileL;
    }

    public static int convertDpToPixel(int dp, Context context) {
        //return dp * ((int) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int convertPixelsToDp(int px, Context context) {
        //return px / ((int) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics());
    }

    public static String getStringFromResource(Context context, int id) {
        return context.getString(id);
    }

    public static Drawable getDrawableFromResource(Context context, int id) {
        return context.getResources().getDrawable(id);
    }

    public static int getColorFromResource(Context context, int id) {
        return context.getResources().getColor(id);
    }

    public static Map<String, String> getValueParamFromUrl(URL url) {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        try {
            String query = url.getQuery();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        } catch (Exception e) {
            //Log.e("", e.getMessage());
        }
        return query_pairs;
    }

    public static File convertIsToFile(InputStream is, String fileName) throws IOException {

        byte[] buffer = new byte[is.available()];
        is.read(buffer);

        File targetFile = new File(fileName);
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
        return targetFile;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (manager != null) {
            activeNetwork = manager.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void disableTouch(Activity a){
        a.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void enableTouch(Activity a){
        a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
