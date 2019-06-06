package midman.midmantestiptv.player;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import java.io.IOException;

/**
 * Created by lviana on 26/02/18.
 */

public class EventLogger implements Player.EventListener,
        MetadataOutput, MediaSourceEventListener {

    private static final String TAG = "MIDMANTV_BUFFERING";

    private PlayerManager pManager;
    private int state = Player.STATE_ENDED;
    private boolean stoppedByBuffering = false;
    int positionItem;


    public EventLogger(PlayerManager _pManager, int _posizionItem) {
        pManager = _pManager;
        positionItem = _posizionItem;
    }

    @Override
    public void onMetadata(Metadata metadata) {
        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry entry = metadata.get(i);
            if (entry instanceof TextInformationFrame) {
                TextInformationFrame textInformationFrame = (TextInformationFrame) entry;
                Log.e(TAG, "onMetadata: [ "+ textInformationFrame.id +" - "+ textInformationFrame.value +"]");
            }
        }
    }

    @Override
    public void onMediaPeriodCreated(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
        Log.e(TAG, "onMediaPeriodCreated: [ "+ mediaPeriodId +" ]");
    }

    @Override
    public void onMediaPeriodReleased(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
        Log.e(TAG, "onMediaPeriodReleased: [ "+ mediaPeriodId +" ]");
    }

    @Override
    public void onLoadStarted(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
        Log.e(TAG, "onLoadStarted: [ "+ loadEventInfo.uri.toString() +" - "+ mediaPeriodId +" - " + mediaLoadData + " ]");
    }

    @Override
    public void onLoadCompleted(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
        Log.e(TAG, "onLoadCompleted: [ "+ loadEventInfo.uri.toString() +" - "+ mediaPeriodId +" - " + mediaLoadData + " ]");
    }

    @Override
    public void onLoadCanceled(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
        Log.e(TAG, "onLoadCanceled: [ "+ loadEventInfo.uri.toString() +" - "+ mediaPeriodId +" - " + mediaLoadData + " ]");
    }

    @Override
    public void onLoadError(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
        Log.e(TAG, "onLoadError: [ "+ loadEventInfo.uri.toString() +" - "+ mediaPeriodId +" - " + mediaLoadData + " ]");
    }

    @Override
    public void onReadingStarted(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
        Log.e(TAG, "onReadingStarted: [ "+ mediaPeriodId +" ]");
    }

    @Override
    public void onUpstreamDiscarded(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {
        Log.e(TAG, "onUpstreamDiscarded: [ "+ mediaPeriodId +" - " + mediaLoadData + " ]");
    }

    @Override
    public void onDownstreamFormatChanged(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {
        Log.e(TAG, "onDownstreamFormatChanged: [ "+ mediaPeriodId +" - " + mediaLoadData + " ]");
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
        Log.e(TAG, "onTimelineChanged");
        int stop = 0;
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.e(TAG, "onTracksChanged");
        int stop = 0;
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.e(TAG, "onLoadingChanged: [ "+ isLoading +" ]");

        //Nel caso in cui il player non e' in loading ma lo stato e' READY, allora significa che probabilmente e' andato in buffering
        //e quindi allo stato STATE_ENDED sara' ripreso il video dal punto di fermo con il metodo seek!
        if (isLoading==false && state==Player.STATE_READY)
            stoppedByBuffering=true;
        else
            stoppedByBuffering=false;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.e(TAG, "onPlayerStateChanged: [ "+ playWhenReady +", " + playbackState + " ]");
        pManager.changeState(playbackState);
        state = playbackState;
        pManager.setPlayWhenReadyState(playWhenReady);

        if (playWhenReady && playbackState == Player.STATE_READY) {

            //Il player e' in streaming e il video sta scorrendo...

            //mantieni schermo sempre acceso!
            pManager.setScreeOn(true);
            //pManager.setLiveStreaming(true);

        } else {
            //lo streaming e' fermo in pausa, o stop ...o spento completamente!

            pManager.setScreeOn(false);
            //pManager.setLiveStreaming(false);
        }

        switch(playbackState) {
            case Player.STATE_BUFFERING: //2
                Log.i(TAG, "STATE_BUFFERING " + Player.STATE_BUFFERING);
                //mantieni schermo sempre acceso!
                pManager.setScreeOn(true);
                pManager.setLiveStreaming(true);
                stoppedByBuffering=false;
                break;
            case Player.STATE_ENDED: //4
                Log.i(TAG, "STATE_ENDED " + Player.STATE_ENDED);
                if (stoppedByBuffering){
                    //mantieni schermo acceso!
                    pManager.setScreeOn(true);
                    pManager.setLiveStreaming(true);
                    pManager.seek(0);
                } else {
                    //spegni schermo secondo impostazioni!
                    pManager.setScreeOn(false);
                }
                break;
            case Player.STATE_IDLE: //1
                //spegni schermo secondo impostazioni!
                pManager.setScreeOn(false);
                pManager.setLiveStreaming(false);
                Log.i(TAG, "STATE_IDLE " + Player.STATE_IDLE);
                break;
            case Player.STATE_READY: //3
                //mantieni schermo sempre acceso!
                pManager.setScreeOn(true);
                pManager.setLiveStreaming(true);
                pManager.onPlayerReady(positionItem);
                Log.i(TAG, "STATE_READY " + Player.STATE_READY);
                break;
            default:
                break;
        }

        int stop = 0;
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        Log.e(TAG, "onRepeatModeChanged: [ "+ repeatMode +" ]");
        int stop = 0;
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        Log.e(TAG, "onShuffleModeEnabledChanged: [ "+ shuffleModeEnabled +" ]");
        int stop = 0;
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.e(TAG, "onPlayerError: [ "+ error.getMessage() +" ]");

        //pManager.stop();
        //pManager.release();
        pManager.onPlayerException(error, positionItem);
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        Log.e(TAG, "onPositionDiscontinuity: [ "+ reason +" ]");
        int stop = 0;
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        Log.e(TAG, "onPlaybackParametersChanged: [ "+ playbackParameters +" ]");

        int stop = 0;
    }

    @Override
    public void onSeekProcessed() {
        Log.e(TAG, "onSeekProcessed: ()");
        int stop = 0;
    }

}