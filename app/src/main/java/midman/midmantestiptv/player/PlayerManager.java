package midman.midmantestiptv.player;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

import midman.midmantestiptv.listener.OnPlayerListener;


public class PlayerManager {

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private static final Boolean DEFAULT_PLAYER = false;
    private static final Boolean DEFAULT_BUFFERVALUE = true;
    private static final Boolean DEFAULT_MEDIASOURCE = false;
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";
    public static final String DRM_SCHEME_EXTRA = "drm_scheme";
    public static final String DRM_LICENSE_URL_EXTRA = "drm_license_url";
    public static final String DRM_KEY_REQUEST_PROPERTIES_EXTRA = "drm_key_request_properties";
    public static final String DRM_MULTI_SESSION_EXTRA = "drm_multi_session";
    public static final String PREFER_EXTENSION_DECODERS_EXTRA = "prefer_extension_decoders";
    private static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";

    public static final String ABR_ALGORITHM_EXTRA = "abr_algorithm";
    public static final String ABR_ALGORITHM_DEFAULT = "default";
    public static final String ABR_ALGORITHM_RANDOM = "random";

    private Stream tStreaming;
    private boolean starting = false;
    private boolean stopBuffering = false;
    private boolean playWhenReadyState = false;

    private Context context;
    private PlayerView playerView;
    private String urlStreaming = "";
    //private ProgressBar spinner;
    private OnPlayerListener onPlayerListener;
    //private static PlayerManager instance;

    private boolean mPlayWhenReady;

    public static PlayerManager createNewPlayerManager(Context _context, View _playerView, OnPlayerListener _onPlayerListener) {
        PlayerManager pManager = new PlayerManager(_context, (PlayerView) _playerView, _onPlayerListener);
        pManager.init();
        return pManager;
    }

    private PlayerManager(Context _context, PlayerView _playerView, OnPlayerListener _onPlayerListener) {
        context = _context;
        playerView = _playerView;
        onPlayerListener = _onPlayerListener;
    }

    private void init() {
        tStreaming = new Stream(playerView, false);
        //spinner = ((Activity)context).findViewById(R.id.player_spinner_buffering);
        onPlayerListener.playerInitialized();
    }

    public PlayerManager addUrlStreaming(String _urlStreaming) {
        urlStreaming = _urlStreaming;
        return this;
    }

    public void start(int posizionItem) {
        if (urlStreaming==null || urlStreaming.isEmpty())
            return;

        stop();
        tStreaming.startStreaming(posizionItem);
        starting = true;
    }

    public void pause() {
        if (!starting)
            return;

        tStreaming.pauseStreaming();
        //starting = false;
    }

    public void stop() {
        if (!starting)
            return;

        tStreaming.stopStreaming();
        //starting = false;
    }

    public void restart(int positionItem) {
        if (!starting)
            return;
        stop();
        start(positionItem);
    }

    public void release() {
        tStreaming.releaseStreaming();

    }

    /*
    public void cleanInstance() {
        if (initialized)
            tStreaming.releaseStreaming();

        if (mPlayerFullscreen)
            closeFullscreenDialog();

        tStreaming=null;
        initialized = false;
    }
    */

    public void changeState(int playerState) {
        onPlayerListener.changePlayerState(playerState);
    }

    public void onPlayerException(ExoPlaybackException error, int positionItem) {
        PlayerException ex = new PlayerException(positionItem, error.getMessage());
        onPlayerListener.onPlayerException(ex);
    }

    public boolean isLiveStreaming() {
        return tStreaming.isLive();
    }

    protected void setLiveStreaming(boolean isLive) {
        tStreaming.setLive(isLive);
    }

    protected void onPlayerReady(int position) {
        onPlayerListener.onPlayerOK(position);
    }

    public boolean isStopBuffering() {
        return stopBuffering;
    }

    public void setStopBuffering(boolean _stopBuffering) {
        stopBuffering =_stopBuffering;
    }

    public String getUrlLoadStreaming() {
        if (!isLiveStreaming())
            return "";

        return urlStreaming;
    }

    public void seek(long position) {
        Log.i("MIDTV_BUFFERING", "seek");
        tStreaming.seekStreaming(position);
    }

    public void setScreeOn(boolean value){
        playerView.setKeepScreenOn(value);
    }

    public boolean isPlayWhenReadyState(){
        return playWhenReadyState;
    }

    public void setPlayWhenReadyState(boolean playWhenReadyState) {
        this.playWhenReadyState = playWhenReadyState;
    }

