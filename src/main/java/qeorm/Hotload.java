package qeorm;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ashen on 2017-6-20.
 */
public class Hotload {
    private static final String KEY_MD5 = "MD5";
    private static final Charset charset = Charset.forName("UTF-8");
    static Logger logger = LoggerFactory.getLogger(Hotload.class);
    private static Long beforeTime = System.currentTimeMillis();    // 上一次刷新时间
    private static Map<String, String> set = new HashMap<String, String>();
    private static MapperScanner mapperScanner;
    private static int interval = 0;

    public static void init(MapperScanner mapperScanner, int interval) {
        Hotload.mapperScanner = mapperScanner;
        Hotload.interval = interval;
        if (interval > 0) run();
    }

    /**
     * 是否可以加载资源文件
     *
     * @param resource
     * @return
     */
    public static boolean canReload(Resource resource) {
        if (mapperScanner == null) return true;
        String path = resource.toString();
        String md5str = getMd5(resource);
        if (set.containsKey(path)) {
            if (!set.get(path).equals(md5str)) {
                set.put(path, md5str);
                return true;
            }
            return false;
        } else {
            set.put(path, md5str);
            return true;
        }
    }

    private static String getMd5(Resource resource) {
        try {
            String content = Resources.toString(resource.getURL(), Charsets.UTF_8);
            return md5(content);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    private static String md5(String str) {
        MessageDigest messageDigest = null;
        byte[] byteArray = null;
        try {
            messageDigest = MessageDigest.getInstance(KEY_MD5);
            byteArray = messageDigest.digest(str.getBytes(charset));
            StringBuffer md5StrBuff = new StringBuffer();
            for (int i = 0; i < byteArray.length; i++) {
                if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
                else
                    md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
            return md5StrBuff.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    public static void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(interval * 1000);
                        logger.trace("重新加载  " + mapperScanner.mapperLocations);
                        SqlConfigManager.scan(StringUtils.tokenizeToStringArray(mapperScanner.mapperLocations, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }

            }
        }).start();
    }
}
