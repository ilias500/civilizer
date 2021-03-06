package com.civilizer.config;

import java.util.*;
import java.io.*;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.civilizer.utils.FsUtil;

public final class Configurator {
	
    private final Logger logger = LoggerFactory.getLogger(Configurator.class);
	
	public Configurator() {
	    final File privateHome = detectPrivateHome(AppOptions.DEF_PRIVATE_HOME_PATH);
        preSetupPrivateHome(privateHome);
		addAppOptionsToSystemProperties(privateHome);
		postSetupPrivateHome(privateHome);
	}

	public Configurator(String defaultPrivateHomeName) {
	    final File privateHome = detectPrivateHome(defaultPrivateHomeName);
        preSetupPrivateHome(privateHome);
        addAppOptionsToSystemProperties(privateHome);
        postSetupPrivateHome(privateHome);
	}
    
	private static boolean equals(Properties p, String optionKey, String optionValue, boolean caseSensitive) {
	    String v = p.getProperty(optionKey);
	    if (v == null)
	        return false;
	    if (! caseSensitive)
	        v = v.toLowerCase();
	    return v.equals(optionValue);
	}

	private static boolean isTrue(Properties p, String optionKey) {
	    String v = p.getProperty(optionKey);
	    if (v == null)
	        return false;
        v = v.toLowerCase();
	    return v.equals("true") || v.equals("yes") || v.equals("on");
	}

    public static boolean equals(String optionKey, String optionValue, boolean caseSensitive) {
        return equals(System.getProperties(), optionKey, optionValue, caseSensitive);
    }

    public static boolean isTrue(String optionKey) {
        return isTrue(System.getProperties(), optionKey);
    }
	
	public static String getDefaultPrivateHomePath(String defaultPrivateHomeName) {
	    return FsUtil.concatPath(System.getProperty("user.home"), defaultPrivateHomeName);
	}

    private static Locale getLocaleByName(String name) {
        if (name == null)
            name = "";
        name = name.toLowerCase();
        switch (name) {
        case "ja":
        case "jp":
            return Locale.JAPANESE;
        // case "ko":
        // case "kr":
        // return Locale.KOREAN;
        case "en":
            return Locale.ENGLISH;
        default:
            return null;
        }
    }

    public static Locale resolveLocale(Locale localeFromClient) {
        // A locale setting specified via application options (e.g. app-options.properties)
        final String localeFromAppOption = System.getProperty(AppOptions.LOCALE);
        Locale locale = getLocaleByName(localeFromAppOption);
        if (locale == null) {
            // Or we use a locale setting from client (e.g. cookie)
            assert localeFromAppOption != null;
            locale = localeFromClient;
        }
        System.setProperty(AppOptions.CUR_LOCALE, locale.getLanguage());
        return locale;
    }

    public static Locale getCurLocale() {
        Locale locale = getLocaleByName(System.getProperty(AppOptions.CUR_LOCALE));
        if (locale == null)
            locale = Locale.ENGLISH;
        return locale;
    }

	private File detectPrivateHome(String defaultPrivateHomeName) {
		// We use default private home path unless a path is provided at runtime

		final String defaultPrivateHomePath = getDefaultPrivateHomePath(defaultPrivateHomeName);
        final String privateHomePathByRuntimeArg = System.getProperty(AppOptions.PRIVATE_HOME_PATH);
        
        if (privateHomePathByRuntimeArg != null && !privateHomePathByRuntimeArg.isEmpty()) {
        	final File f = new File(privateHomePathByRuntimeArg);
        	if (! f.isAbsolute()) {
        		// if the private home path is relative, the final path gets determined by the context of the current user working directory.
        		// it might get troublesome unless it is intended by the user explicitly
        		logger.warn("The specified home path \"%s\" is not absolute! it's error-prone!", privateHomePathByRuntimeArg);
        	}
        	return f;
        }
        
        return new File(defaultPrivateHomePath);
	}
	
