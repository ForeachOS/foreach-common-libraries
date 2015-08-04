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
package com.foreach.common.web.mapper;

import org.springframework.beans.BeansException;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * This is a partial copy from AbstractUrlHandlerMapping except for the registerHandler Method.
 * Compatible with Spring 3.1.1-RELEASE.
 *
 * Usage:
 * Initialize the class in the spring-dispatcher-servlet.xml
 * 	<bean class="com.foreach.web.mapper.SubClassAllowingRequestHandlerMapping"/>
 *
 * Note: the overwriting controller method must have the same signature as the parent!
 *
 * @deprecated because using this mapping would encourage having multiple singleton beans whereas only
 * one would be used.  Better solution is to ensure only one controller exists.
 */
@Deprecated
public class SubClassAllowingRequestHandlerMapping extends DefaultAnnotationHandlerMapping {
    private boolean lazyInitHandlers = false;

    private final Map<String, Object> handlerMap = new LinkedHashMap<String, Object>();

    /**
     * Set whether to lazily initialize handlers. Only applicable to
     * singleton handlers, as prototypes are always lazily initialized.
     * Default is "false", as eager initialization allows for more efficiency
     * through referencing the controller objects directly.
     * <p>If you want to allow your controllers to be lazily initialized,
     * make them "lazy-init" and set this flag to true. Just making them
     * "lazy-init" will not work, as they are initialized through the
     * references from the handler mapping in this case.
     */
    public void setLazyInitHandlers( boolean lazyInitHandlers ) {
        this.lazyInitHandlers = lazyInitHandlers;
    }

    /**
     * Look up a handler instance for the given URL path.
     * <p>Supports direct matches, e.g. a registered "/test" matches "/test",
     * and various Ant-style pattern matches, e.g. a registered "/t*" matches
     * both "/test" and "/team". For details, see the AntPathMatcher class.
     * <p>Looks for the most exact pattern, where most exact is defined as
     * the longest path pattern.
     *
     * @param urlPath URL the bean is mapped to
     * @param request current HTTP request (to expose the path within the mapping to)
     * @return the associated handler instance, or <code>null</code> if not found
     * @see #exposePathWithinMapping
     * @see org.springframework.util.AntPathMatcher
     */
    protected Object lookupHandler( String urlPath, HttpServletRequest request ) throws Exception {
        // Direct match?
        Object handler = this.handlerMap.get( urlPath );
        if( handler != null ) {
            // Bean name or resolved handler?
            if( handler instanceof String ) {
                String handlerName = ( String ) handler;
                handler = getApplicationContext().getBean( handlerName );
            }
            validateHandler( handler, request );
            return buildPathExposingHandler( handler, urlPath, urlPath, null );
        }
        // Pattern match?
        List<String> matchingPatterns = new ArrayList<String>();
        for( String registeredPattern : this.handlerMap.keySet() ) {
            if( getPathMatcher().match( registeredPattern, urlPath ) ) {
                matchingPatterns.add( registeredPattern );
            }
        }
        String bestPatternMatch = null;
        Comparator<String> patternComparator = getPathMatcher().getPatternComparator( urlPath );
        if( !matchingPatterns.isEmpty() ) {
            Collections.sort( matchingPatterns, patternComparator );
            if( logger.isDebugEnabled() ) {
                logger.debug( "Matching patterns for request [" + urlPath + "] are " + matchingPatterns );
            }
            bestPatternMatch = matchingPatterns.get( 0 );
        }
        if( bestPatternMatch != null ) {
            handler = this.handlerMap.get( bestPatternMatch );
            // Bean name or resolved handler?
            if( handler instanceof String ) {
                String handlerName = ( String ) handler;
                handler = getApplicationContext().getBean( handlerName );
            }
            validateHandler( handler, request );
            String pathWithinMapping = getPathMatcher().extractPathWithinPattern( bestPatternMatch, urlPath );

            // There might be multiple 'best patterns', let's make sure we have the correct URI template variables
            // for all of them
            Map<String, String> uriTemplateVariables = new LinkedHashMap<String, String>();
            for( String matchingPattern : matchingPatterns ) {
                if( patternComparator.compare( bestPatternMatch, matchingPattern ) == 0 ) {
                    uriTemplateVariables.putAll(
                            getPathMatcher().extractUriTemplateVariables( matchingPattern, urlPath ) );
                }
            }
            if( logger.isDebugEnabled() ) {
                logger.debug( "URI Template variables for request [" + urlPath + "] are " + uriTemplateVariables );
            }
            return buildPathExposingHandler( handler, bestPatternMatch, pathWithinMapping, uriTemplateVariables );
        }
        // No handler found...
        return null;
    }