    protected void showMessage(String message){
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public class Stream {

        private SimpleExoPlayer player;
        //private PlayerLoadControl playerControl;
        private PlayerView tVideoView = null;

        private Cache downloadCache = null;
        private File downloadDirectory = null;
        private FrameworkMediaDrm mediaDrm;

        private boolean isLive;
        private boolean startInNewThread;

        public Stream(PlayerView _videoView, boolean _startInNewThread) {

            tVideoView = _videoView;
            startInNewThread = _startInNewThread;
        }

        public void startStreaming(int posizionItem) {

            player = DEFAULT_PLAYER ? createDefaultSimplePlayer() : createLiteSimplePlayer();

            //Autofocus nella gestione dell'audio tra piu' app contemporaneamente.
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build();

            //player.setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true);
            player.setVolume(0f);

            EventLogger eventLogger = new EventLogger(PlayerManager.this, posizionItem);
            player.addMetadataOutput(eventLogger);
            player.addListener(eventLogger);

            //tVideoView.setPlayer(player);

            player.seekTo(0, 0);
            player.setPlayWhenReady(true);
            playWhenReadyState = true;

            if(startInNewThread){
                goLiveInNewThread(posizionItem);
            } else {
                goLive(posizionItem);
            }
            ((Activity)context).overridePendingTransition(0, 0);
        }

        private SimpleExoPlayer createDefaultSimplePlayer(){

            SimpleExoPlayer sPlayer = ExoPlayerFactory.newSimpleInstance(context,
                    new DefaultRenderersFactory(context, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl());

            return sPlayer;

        }

        private SimpleExoPlayer createLiteSimplePlayer() {

            // 1. Create a default TrackSelector
            Handler mainHandler = new Handler();
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
            TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

            // 2. Create a default LoadControl
            LoadControl loadControl = new DefaultLoadControl();

            // 3. Create a default RenderersFactory
            RenderersFactory renderers = new DefaultRenderersFactory(context, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);

            // 4. Create the player
            SimpleExoPlayer sPlayer = ExoPlayerFactory.newSimpleInstance(context, renderers, trackSelector, loadControl, null, bandwidthMeter);

            return sPlayer;
        }

        public void pauseStreaming() {
            if (player != null && playWhenReadyState) {
                playWhenReadyState = false;
                player.setPlayWhenReady(false);
            }
            //isLive = true;
        }

        public void stopStreaming() {
            if (player!=null)
                player.stop();
            playWhenReadyState = false;
            player = null;
            //isLive = false;
        }

        public void seekStreaming(long position) {
            if (player!=null)
                player.seekTo(0);
        }

        public void releaseStreaming() {
            if (player!=null){
                player.stop();
                player.release();
            }
            player=null;
            playWhenReadyState = false;
            isLive = false;
        }

        public void restartBuffering(int positionItem) {
            stopStreaming();
            startStreaming(positionItem);
        }

        private void goLive(int posizionItem){
            int p = posizionItem;
            try {
                Uri uri = Uri.parse(urlStreaming);
                MediaSource mediaSource = DEFAULT_MEDIASOURCE ? buildDefaultMediaSource(uri) : buildDynamicMediaSource(uri); //buildAllMediaSource(uri, null);
                player.prepare(mediaSource, true, false);
            } catch (Exception e) {
                releaseStreaming();
                PlayerException ex = new PlayerException(p, e.getMessage());
                onPlayerListener.onPlayerException(ex);
            }
        }

        private void goLiveInNewThread(final int posizionItem) {
            new Thread() {
                public void run() {
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            goLive(posizionItem);
                        }
                    });
                }
            }.start();
        }

        private MediaSource buildDefaultMediaSource(Uri uri) {
            return MediaSourceFactory.buildDefaultMediaSource(uri);
        }


        private MediaSource buildDynamicMediaSource(Uri uri) {
            return MediaSourceFactory.getMediaSourceFor(context.getApplicationContext(), uri);
        }

        private synchronized Cache getDownloadCache() {
            if (downloadCache == null) {
                File downloadContentDirectory = new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY);
                downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
            }
            return downloadCache;
        }

        private File getDownloadDirectory() {
            if (downloadDirectory == null) {
                downloadDirectory = context.getExternalFilesDir(null);
                if (downloadDirectory == null) {
                    downloadDirectory = context.getFilesDir();
                }
            }
            return downloadDirectory;
        }

        private CacheDataSourceFactory buildReadOnlyCacheDataSource(
                DefaultDataSourceFactory upstreamFactory, Cache cache) {
            return new CacheDataSourceFactory(
                    cache,
                    upstreamFactory,
                    new FileDataSourceFactory(),
                    /* cacheWriteDataSinkFactory= */ null,
                    CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                    /* eventListener= */ null);
        }

        private int getUrlType(String url) {
            if (url.contains(".mpd")) {
                return C.TYPE_DASH;
            } else if (url.contains(".ism") || url.contains(".isml")) {
                return C.TYPE_SS;
            } else if (url.contains(".m3u8")) {
                return C.TYPE_HLS;
            } else {
                return C.TYPE_OTHER;
            }
        }

        public void goToBackground(){
            if(player != null){
                mPlayWhenReady = player.getPlayWhenReady();
                player.setPlayWhenReady(false);
            }
        }

        public void goToForeground(){
            if(player != null){
                player.setPlayWhenReady(mPlayWhenReady);
            }
        }

        public boolean isLive() {
            return isLive;
        }

        public void setLive(boolean _isLive){
            this.isLive = _isLive;
        }

    }
}
