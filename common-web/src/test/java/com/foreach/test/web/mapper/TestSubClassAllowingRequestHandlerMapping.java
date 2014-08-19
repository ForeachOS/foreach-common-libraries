package com.foreach.test.web.mapper;

import com.foreach.web.mapper.SubClassAllowingRequestHandlerMapping;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.web.servlet.HandlerExecutionChain;

import javax.management.AttributeList;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSubClassAllowingRequestHandlerMapping {

    private Mapper mapper;

    @Before
    public void setup(){
        mapper = new Mapper();
    }


    @Test
    public void testRegisterHandler_simpleCase() throws Exception {
        Object handler = new Object();
        String url = "/simple";
        mapper.registerHandler( url, handler );

        HandlerExecutionChain actual = ( HandlerExecutionChain ) mapper.lookupHandler( url, null );
        Assert.assertSame(handler, actual.getHandler() );
    }

    @Test
    public void testRegisterHandler_subClassFirst() throws Exception {
        List handler1 = new ArrayList();
        List handler2 = new AttributeList();

        String url = "/simple";
        mapper.registerHandler( url, handler2 );
        mapper.registerHandler( url, handler1);

        HandlerExecutionChain actual = ( HandlerExecutionChain ) mapper.lookupHandler( url, null );
        Assert.assertSame(handler2, actual.getHandler() );
    }

    @Test
    public void testRegisterHandler_subClassSecond() throws Exception {
        List handler1 = new ArrayList();
        List handler2 = new AttributeList();

        String url = "/simple";
        mapper.registerHandler( url, handler1);
        mapper.registerHandler( url, handler2 );

        HandlerExecutionChain actual = ( HandlerExecutionChain ) mapper.lookupHandler( url, null );
        Assert.assertSame(handler2, actual.getHandler() );
    }

    @Test
    public void testRegisterHandler_rootPath_SubClassFirst() throws Exception {
        List handler1 = new ArrayList();
        List handler2 = new AttributeList();

        String url = "/";
        mapper.registerHandler( url, handler2 );
        mapper.registerHandler( url, handler1);

        Assert.assertSame(handler2, mapper.getRootHandler() );
    }

    @Test
    public void testRegisterHandler_rootPath_simpleSubClassSecond() throws Exception {
        List handler1 = new ArrayList();
        List handler2 = new AttributeList();

        String url = "/";
        mapper.registerHandler( url, handler1);
        mapper.registerHandler( url, handler2 );

        Assert.assertSame(handler2, mapper.getRootHandler() );
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterHandler_noSubClass() throws Exception {
        List handler1 = new ArrayList();
        Map handler2 = new HashMap();
        String url = "/simple";
        mapper.registerHandler( url, handler1);
        mapper.registerHandler( url, handler2 );
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterHandler_rootPathNoSubClass() throws Exception {
        List handler1 = new ArrayList();
        HashMap handler2 = new HashMap();
        String url = "/";
        mapper.registerHandler( url, handler1);
        mapper.registerHandler( url, handler2 );
    }

    private class Mapper extends SubClassAllowingRequestHandlerMapping{
        public void registerHandler( String urlPath, Object handler ) throws BeansException, IllegalStateException{
            super.registerHandler( urlPath, handler );
        }

        public Object lookupHandler( String urlPath, HttpServletRequest request ) throws Exception  {
            return super.lookupHandler( urlPath, request );
        }
    }
}
