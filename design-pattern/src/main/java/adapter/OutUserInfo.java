package adapter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhaoyancheng
 * @date 2021-05-20 07:51
 **/
public class OutUserInfo {

    public Map getUserInfo() {
        Map<String, String> map = new HashMap();
        map.put("name", "赵舞");
        map.put("phone", "187901212");
        return map;
    }
}
