package midman.midmantestiptv.m3u.fedorenkoalex;


import midman.midmantestiptv.m3u.Channel;

/**
 * Created by fedor on 25.11.2016.
 */

public class M3UItem extends Channel {

    private String itemDuration;

    private String itemName;

    private String itemUrl;

    private String itemIcon;

    public String getItemIcon() {
        return itemIcon;
    }

    public void setItemIcon(String itemIcon) {
        this.itemIcon = itemIcon;
    }

    public String getItemDuration() {
        return itemDuration;
    }

    public void setItemDuration(String itemDuration) {
        this.itemDuration = itemDuration;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemUrl() {
        return itemUrl;
    }

    public void setItemUrl(String itemUrl) {
        this.itemUrl = itemUrl;
    }

    @Override
    public String getDuration() {
        return itemDuration;
    }

    @Override
    public String getTvgId() {
        return null;
    }

    @Override
    public String getTvgName() {
        return itemName;
    }

    @Override
    public String getTvgLogoUrl() {
        return itemIcon;
    }

    @Override
    public String getGroupTitle() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getUrl() {
        return itemUrl;
    }
}
