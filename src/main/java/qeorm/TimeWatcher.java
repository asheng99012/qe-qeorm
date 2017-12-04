package qeorm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by ashen on 2017-3-4.
 */
public class TimeWatcher {
    private static Logger logger = LoggerFactory.getLogger(TimeWatcher.class);

    public static void watch(String label, Action action) {
        Long start = new Date().getTime();
        action.apply();
        logger.info("[耗时]：{} : {}", label, new Date().getTime() - start);
    }
}
