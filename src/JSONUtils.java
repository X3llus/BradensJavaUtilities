import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

public class JSONUtils {
    /**
     * parses a json string to a hashmap
     *
     * @param json json string
     * @return hashmap from json
     */
    static public HashMap<String, Object> jsonToMap(String json) {
//        This snippet of code is from Toon Borgers on Stack Overflow https://stackoverflow.com/a/21720953
        return new Gson().fromJson(
                json, new TypeToken<HashMap<String, Object>>() {}.getType()
        );
    }
}
