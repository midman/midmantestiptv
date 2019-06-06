package midman.midmantestiptv.adapter.holder;

public class DirItemHolder {

    String path;
    String name;
    boolean file = false;

    public DirItemHolder(String path, String name, boolean file){
        this.path=path;
        this.name=name;
        this.file=file;
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

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }
}
