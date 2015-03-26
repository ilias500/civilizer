package com.civilizer.test.util;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.civilizer.config.AppOptions;
import com.civilizer.config.Configurator;

public final class TestUtil {

    private static final Log log = newLogger();
    private static final Random random = newRandomGenerator();
    
    private static Log newLogger() {
    	String log4jPropName = "log4j-test.properties";
    	URL url = ClassLoader.getSystemClassLoader().getResource(log4jPropName);
    	assertNotNull(url);
    	String log4jPropPath = url.getPath();
    	assertTrue(log4jPropPath != null && log4jPropPath.isEmpty() == false);
    	PropertyConfigurator.configure(log4jPropPath);

    	return newLogger(TestUtil.class);
    }
    
    private static Random newRandomGenerator() {
		Calendar cal = Calendar.getInstance();
        assertNotNull(cal);

//        final long seed = 1421048938148L;
        final long seed = cal.getTimeInMillis();
        
        log.info("random seed = " + seed);

        Random random = new Random(seed);
        assertNotNull(random);

        return random;
	}
    
    public static Log newLogger(Class<?> clazz) {
    	Log output = LogFactory.getLog(clazz);
        assertNotNull(output);    	
    	return output;
    }

    public static Random getRandom() {
        assertNotNull(random);
        return random;
    }

    public static String randomString(Random r, int minCount, int maxCount) {
        minCount = Math.max(Math.min(minCount, maxCount), 1);
        maxCount = Math.max(minCount, maxCount);
        final int stringCount = minCount + r.nextInt(maxCount - minCount);
        char[] s = new char[stringCount];

        for (int i = 0; i < stringCount; ++i) {
            s[i] = (char) (32 + r.nextInt(127 - 32));
        }

        return new String(s);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj, Class<T> type) {
        assertNotNull(obj);
        assertSame(obj.getClass().getName(), type.getName());
        return (T) obj;
    }

    public static <T> void checkIfNoDuplicateExistsIn(Collection<T> coll) {
    	if (coll instanceof Set == false) {
    		Set<T> s = new HashSet<T>(coll);
    		assertEquals(s.size(), coll.size());
    	}
    }
    
    public static void configure() {
    	final String path = System.getProperty("user.dir") + "/test/private-home";
    	System.setProperty(AppOptions.PRIVATE_HOME_PATH, path);
    	new Configurator();
    }

    public static void unconfigure() {
    	System.clearProperty(AppOptions.PRIVATE_HOME_PATH);
    }
    
    public static String getFilesHomePath() {
    	return System.getProperty(AppOptions.PRIVATE_HOME_PATH) + File.separatorChar + "files";
    }
}