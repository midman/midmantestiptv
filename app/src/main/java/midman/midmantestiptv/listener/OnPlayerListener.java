package midman.midmantestiptv.listener;


import midman.midmantestiptv.player.PlayerException;

public interface OnPlayerListener {

    public void playerInitialized();

    public void playerRelease();

    public void changePlayerState(int playerState);

    public void onPlayerException(PlayerException e);

    public void onPlayerOK(int position);
}
