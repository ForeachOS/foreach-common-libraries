package com.foreach.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.exceptions.misusing.NotAMockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestMockedLoader.TestConfig.class, loader = MockedLoader.class)
public class TestMockedLoader
{
	@Autowired
	private SomeBean someBean;

	@Autowired
	private MockedImplementation mocked;

	@Autowired
	private ActualImplementation actual;

	@Test
	public void testClassIsAutowired() {
		assertNotNull( someBean );
		assertNotNull( mocked );
		assertNotNull( actual );
		assertFalse( isMock( someBean ) );
		assertFalse( isMock( actual ) );
		assertTrue( isMock( mocked ) );
	}

	@Test
	public void beanIsAutowired() {
		assertNotNull( someBean );
		assertNotNull( someBean.actual );
		assertNotNull( someBean.mocked );
		assertFalse( isMock( someBean.actual ) );
		assertTrue( isMock( someBean.mocked) );
		assertSame( actual, someBean.actual );
		assertSame( mocked, someBean.mocked );
	}

	@Test
	public void valuesFromMockAndActual() {
		when( mocked.value() ).thenReturn( "mocked value" );

		assertEquals( "actual", someBean.actual.value() );
		assertEquals( "mocked value", someBean.mocked.value() );
	}

	private boolean isMock( Object o ) {
		try {
			reset( o );
			return true;
		}
		catch ( NotAMockException name ) {
			return false;
		}
	}

	@Configuration
	public static class TestConfig
	{
		@Bean
		public SomeBean someBean() {
			return new SomeBean();
		}

		@Bean
		public ActualImplementation actualImplementation() {
			return new ActualImplementationImpl();
		}
	}

	// Domain classes for test
	public static class SomeBean
	{
		@Autowired
		ActualImplementation actual;

		@Autowired
		MockedImplementation mocked;
	}

	public interface MockedImplementation
	{
		String value();
	}

	public interface ActualImplementation
	{
		String value();
	}

	public static class ActualImplementationImpl implements ActualImplementation
	{
		public String value() {
			return "actual";
		}
	}
}
