package midman.midmantestiptv.util;

import java.util.HashMap;
import java.util.Map;

public class MMSession {

    private static MMSession instance;
    public Map<String, Object> map = new HashMap<String, Object>();

    private MMSession() {
    }

    public static MMSession getInstance(){
        if (instance==null){
            instance = new MMSession();
        }
        return instance;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public Object getValue(String key) {
        return map.get(key);
    }

}
