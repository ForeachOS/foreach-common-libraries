package com.foreach.test;

import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.stubbing.InvocationContainer;
import org.mockito.internal.util.MockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

// Intellij has the bad idea to create TestClasses if you use AnnotationConfigContextLoaderDecorator
// So we'll just extend DelegatingSmartContextLoader like SpringJUnit4ClassRunner does

/**
 * Somebody will write a short howto here.
 */
public class MockedLoader extends DelegatingSmartContextLoader {

    private AnnotationConfigContextLoaderDecorator loader = new AnnotationConfigContextLoaderDecorator();
    private static final Logger LOG = LoggerFactory.getLogger( MockedLoader.class );

    @Override
    public ApplicationContext loadContext( MergedContextConfiguration mergedConfig ) throws Exception {
        // Overwrite the annotationConfigLoader with out implementation
        ReflectionTestUtils.setField( this, "annotationConfigLoader", loader );
        return super.loadContext( mergedConfig );
    }

    @Override
    public void processContextConfiguration( ContextConfigurationAttributes configAttributes ) {
        Class<?>[] classes = configAttributes.getClasses();
        if ( classes.length == 0 ) {
            // Allow empty loaders, in this case everything should be mocked
            configAttributes.setClasses( new Class<?>[]{Object.class} );
        }
        super.processContextConfiguration( configAttributes );
    }

    private class AnnotationConfigContextLoaderDecorator extends AnnotationConfigContextLoader {

        @Override
        protected void prepareContext( GenericApplicationContext context ) {
            QualifierAnnotationAutowireCandidateResolver qualifierAnnotationAutowireCandidateResolver = new QualifierAnnotationAutowireCandidateResolver();
            BeanFactoryDecorator beanFactoryDecorator = new BeanFactoryDecorator();
            beanFactoryDecorator.setAutowireCandidateResolver( qualifierAnnotationAutowireCandidateResolver );
            // Override the beanFactory with our custom implementation
            ReflectionTestUtils.setField( context, "beanFactory", beanFactoryDecorator );
            super.prepareContext( context );
        }
    }

    private class BeanFactoryDecorator extends DefaultListableBeanFactory {

        private final Map<Class, Object> mockedBeans = new HashMap<Class, Object>();
        //        private final Map<Class, Object> usedBeans = new HashMap<Class, Object>();

        //        public BeanFactoryDecorator() {
        //            for( Map.Entry<Class, Object> bean : usedBeans.entrySet() ) {
        //                reset( bean.getValue() );
        //            }
        //        }

        @Override
        public void destroySingletons() {
            super.destroySingletons();
            int mockedBeansWithoutInvocations = 0;
            int mockedBeansWithInvocations = 0;
            MockUtil mockUtil = new MockUtil();
            for ( Map.Entry<Class, Object> entry : mockedBeans.entrySet() ) {
                Object mock = entry.getValue();
                InvocationContainer container = mockUtil.getMockHandler( mock ).getInvocationContainer();
                List<Invocation> invocations = container.getInvocations();
                if ( invocations.isEmpty() ) {
                    mockedBeansWithoutInvocations++;
                } else {
                    mockedBeansWithInvocations++;
                }
                reset( mock );
            }
            System.out.println( "*** MockedLoader stats: [" + mockedBeans.size() + "] mocked beans of which [" + mockedBeansWithInvocations + "] with invocations and [" + mockedBeansWithoutInvocations + "] without invocations" );
            mockedBeans.clear();

        }

        @Override
        public Object resolveDependency( DependencyDescriptor descriptor, String beanName,
                                         Set<String> autowiredBeanNames,
                                         TypeConverter typeConverter ) throws BeansException {
            try {
                return super.resolveDependency( descriptor, beanName, autowiredBeanNames, typeConverter );
            } catch ( NoSuchBeanDefinitionException noSuchBeanDefinitionException ) {

                Class<?> dependencyType = descriptor.getDependencyType();
                if ( !dependencyType.isInterface() ) {
                    throw new NoSuchBeanDefinitionException( dependencyType, "Cannot create an automatic for for a non-interface for type: " + dependencyType );
                }

                Object mockedBean = mockedBeans.get( dependencyType );
                if ( mockedBean == null ) {
                    LOG.debug( "Did not find a mocked bean for type {}", dependencyType );
                    mockedBean = mock( dependencyType );

                    // We could actually also try to instantiate the Impl if we feel the
                    mockedBeans.put( dependencyType, mockedBean );
                } else {
                    //                    usedBeans.put( dependencyType, mockedBean );
                    LOG.debug( "returning mocked bean for type {}", dependencyType );
                }
                return mockedBean;
            }
        }
    }
}
