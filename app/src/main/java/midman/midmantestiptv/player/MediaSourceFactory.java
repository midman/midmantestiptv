package midman.midmantestiptv.player;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.mp4.FragmentedMp4Extractor;
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.DefaultHlsExtractorFactory;
import com.google.android.exoplayer2.source.hls.HlsExtractorFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

public class MediaSourceFactory {

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static String userAgent = "ExoPlayer";

    public static MediaSource getMediaSourceFor(Context context, Uri mediaUri) {
        return getMediaSourceFor(context, mediaUri, null);
    }

    public static MediaSource getMediaSourceFor(Context context, Uri uri,
                                                String overrideExtension) {

        userAgent = Util.getUserAgent(context, "MidmanTV");

        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ?
                "." + overrideExtension
                : uri.getLastPathSegment());
        Handler mainHandler = new Handler();

        switch (type) {
            case C.TYPE_SS:
                return buildSsMediaSource(context, uri);
            case C.TYPE_DASH:
                return buildDashMediaSource(context, uri);
            case C.TYPE_HLS:
                return buildHLsMediaSource(uri);
            case C.TYPE_OTHER:
                return buildDefaultMediaSource(uri);
            default: {
                throw new NotMediaException("Unsupported type: " + type);
            }
        }
    }

    public static MediaSource buildDefaultMediaSource(Uri uri) {

        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        //((DefaultExtractorsFactory) extractorsFactory).setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS);
        ((DefaultExtractorsFactory) extractorsFactory).setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES);
        ((DefaultExtractorsFactory) extractorsFactory).setMp4ExtractorFlags(Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS);
        ((DefaultExtractorsFactory) extractorsFactory).setFragmentedMp4ExtractorFlags(FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS);

        MediaSource ms = new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(userAgent))
                .setExtractorsFactory(extractorsFactory)
                .createMediaSource(uri);

        return ms;
    }

    private static MediaSource buildHLsMediaSource(Uri uri) {

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        //DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(), Util.getUserAgent(getActivity(), "Exo2"), defaultBandwidthMeter);
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
        DefaultHlsDataSourceFactory hlsDataSourceFactory = new DefaultHlsDataSourceFactory(httpDataSourceFactory);
        // Produces Extractor instances for parsing the media data.
        HlsExtractorFactory extractorsFactory = new DefaultHlsExtractorFactory();

        // This is the MediaSource representing the media to be played.
        HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(hlsDataSourceFactory)
                .setExtractorFactory(extractorsFactory)
                .createMediaSource(uri);

        return hlsMediaSource;
    }

    private static SsMediaSource buildSsMediaSource(Context context, Uri uri) {
        return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(getDataSource(context)), new DefaultDataSourceFactory(context, BANDWIDTH_METER,
                getDataSource(context))).createMediaSource(uri);
    }

    private static DashMediaSource buildDashMediaSource(Context context, Uri uri){
        return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(getDataSource(context)), new DefaultDataSourceFactory(context, BANDWIDTH_METER,
                getDataSource(context))).createMediaSource(uri);
    }

    private static DataSource.Factory getDataSource(Context context){
        return buildDataSourceFactory(context, false);
    }

    public static class NotMediaException extends IllegalArgumentException {
        public NotMediaException(String s) {
            super(s);
        }
    }

    private static DataSource.Factory buildDataSourceFactory(Context context, boolean useBandwidthMeter) {
        return buildDataSourceFactory(context, useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private static DataSource.Factory buildDataSourceFactory(Context context, DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(context, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    private static HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }
}
