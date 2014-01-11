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

package com.vaushell.spipes.nodes.fb;

import com.vaushell.spipes.dispatch.Message;
import com.vaushell.spipes.nodes.A_Node;
import com.vaushell.spipes.tools.scribe.fb.FB_Post;
import com.vaushell.spipes.tools.scribe.fb.FacebookClient;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Post a message to Facebook.
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public class N_FB
    extends A_Node
{
    // PUBLIC
    public N_FB()
    {
        // Read every 10 minutes
        super( 600000L ,
               0L );

        this.client = new FacebookClient();
    }

    // PROTECTED
    @Override
    protected void prepareImpl()
        throws Exception
    {
        final Path tokenPath = getDispatcher().getDatas().resolve( Paths.get( getNodeID() ,
                                                                              "token" ) );

        final String pageName = getConfig( "pagename" );
        if ( pageName == null )
        {
            final String userID = getConfig( "userid" );
            if ( userID == null )
            {
                client.login( getConfig( "key" ) ,
                              getConfig( "secret" ) ,
                              tokenPath ,
                              getDispatcher().getVCodeFactory().create( "[" + getClass().getName() + " / " + getNodeID() + "] " ) );
            }
            else
            {
                client.loginAsOtherUser( userID ,
                                         getConfig( "key" ) ,
                                         getConfig( "secret" ) ,
                                         tokenPath ,
                                         getDispatcher().getVCodeFactory().create(
                    "[" + getClass().getName() + " / " + getNodeID() + "] " ) );
            }
        }
        else
        {
            client.loginAsPage( pageName ,
                                getConfig( "key" ) ,
                                getConfig( "secret" ) ,
                                tokenPath ,
                                getDispatcher().getVCodeFactory().
                create( "[" + getClass().getName() + " / " + getNodeID() + "] " ) );
        }
    }

    @Override
    protected void loop()
        throws Exception
    {
        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( "[" + getNodeID() + "] read feed " );
        }

        final int max = Integer.parseInt( getConfig( "max" ) );

        int count = 0;
        final Iterator<FB_Post> it = client.iteratorFeed( Math.min( POST_MAX_COUNT ,
                                                                    max ) );
        while ( it.hasNext() && count < max )
        {
            final FB_Post post = it.next();

            if ( post.getID() != null )
            {
                final Message message = Message.create(
                    "id-facebook" ,
                    post.getID() ,
                    Message.KeyIndex.PUBLISHED_DATE ,
                    post.getCreatedTime()
                );

                if ( post.getFrom() != null && post.getFrom().getName() != null )
                {
                    message.setProperty( Message.KeyIndex.AUTHOR ,
                                         post.getFrom().getName() );
                }

                if ( post.getMessage() != null )
                {
                    message.setProperty( Message.KeyIndex.CONTENT ,
                                         post.getMessage() );
                }

                if ( post.getURL() != null )
                {
                    message.setProperty( Message.KeyIndex.URI ,
                                         URI.create( post.getURL() ) );
                }

                if ( post.getURLcaption() != null )
                {
                    message.setProperty( "caption-facebook" ,
                                         post.getURLcaption() );
                }

                if ( post.getURLdescription() != null )
                {
                    message.setProperty( Message.KeyIndex.DESCRIPTION ,
                                         post.getURLdescription() );
                }

                if ( post.getURLname() != null )
                {
                    message.setProperty( Message.KeyIndex.TITLE ,
                                         post.getURLname() );
                }

                sendMessage( message );
            }

            ++count;
        }
    }

    @Override
    protected void terminateImpl()
        throws Exception
    {
        // Nothing
    }
    // PRIVATE
    private static final Logger LOGGER = LoggerFactory.getLogger( N_FB.class );
    private static final int POST_MAX_COUNT = 25;
    private final FacebookClient client;
}