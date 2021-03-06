/*
 * Copyright (C) 2013 Fabien Vauchelles (fabien_AT_vauchelles_DOT_com).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3, 29 June 2007, of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.vaushell.superpipes.tools.scribe.fb;

import com.vaushell.superpipes.dispatch.ConfigProperties;
import com.vaushell.superpipes.dispatch.Dispatcher;
import com.vaushell.superpipes.tools.scribe.code.VC_FileFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import org.apache.commons.configuration.XMLConfiguration;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test.
 *
 * @see FacebookClient
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public class FacebookClientPageTest
{
    // PUBLIC
    public FacebookClientPageTest()
    {
        this.dispatcher = new Dispatcher();
        this.client = new FacebookClient();
    }

    /**
     * Initialize the test.
     *
     * @throws Exception
     */
    @BeforeClass
    public void setUp()
        throws Exception
    {
        // My config
        String conf = System.getProperty( "conf" );
        if ( conf == null )
        {
            conf = "conf-local/test/configuration.xml";
        }

        String datas = System.getProperty( "datas" );
        if ( datas == null )
        {
            datas = "conf-local/test/datas";
        }

        final XMLConfiguration config = new XMLConfiguration( conf );

        final Path pDatas = Paths.get( datas );
        dispatcher.init( config ,
                         pDatas ,
                         new VC_FileFactory( pDatas ) );

        // Test if parameters are set
        final ConfigProperties properties = dispatcher.getCommon( "facebookpage" );

        // Create tokens & login
        client.loginAsPage( properties.getConfigString( "pagename" ) ,
                            properties.getConfigString( "key" ) ,
                            properties.getConfigString( "secret" ) ,
                            dispatcher.getDatas().resolve( "test-tokens/facebookpage.token" ) ,
                            dispatcher.getVCodeFactory().create( "[" + getClass().getName() + "] " ) );
    }

    /**
     * Remove all links before each test.
     *
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void cleanAndCheck()
        throws Exception
    {
        client.deleteAllPosts();

        final Iterator<FB_Post> itControl = client.iteratorFeed( null ,
                                                                 1 );
        assertFalse( "Delete all should remove all links" ,
                     itControl.hasNext() );
    }

    /**
     * Test postLink.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testPostLink()
        throws Exception
    {
        // Post
        final String message = "Allez voir ce blog #" + new DateTime().getMillis();

        // Force post to be post one month ago
        final DateTime dt = new DateTime().minusMonths( 1 );

        final String ID = client.postLink( null ,
                                           message ,
                                           "http://fabien.vauchelles.com/" ,
                                           "Blog de Fabien Vauchelles" ,
                                           "JAVA ou JAVAPA?" ,
                                           "Du JAVA, du big data, et de l'entreprenariat" ,
                                           dt );

        assertTrue( "ID should be return" ,
                    ID != null && !ID.isEmpty() );

        // Read
        final FB_Post post = client.readPost( ID );

        assertEquals( "ID should be the same" ,
                      ID ,
                      post.getID() );
        assertEquals( "message should be the same" ,
                      message ,
                      post.getMessage() );
        assertEquals( "URL should be the same" ,
                      "http://fabien.vauchelles.com/" ,
                      post.getURL() );
        assertEquals( "URLname should be the same" ,
                      "Blog de Fabien Vauchelles" ,
                      post.getURLname() );
        assertEquals( "URLcaption should be the same" ,
                      "JAVA ou JAVAPA?" ,
                      post.getURLcaption() );
        assertEquals( "URLdescription should be the same" ,
                      "Du JAVA, du big data, et de l'entreprenariat" ,
                      post.getURLdescription() );
        assertTrue( "Post should have been created less than 1 minute" ,
                    new Duration( post.getCreatedTime() ,
                                  null ).getMillis() < 60000L );

        // Warning : we cannot control backdating. FB send only real creation date.
//        assertEquals( "Create date should be the same" ,
//                      dt.getMillis() ,
//                      post.getCreatedTime().getMillis() );
        assertTrue( "Post is usable" ,
                    post.isUsable() );

        // Like/Unlike
        assertTrue( "Like should work" ,
                    client.likePost( ID ) );
        assertTrue( "Unlike should work" ,
                    client.unlikePost( ID ) );

        // Delete
        assertTrue( "Delete should work" ,
                    client.deletePost( ID ) );
    }

    /**
     * Test postMessage.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testPostMessage()
        throws Exception
    {
        // Post
        final String message = "Allez voir mon blog #" + new DateTime().getMillis();

        // Force post to be post one month ago
        final DateTime dt = new DateTime().minusMonths( 1 );

        final String ID = client.postMessage( null ,
                                              message ,
                                              dt );

        assertTrue( "ID should be return" ,
                    ID != null && !ID.isEmpty() );

        // Read
        final FB_Post post = client.readPost( ID );

        assertEquals( "ID should be the same" ,
                      ID ,
                      post.getID() );
        assertEquals( "message should be the same" ,
                      message ,
                      post.getMessage() );
        assertTrue( "Post should have been created less than 1 minute" ,
                    new Duration( post.getCreatedTime() ,
                                  null ).getMillis() < 60000L );

        // Warning : we cannot control backdating. FB send only real creation date.
//        assertEquals( "Create date should be the same" ,
//                      dt.getMillis() ,
//                      post.getCreatedTime().getMillis() );
        assertTrue( "Post is usable" ,
                    post.isUsable() );

        // Delete
        assertTrue( "Delete should work" ,
                    client.deletePost( ID ) );
    }

    /**
     * Test deletePost.
     *
     * @throws java.lang.Exception
     */
    @Test( expectedExceptions =
    {
        FacebookException.class
    } )
    public void testDeletePost()
        throws Exception
    {
        // Post
        final String ID = client.postMessage( null ,
                                              "Allez voir mon blog #" + new DateTime().getMillis() ,
                                              null );

        assertTrue( "ID should be return" ,
                    ID != null && !ID.isEmpty() );

        // Delete
        assertTrue( "Delete should work" ,
                    client.deletePost( ID ) );

        // Read error
        client.readPost( ID );
    }

    // PRIVATE
    private final Dispatcher dispatcher;
    private final FacebookClient client;
}
