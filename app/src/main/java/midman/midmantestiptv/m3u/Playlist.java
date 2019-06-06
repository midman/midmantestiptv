package midman.midmantestiptv.m3u;

public class Playlist {

    private static final String KEY_ARGS_URL = "url.streaming.m3u";
    private static final String KEY_ARGS_LISTNAME = "list.name.streaming.m3u";

    private ChannelList playlist;

    public static Playlist newInstance(ChannelList _pList) {
        Playlist f = new Playlist();
        f.setPlaylist(_pList);

        return f;
    }

    private void setPlaylist(ChannelList _pList) {
        this.playlist=_pList;
    }

    public ChannelList getPlaylist() {
        return playlist;
    }
}
