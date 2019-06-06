package midman.midmantestiptv.player;

import android.util.Log;

import com.google.android.exoplayer2.video.VideoListener;

public class VideoPlayerEventListener implements VideoListener {

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

    }

    @Override
    public void onSurfaceSizeChanged(int width, int height) {
        Log.i("MIDTV_onSurfSizeChang", "width: " + width + " - height: " + height);
    }

    @Override
    public void onRenderedFirstFrame() {

    }
}
