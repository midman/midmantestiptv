package midman.midmantestiptv.player;

public class PlayerException extends Exception {

    int position;
    String message;

    public PlayerException(int _position, String _message) {
        super();
        position=_position;
        message=_message;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
