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

package com.vaushell.spipes.nodes.tumblr;

import com.vaushell.spipes.nodes.A_Node;
import com.vaushell.spipes.nodes.rss.News;
import com.vaushell.spipes.tools.HTMLhelper;
import com.vaushell.spipes.tools.scribe.tumblr.TumblrClient;
import com.vaushell.spipes.tools.scribe.tumblr.TumblrException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public class N_TB_Post
    extends A_Node
{
    // PUBLIC
    public N_TB_Post()
    {
        this.client = new TumblrClient();
    }

    @Override
    public void prepare()
        throws Exception
    {
        Path tokenPath = Paths.get( getMainConfig( "datas-directory" ) ,
                                    getNodeID() ,
                                    "token" );

        client.login( getConfig( "blogname" ) ,
                      getConfig( "key" ) ,
                      getConfig( "secret" ) ,
                      tokenPath ,
                      "[" + getClass().getName() + " / " + getNodeID() + "]" );
    }

    @Override
    public void terminate()
        throws Exception
    {
    }

    // PROTECTED
    @Override
    protected void loop()
        throws InterruptedException , TumblrException , IOException
    {
        // Receive
        Object message = getLastMessageOrWait();

        if ( logger.isTraceEnabled() )
        {
            logger.trace( "[" + getNodeID() + "] receive message : " + message );
        }

        // Convert if possible
        TB_Post post;
        if ( message == null )
        {
            post = null;
        }
        else
        {
            if ( message instanceof TB_Post )
            {
                post = (TB_Post) message;
            }
            else if ( message instanceof News )
            {
                post = convertFromNews( (News) message );
            }
            else
            {
                post = null;
            }
        }

        if ( post == null )
        {
            throw new IllegalArgumentException( "message type is unknown : " + message.getClass().getName() );
        }

        // Send to FB
        if ( logger.isTraceEnabled() )
        {
            logger.trace( "[" + getNodeID() + "] send post to facebook : " + post );
        }

        String uri;
        if ( post.getURI() != null )
        {
            uri = post.getURI().toURL().toString();
        }
        else
        {
            uri = null;
        }

        long ID = client.postLink( post.getMessage() ,
                                   uri ,
                                   post.getURIname() ,
                                   post.getURIdescription() ,
                                   post.getTags() );

        post.setTumblrID( ID );

        if ( logger.isTraceEnabled() )
        {
            logger.trace( "[" + getNodeID() + "] receive ID : " + ID );
        }

        sendMessage( post );
    }
    // PRIVATE
    private final static Logger logger = LoggerFactory.getLogger( N_TB_Post.class );
    private TumblrClient client;

    private static TB_Post convertFromNews( News news )
    {
        if ( news.getURI() == null )
        {
            throw new NullPointerException( "URI can not be null" );
        }

        return new TB_Post( null ,
                            news.getURI() ,
                            news.getURIsource() ,
                            HTMLhelper.cleanHTML( news.getTitle() ) ,
                            HTMLhelper.cleanHTML( news.getDescription() ) ,
                            news.getTags() );
    }
}
