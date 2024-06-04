/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.common.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.exceptions.misusing.NotAMockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD )
@ContextConfiguration( classes = TestMockedLoader.TestConfig.class, loader = MockedLoader.class )
public class TestMockedLoader {
    @Autowired
    private SomeBean someBean;

    @Autowired
    private MockedImplementation mocked;

    @Autowired
    private ActualImplementation actual;

    @Autowired
    private NonFinalClass nonFinalClass;

    @Test
    public void testClassIsAutowired() {
        assertNotNull( someBean );
        assertNotNull( mocked );
        assertNotNull( actual );
        assertNotNull( nonFinalClass );
        assertFalse( isMock( someBean ) );
        assertFalse( isMock( actual ) );
        assertTrue( isMock( mocked ) );
        assertTrue( isMock( nonFinalClass ) );
    }

    @Test
    public void beanIsAutowired() {
        assertNotNull( someBean );
        assertNotNull( someBean.actual );
        assertNotNull( someBean.mocked );
        assertNotNull( someBean.nonFinalClass );
        assertFalse( isMock( someBean.actual ) );
        assertTrue( isMock( someBean.mocked ) );
        assertTrue( isMock( someBean.nonFinalClass ) );
        assertSame( actual, someBean.actual );
        assertSame( mocked, someBean.mocked );
        assertSame( nonFinalClass, someBean.nonFinalClass );
    }

    @Test
    public void valuesFromMockAndActual() {
        when( mocked.value() ).thenReturn( "mocked value" );

        assertEquals( "actual", someBean.actual.value() );
        assertEquals( "mocked value", someBean.mocked.value() );
        assertEquals( null, someBean.nonFinalClass.value() );
    }

    private boolean isMock( Object o ) {
        try {
            reset( o );
            return true;
        } catch ( NotAMockException name ) {
            return false;
        }
    }

    @Configuration
    public static class TestConfig {
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
    public static class SomeBean {
        @Autowired
        ActualImplementation actual;

        @Autowired
        MockedImplementation mocked;

        @Autowired
        NonFinalClass nonFinalClass;
    }

    public interface MockedImplementation {
        String value();
    }

    public interface ActualImplementation {
        String value();
    }

    public static class ActualImplementationImpl implements ActualImplementation {
        public String value() {
            return "actual";
        }
    }

    public static class NonFinalClass {
        public String value() {
            return "non-final";
        }
    }
}
