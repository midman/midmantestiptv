package midman.midmantestiptv.adapter;

public class FileItem {

    String path;
    String name;
    int position;
    Boolean check;

    public FileItem(){}

    public FileItem(String name, String path, int position){
        this.name = name;
        this.path=path;
        this.position=position;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getPosition() {
        return position;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
