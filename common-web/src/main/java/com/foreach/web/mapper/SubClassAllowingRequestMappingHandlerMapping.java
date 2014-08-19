package com.foreach.web.mapper;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Abstract base class for {@link org.springframework.web.servlet.HandlerMapping} implementations that define a
 * mapping between a request and a {@link org.springframework.web.method.HandlerMethod}.
 * <p/>
 * <p>For each registered handler method, a unique mapping is maintained with
 * subclasses defining the details of the mapping type {@code <RequestMappingInfo>}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 3.1
 */

public class SubClassAllowingRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private boolean detectHandlerMethodsInAncestorContexts = false;

    private final Map<RequestMappingInfo, HandlerMethod> handlerMethods =
            new LinkedHashMap<RequestMappingInfo, HandlerMethod>();

    private final MultiValueMap<String, RequestMappingInfo> urlMap =
            new LinkedMultiValueMap<String, RequestMappingInfo>();

    /**
     * Whether to detect handler methods in beans in ancestor ApplicationContexts.
     * <p>Default is "false": Only beans in the current ApplicationContext are
     * considered, i.e. only in the context that this HandlerMapping itself
     * is defined in (typically the current DispatcherServlet's context).
     * <p>Switch this flag on to detect handler beans in ancestor contexts
     * (typically the Spring root WebApplicationContext) as well.
     */
    public void setDetectHandlerMethodsInAncestorContexts( boolean detectHandlerMethodsInAncestorContexts ) {
        this.detectHandlerMethodsInAncestorContexts = detectHandlerMethodsInAncestorContexts;
    }

    /**
     * Return a map with all handler methods and their mappings.
     */
    public Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
        return Collections.unmodifiableMap( handlerMethods );
    }

    /**
     * ApplicationContext initialization and handler method detection.
     */
    @Override
    public void initApplicationContext() throws ApplicationContextException {
        super.initApplicationContext();
        initHandlerMethods();
    }

    /**
     * Scan beans in the ApplicationContext, detect and register handler methods.
     *
     * @see #isHandler(Class)
     * @see #getMappingForMethod(java.lang.reflect.Method, Class)
     * @see #handlerMethodsInitialized(java.util.Map)
     */
    protected void initHandlerMethods() {
        if( logger.isDebugEnabled() ) {
            logger.debug( "Looking for request mappings in application context: " + getApplicationContext() );
        }

        String[] beanNames =
                (this.detectHandlerMethodsInAncestorContexts ? BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                        getApplicationContext(), Object.class ) : getApplicationContext().getBeanNamesForType(
                        Object.class ));

        for( String beanName : beanNames ) {
            if( isHandler( getApplicationContext().getType( beanName ) ) ) {
                detectHandlerMethods( beanName );
            }
        }
        handlerMethodsInitialized( getHandlerMethods() );
    }

    /**
     * Register a handler method and its unique mapping.
     *
     * @param handler the bean name of the handler or the handler instance
     * @param method  the method to register
     * @param mapping the mapping conditions associated with the handler method
     * @throws IllegalStateException if another method was already registered
     *                               under the same mapping
     */
    @Override
    protected void registerHandlerMethod( Object handler, Method method, RequestMappingInfo mapping ) {
        HandlerMethod handlerMethod;
        if( handler instanceof String ) {
            String beanName = ( String ) handler;
            handlerMethod = new HandlerMethod( beanName, getApplicationContext(), method );
        } else {
            handlerMethod = new HandlerMethod( handler, method );
        }

        HandlerMethod oldHandlerMethod = handlerMethods.get( mapping );
        if( oldHandlerMethod != null ) {
            if( oldHandlerMethod.equals( handlerMethod ) || handlerMethod.getBeanType().isAssignableFrom( oldHandlerMethod.getBeanType() ) ) {
                return;
            } else if( !oldHandlerMethod.getBeanType().isAssignableFrom( handlerMethod.getBeanType() ) ) {
                throw new IllegalStateException(
                        "Ambiguous mapping found. Cannot map '" + handlerMethod.getBean() + "' bean method \n" + handlerMethod + "\nto " +
                                mapping + ": There is already '" + oldHandlerMethod.getBean() + "' bean method\n" + oldHandlerMethod + " mapped and one is not a subclass of the other." );
            } else {
                Set<String> patterns = getMappingPathPatterns( mapping );
                for( String pattern : patterns ) {
                    if( !getPathMatcher().isPattern( pattern ) && urlMap.containsKey( pattern ) ) {
                        urlMap.remove( pattern );
                    }
                }
            }
        }

        handlerMethods.put( mapping, handlerMethod );
        if( logger.isInfoEnabled() ) {
            logger.info( "Mapped \"" + mapping + "\" onto " + handlerMethod );
        }

        Set<String> patterns = getMappingPathPatterns( mapping );
        for( String pattern : patterns ) {
            if( !getPathMatcher().isPattern( pattern ) ) {
                urlMap.add( pattern, mapping );
            }
        }
    }

    /**
     * Look up a handler method for the given request.
     */
    @Override
    protected HandlerMethod getHandlerInternal( HttpServletRequest request ) throws Exception {
        String lookupPath = getUrlPathHelper().getLookupPathForRequest( request );
        if( logger.isDebugEnabled() ) {
            logger.debug( "Looking up handler method for path " + lookupPath );
        }

        HandlerMethod handlerMethod = lookupHandlerMethod( lookupPath, request );

        if( logger.isDebugEnabled() ) {
            if( handlerMethod != null ) {
                logger.debug( "Returning handler method [" + handlerMethod + "]" );
            } else {
                logger.debug( "Did not find handler method for [" + lookupPath + "]" );
            }
        }

        return (handlerMethod != null) ? handlerMethod.createWithResolvedBean() : null;
    }

    /**
     * Look up the best-matching handler method for the current request.
     * If multiple matches are found, the best match is selected.
     *
     * @param lookupPath mapping lookup path within the current servlet mapping
     * @param request    the current request
     * @return the best-matching handler method, or {@code null} if no match
     * @see #handleMatch(Object, String, javax.servlet.http.HttpServletRequest)
     * @see #handleNoMatch(java.util.Set, String, javax.servlet.http.HttpServletRequest)
     */
    protected HandlerMethod lookupHandlerMethod( String lookupPath, HttpServletRequest request ) throws Exception {
        List<Match> matches = new ArrayList<Match>();

        List<RequestMappingInfo> directPathMatches = this.urlMap.get( lookupPath );
        if( directPathMatches != null ) {
            addMatchingMappings( directPathMatches, matches, request );
        }

        if( matches.isEmpty() ) {
            // No choice but to go through all mappings
            addMatchingMappings( this.handlerMethods.keySet(), matches, request );
        }

        if( !matches.isEmpty() ) {
            Comparator<Match> comparator = new MatchComparator( getMappingComparator( request ) );
            Collections.sort( matches, comparator );

            if( logger.isTraceEnabled() ) {
                logger.trace(
                        "Found " + matches.size() + " matching mapping(s) for [" + lookupPath + "] : " + matches );
            }

            Match bestMatch = matches.get( 0 );
            if( matches.size() > 1 ) {
                Match secondBestMatch = matches.get( 1 );
                if( comparator.compare( bestMatch, secondBestMatch ) == 0 ) {
                    Method m1 = bestMatch.handlerMethod.getMethod();
                    Method m2 = secondBestMatch.handlerMethod.getMethod();
                    throw new IllegalStateException(
                            "Ambiguous handler methods mapped for HTTP path '" + request.getRequestURL() + "': {" +
                                    m1 + ", " + m2 + "}" );
                }
            }

            handleMatch( bestMatch.mapping, lookupPath, request );
            return bestMatch.handlerMethod;
        } else {
            return handleNoMatch( handlerMethods.keySet(), lookupPath, request );
        }
    }

    private void addMatchingMappings(
            Collection<RequestMappingInfo> mappings,
            List<Match> matches,
            HttpServletRequest request ) {
        for( RequestMappingInfo mapping : mappings ) {
            RequestMappingInfo match = getMatchingMapping( mapping, request );
            if( match != null ) {
                matches.add( new Match( match, handlerMethods.get( mapping ) ) );
            }
        }
    }

    /**
     * A temporary container for a mapping matched to a request.
     */
    private class Match {

        private final RequestMappingInfo mapping;

        private final HandlerMethod handlerMethod;

        private Match( RequestMappingInfo mapping, HandlerMethod handlerMethod ) {
            this.mapping = mapping;
            this.handlerMethod = handlerMethod;
        }

        @Override
        public String toString() {
            return mapping.toString();
        }
    }

    private class MatchComparator implements Comparator<Match> {

        private final Comparator<RequestMappingInfo> comparator;

        public MatchComparator( Comparator<RequestMappingInfo> comparator ) {
            this.comparator = comparator;
        }

        public int compare( Match match1, Match match2 ) {
            return comparator.compare( match1.mapping, match2.mapping );
        }
    }

}