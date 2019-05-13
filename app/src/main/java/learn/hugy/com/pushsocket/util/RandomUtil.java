package learn.hugy.com.pushsocket.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class RandomUtil {
    public static String greater(char flag, Context mContext, String key) {
        SharedPreferences sp = mContext.getSharedPreferences("ID_NUM", Context.MODE_PRIVATE);
        String result = sp.getString(key, "");
        if ("".equals(result)) {
            int ran = (int) (Math.random() * 900) + 100;
            result = flag + String.valueOf(ran) + getUuid();
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, result);
            editor.commit();
        }
        return result;
    }

    public static String[] chars = new String[]{"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};

    public static String getUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 16; i++) {
            String str = uuid.substring(i * 2, i * 2 + 2);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 36]);
        }
        return shortBuffer.toString();
    }
}
