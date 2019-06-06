package midman.midmantestiptv.m3u;

public abstract class Channel {

    private static int name_channel_progressive = 1;

    private static final int INIT_LETTER = 1;

    public abstract String getDuration();

    public abstract String getTvgId();

    public  abstract String getTvgName();

    public  abstract String getTvgLogoUrl();

    public  abstract String getGroupTitle();

    public  abstract String getTitle();

    public abstract String getUrl();

    public String getInitLetterName(int numLetter) {
        if (getTvgName()!=null && getTvgName().length()>numLetter)
            return getTvgName().substring(0, numLetter);
        else {
            return "A";
        }
    }
}
