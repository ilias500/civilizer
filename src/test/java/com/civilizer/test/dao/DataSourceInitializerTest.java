package com.civilizer.test.dao;

import static org.junit.Assert.*;

import org.junit.*;

import com.civilizer.config.AppOptions;
import com.civilizer.dao.DataSourceInitializer;

public class DataSourceInitializerTest extends DaoTest {

	@Before
	public void setUp() throws Exception {
		DaoTest.setUpBeforeClass(
				"classpath:datasource-context-h2-empty.xml"
				, SearchTest.class
				);
		super.setUp();
	}
    
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        DaoTest.tearDownAfterClass();
    }

	@Test
	public void testRunSqlScriptProgrammatically() {
		assertEquals(0, fragmentDao.countAll(true));
		assertEquals(true, tagDao.findAllWithChildren(false).isEmpty());
		
		runSqlScript("db_test/test-data.sql");
		
		assertEquals(false, tagDao.findAllWithChildren(false).isEmpty());
	}

	@Test
	public void testOverwriteData() {
		runSqlScript("db_test/test-data.sql");
		assertEquals(false, fragmentDao.findAll(true).isEmpty());

		runSqlScript("db_test/drop.sql", "db_test/schema.sql");
		assertEquals(true, fragmentDao.findAll(true).isEmpty());
		
		runSqlScript("db_test/test-data.sql");		
		assertEquals(false, fragmentDao.findAll(true).isEmpty());
	}
	
	@Test
    public void testInjectDataSourceInitializer() {
	    final DataSourceInitializer dsi = ctx.getBean("dataSourceInitializer", DataSourceInitializer.class);
	    assertNotNull(dsi);
	    assertNotNull(dsi.getDataSource());
	    assertEquals(false, dsi.getInitializingScripts().isEmpty());
    }

	@Test
	public void testKickInitializingDataSource() throws Exception {
	    // emulate database reset by the user
	    tearDown();
	    
	    final String option = AppOptions.INITIALIZE_DB;
	    System.setProperty(option, "true");
	    
	    setUp();
	    
	    System.clearProperty(option);
	}

	@Test
	public void testFirstRunOfApp() throws Exception {
	    // emulate the 1st run of the application
	    tearDown();
        
	    final String option = "civilizer.no_schema";
        System.setProperty(option, "true");
        
        setUp();
        
        System.clearProperty(option);
	}
	
	@Test
    public void testDataScriptsViaSystemProperty() throws Exception {
	    // make sure DATA_SCRIPTS have no effect without database reset
        tearDown();        
        System.setProperty(AppOptions.DATA_SCRIPTS, ",db_test/test-data.sql,");
        setUp();
        assertEquals(true, fragmentDao.findAll(true).isEmpty());
        
        // make sure DATA_SCRIPTS work as expected
        tearDown();
        System.setProperty("civilizer.no_schema", "true");
//        System.setProperty(AppOptions.INITIALIZE_DB, "true");
        System.setProperty(AppOptions.DATA_SCRIPTS, ",db_test/test-data.sql,");
        setUp();
        assertEquals(false, fragmentDao.findAll(true).isEmpty());
        
        System.clearProperty("civilizer.no_schema");
        System.clearProperty(AppOptions.DATA_SCRIPTS);
        System.clearProperty(AppOptions.INITIALIZE_DB);
    }

}
