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

import org.mockito.internal.stubbing.InvocationContainer;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DelegatingSmartContextLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

// Intellij has the bad idea to create TestClasses if you use AnnotationConfigContextLoaderDecorator
// So we'll just extend DelegatingSmartContextLoader like SpringJUnit4ClassRunner does

/**
 * <p>MockedLoader can be used in unit tests to automatically create mocks for all autowired
 * beans that are not yet defined in the spring context.  To be used in combination with
 * TestConfig classes.  Example use:</p>
 * <pre>
 *  {@literal @}RunWith(SpringJUnit4ClassRunner.class)
 *  {@literal @}DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
 *  {@literal @}ContextConfiguration(classes = MyTest.TestConfig.class, loader = MockedLoader.class)
 *  public class MyTest {
 *      {@literal @}Autowired
 *      private ClassBeingTested classBeingTested;
 *      ...
 *
 *     {@literal @}Configuration
 *     public static class TestConfig {
 *       // Only the ClassBeingTested should be manually created with the actual implementation.
 *       // All autowired beans that the ClassBeingTested depends on, will be replaced by a mock
 *       // by the MockedLoader.  Only beans for which a specific implementation should be used
 *       // need to be specified in the TestConfig.
 *       {@literal @}Bean
 *       public ClassBeingTested classBeingTested() {
 *         return new ClassBeingTested();
 *       }
 *     }
 *  }
 * </pre>
 * <p><strong>Note:</strong> MockedLoader is based on Mockito mocking.</p>
 */
public class MockedLoader extends DelegatingSmartContextLoader {
    private static final Logger LOG = LoggerFactory.getLogger( MockedLoader.class );

    private AnnotationConfigContextLoaderDecorator loader = new AnnotationConfigContextLoaderDecorator();

    @SuppressWarnings("all")
    @Override
    public ApplicationContext loadContext( MergedContextConfiguration mergedConfig ) throws Exception {
        // Overwrite the annotationConfigLoader with out implementation
        ReflectionTestUtils.setField( this, "annotationConfigLoader", loader );
        return super.loadContext( mergedConfig );
    }

    @Override
    public void processContextConfiguration( ContextConfigurationAttributes configAttributes ) {
        Class<?>[] classes = configAttributes.getClasses();
        if( classes.length == 0 ) {
            // Allow empty loaders, in this case everything should be mocked
            configAttributes.setClasses( new Class<?>[]{Object.class} );
        }
        super.processContextConfiguration( configAttributes );
    }

    private class AnnotationConfigContextLoaderDecorator extends AnnotationConfigContextLoader {
        @Override
        protected void prepareContext( GenericApplicationContext context ) {
            QualifierAnnotationAutowireCandidateResolver qualifierAnnotationAutowireCandidateResolver =
                    new QualifierAnnotationAutowireCandidateResolver();
            BeanFactoryDecorator beanFactoryDecorator = new BeanFactoryDecorator();
            beanFactoryDecorator.setAutowireCandidateResolver( qualifierAnnotationAutowireCandidateResolver );
            // Override the beanFactory with our custom implementation
            ReflectionTestUtils.setField( context, "beanFactory", beanFactoryDecorator );
            super.prepareContext( context );
        }
    }

    private static class BeanFactoryDecorator extends DefaultListableBeanFactory {
        private final Map<Class, Object> mockedBeans = new HashMap<Class, Object>();

        @Override
        public void destroySingletons() {
            super.destroySingletons();
            int mockedBeansWithoutInvocations = 0;
            int mockedBeansWithInvocations = 0;
            MockUtil mockUtil = new MockUtil();
            for( Map.Entry<Class, Object> entry : mockedBeans.entrySet() ) {
                Object mock = entry.getValue();
                InvocationContainer container = mockUtil.getMockHandler( mock ).getInvocationContainer();
                List<Invocation> invocations = container.getInvocations();
                if( invocations.isEmpty() ) {
                    mockedBeansWithoutInvocations++;
                } else {
                    mockedBeansWithInvocations++;
                }
                reset( mock );
            }
            LOG.debug(
                    "*** MockedLoader stats: [" + mockedBeans.size() + "] mocked beans of which [" + mockedBeansWithInvocations + "] with invocations and [" + mockedBeansWithoutInvocations + "] without invocations" );
            mockedBeans.clear();

        }

        @Override
        public Object resolveDependency( DependencyDescriptor descriptor,
                                         String beanName,
                                         Set<String> autowiredBeanNames,
                                         TypeConverter typeConverter ) {
            try {
                return super.resolveDependency( descriptor, beanName, autowiredBeanNames, typeConverter );
            } catch ( NoSuchBeanDefinitionException noSuchBeanDefinitionException ) {

                Class<?> dependencyType = descriptor.getDependencyType();

                if( Modifier.isFinal( dependencyType.getModifiers() ) ) {
                    throw new NoSuchBeanDefinitionException( dependencyType,
                            "Cannot create an automatic mock for final type: " + dependencyType );
                }

                Object mockedBean = mockedBeans.get( dependencyType );
                if( mockedBean == null ) {
                    LOG.debug( "Did not find a mocked bean for type {}", dependencyType );
                    mockedBean = mock( dependencyType );

                    // We could actually also try to instantiate the Impl if we feel the need
                    mockedBeans.put( dependencyType, mockedBean );
                } else {
                    LOG.debug( "returning mocked bean for type {}", dependencyType );
                }
                return mockedBean;
            }
        }
    }
}
