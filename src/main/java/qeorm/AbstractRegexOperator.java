package qeorm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by asheng on 2015/6/18 0018.
 */
public abstract class AbstractRegexOperator {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public abstract String getPattern();

    public abstract String exec(Matcher m);
}