	private void preSetupPrivateHome(File privateHome) {
	    FsUtil.createUnexistingDirectory(privateHome);
	    
	    final String tgtOptionFilePath = FsUtil.concatPath(privateHome.getAbsolutePath(), AppOptions.OPTION_FILE_NAME);
	    final File tgtOptionFile = new File(tgtOptionFilePath);
	    if (! tgtOptionFile.exists()) {
	        // copy the default application option file unless it exists
	        final File defaultOptionFile = FsUtil.getResourceAsFile(getClass(), AppOptions.OPTION_FILE_NAME);
	        try {
                FileUtils.copyFile(defaultOptionFile, tgtOptionFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
	    }
    }
	
	private void postSetupPrivateHome(File privateHome) {
		final String fileBoxHome = System.getProperty(AppOptions.FILE_BOX_HOME);
		FsUtil.createUnexistingDirectory(new File(fileBoxHome));
	}
	
	private void overrideOptionValue(String k, Properties p) {
    	final String v = System.getProperty(k);
    	if (v != null && ! v.isEmpty()) {
    		p.setProperty(k, v);
    	}
	}
	
	private void validateUnsignedInt(Properties p, String option) throws NumberFormatException {
//	    Integer.parseUnsignedInt(p.getProperty(option)); // for Java 8
	    // Use the following up until we support Java 7
        final int value = Integer.parseInt(p.getProperty(option));
        if (value < 0)
            throw new NumberFormatException("Negative numbers are not allowed!");
	}

	private void setUnspecifiedOptionsWithDefaultValues(Properties p) {
	    if (p.getProperty(AppOptions.DB_FILE_PREFIX) == null) {
	        p.setProperty(AppOptions.DB_FILE_PREFIX, AppOptions.DEF_DB_FILE_PREFIX);
	    }

        if (p.getProperty(AppOptions.DB_FILE_SUFFIX) == null) {
            p.setProperty(AppOptions.DB_FILE_SUFFIX, AppOptions.DEF_DB_FILE_SUFFIX);
        }
		
        if (p.getProperty(AppOptions.FILE_BOX_HOME) == null) {
            p.setProperty(AppOptions.FILE_BOX_HOME, AppOptions.DEF_FILE_BOX_HOME);
        }

        if (p.getProperty(AppOptions.DEV) == null) {
			p.setProperty(AppOptions.DEV, AppOptions.DEF_DEV);
		}

		if (p.getProperty(AppOptions.INITIALIZE_DB) == null) {
		    p.setProperty(AppOptions.INITIALIZE_DB, AppOptions.DEF_INITIALIZE_DB);
		}


		if (p.getProperty(AppOptions.CUR_LOCALE) == null) {
	        p.setProperty(AppOptions.CUR_LOCALE, AppOptions.DEF_LOCALE);
		}

		if (p.getProperty(AppOptions.DATA_SCRIPTS) == null) {
		    p.setProperty(AppOptions.DATA_SCRIPTS, AppOptions.DEF_DATA_SCRIPTS);
		}

		if (p.getProperty(AppOptions.TEMP_PATH) == null) {
		    p.setProperty(AppOptions.TEMP_PATH, AppOptions.DEF_TEMP_PATH);
		}

		try {
		    validateUnsignedInt(p, AppOptions.REMINDER_INTERVAL);
		} catch (Exception e) {
		    p.setProperty(AppOptions.REMINDER_INTERVAL, AppOptions.DEF_REMINDER_INTERVAL);
		}

	    try {
	        validateUnsignedInt(p, AppOptions.ITEMS_PER_PAGE);
        } catch (Exception e) {
            p.setProperty(AppOptions.ITEMS_PER_PAGE, AppOptions.DEF_ITEMS_PER_PAGE);
        }

	    if (p.getProperty(AppOptions.CLEAN_START) == null) {
	        p.setProperty(AppOptions.CLEAN_START, AppOptions.DEF_CLEAN_START);
	    }
	}
	
	private void setConstrainedOptions(Properties p) {
	    if (! isTrue(p, AppOptions.DEV)) {
            // [NOTE] 'database initialization' is available only for a development mode
            p.setProperty(AppOptions.INITIALIZE_DB, AppOptions.DEF_INITIALIZE_DB);
        }
	    if (isTrue(p, AppOptions.CLEAN_START)) {
	        // [NOTE] CLEAN_START is for starting the app with a clean empty DB (without help documentation)
	        // This option is so dangerous that it should not be specified via option files
	        p.setProperty(AppOptions.INITIALIZE_DB, "true");
	        p.setProperty(AppOptions.DATA_SCRIPTS, "");
	    }
	}
	
	private void addAppOptionsToSystemProperties(File privateHome) {
		try {
		    // load options from the application option file
		    final Properties p = new Properties();
		    final String optionFilePath = privateHome.getAbsolutePath() + FsUtil.SEP + AppOptions.OPTION_FILE_NAME;
		    p.load(new FileInputStream(optionFilePath));
		    
		    p.setProperty(AppOptions.PRIVATE_HOME_PATH, FsUtil.normalizePath(privateHome.getAbsolutePath()));
		    
		    final boolean override =
		            isTrue(p, AppOptions.OVERRIDE_OPTION_FILE) || isTrue(AppOptions.OVERRIDE_OPTION_FILE);
		    if (override) {
		    	// override some options with the corresponding system properties if any
		    	overrideOptionValue(AppOptions.DB_FILE_PREFIX, p);
		    	overrideOptionValue(AppOptions.FILE_BOX_HOME, p);
		    	overrideOptionValue(AppOptions.CLEAN_START, p);
		    }

		    setUnspecifiedOptionsWithDefaultValues(p);
			
		    // make sure the database file prefix is an absolute path
		    setPathAbsolute(p, AppOptions.DB_FILE_PREFIX, privateHome);
			
			// make sure the file box folder path is absolute
			setPathAbsolute(p, AppOptions.FILE_BOX_HOME, privateHome);

			// make sure the temporary folder path is absolute
			setPathAbsolute(p, AppOptions.TEMP_PATH, privateHome);
			
			// as a rule, some options are constrained with another option's value.
			// they may be reset under some condition no matter how they have been set by the user.
			setConstrainedOptions(p);
			
			// add the application options into the system properties
			// and then, we can access the options via SpEL (i.g. "#{systemProperties['key']}")
			// [NOTE] do not use System.setProperties() method to operate the same thing;
			// It will cause "systemProperties" SpEL expression not to work properly
			Enumeration<Object> keys = p.keys();
			while (keys.hasMoreElements()) {
			    final String k = keys.nextElement().toString();
			    final String v = p.getProperty(k);
			    System.setProperty(k, v);
			    logger.info("SYS PROP : {}, {}", k, v);
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void setPathAbsolute(Properties p, String key, File privateHome) {
	    // [NOTE] java.util.Properties.load(InputStream inStream) is assumed to use the ISO 8859-1 character encoding;
	    // So we need additional character encoding conversion in preparation for
	    // users writing arbitrary file paths containing UTF-8 characters. 
	    // See how FsUtil.toUtf8Path() does that.
	    // Currently, we don't support other encodings that are neither of UTF-8 or ISO 8859-1.
		final String srcPath = FsUtil.toUtf8Path(p.getProperty(key));
		final String absPath = FsUtil.getAbsolutePath(srcPath, privateHome.getAbsolutePath());
		p.setProperty(key, absPath);
	}
	
}