    /**
     * Register the specified handler for the given URL path.
     *
     * @param urlPath the URL the bean should be mapped to
     * @param handler the handler instance or handler bean name String
     *                (a bean name will automatically be resolved into the corresponding handler bean)
     * @throws org.springframework.beans.BeansException
     *                               if the handler couldn't be registered
     * @throws IllegalStateException if there is a conflicting handler registered
     */
    protected void registerHandler( String urlPath, Object handler ) throws BeansException, IllegalStateException {
        Assert.notNull( urlPath, "URL path must not be null" );
        Assert.notNull( handler, "Handler object must not be null" );
        Object resolvedHandler = handler;

        // Eagerly resolve handler if referencing singleton via name.
        if( !this.lazyInitHandlers && handler instanceof String ) {
            String handlerName = ( String ) handler;
            if( getApplicationContext().isSingleton( handlerName ) ) {
                resolvedHandler = getApplicationContext().getBean( handlerName );
            }
        }

        Object mappedHandler = this.handlerMap.get( urlPath );
        if( mappedHandler != null ) {

            if( mappedHandler != resolvedHandler ) {
                // This is where the magic happens
                if( resolvedHandler.getClass().isAssignableFrom( mappedHandler.getClass() ) ) { // handler is superClass
                    return;
                } else if( !mappedHandler.getClass().isAssignableFrom( resolvedHandler.getClass() ) ) {
                    throw new IllegalStateException(
                            "Cannot map " + getHandlerDescription( resolvedHandler ) + " to URL path [" + urlPath +
                                    "]: There is already " + getHandlerDescription( mappedHandler ) + " mapped and it is not a subclass." );
                }
            }
        }

        if( urlPath.equals( "/" ) ) {
            // This is where the magic also happens
            if( getRootHandler() != null && resolvedHandler.getClass().isAssignableFrom(
                    getRootHandler().getClass() ) ) {
                return;
            } else if( getRootHandler() != null && !getRootHandler().getClass().isAssignableFrom(
                    resolvedHandler.getClass() ) ) {
                throw new IllegalStateException(
                        "Cannot map " + getHandlerDescription( resolvedHandler ) + " to URL path [" + urlPath +
                                "]: There is already " + getHandlerDescription( getRootHandler() ) + " mapped and it is not a subclass." );
            }
            if( logger.isInfoEnabled() ) {
                logger.info( "Root mapping to " + getHandlerDescription( resolvedHandler ) );
            }
            setRootHandler( resolvedHandler );
        } else if( urlPath.equals( "/*" ) ) {
            if( logger.isInfoEnabled() ) {
                logger.info( "Default mapping to " + getHandlerDescription( resolvedHandler ) );
            }
            setDefaultHandler( resolvedHandler );
        } else {
            this.handlerMap.put( urlPath, resolvedHandler );
            if( logger.isInfoEnabled() ) {
                logger.info( "Mapped URL path [" + urlPath + "] onto " + getHandlerDescription( resolvedHandler ) );
            }
        }
    }

    private String getHandlerDescription( Object handler ) {
        return "handler " + (handler instanceof String ? "'" + handler + "'" : "of type [" + handler.getClass() + "]");
    }
}
