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

package com.vaushell.superpipes.nodes;

import com.vaushell.superpipes.dispatch.ConfigProperties;
import com.vaushell.superpipes.dispatch.Dispatcher;
import com.vaushell.superpipes.dispatch.Message;
import com.vaushell.superpipes.transforms.A_Transform;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.SerializationUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A processing node.
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public abstract class A_Node
    extends Thread
{
    // PUBLIC
    public static final Duration DEFAULT_DELAY = new Duration( 1L * 1000L );
    public static final Duration DEFAULT_ANTIBURST = new Duration( 2L * 1000L );
    public static final Duration SECURE_ANTIBURST = new Duration( 60L * 1000L );
    public static final Duration LIGHT_ANTIBURST = new Duration( 500L );

    public A_Node( final Duration defaultDelay ,
                   final Duration defaultAntiBurst )
    {
        super();

        this.activated = true;
        this.internalStack = new LinkedList<>();
        this.transformsIN = new ArrayList<>();
        this.transformsOUT = new ArrayList<>();
        this.properties = new ConfigProperties();
        this.lastPop = null;
        this.message = null;

        if ( defaultAntiBurst != null && defaultAntiBurst.getMillis() <= 0L )
        {
            throw new IllegalArgumentException( "defaultAntiBurst can't be <=0. Should be null." );
        }
        this.antiBurst = defaultAntiBurst;

        if ( defaultDelay != null && defaultDelay.getMillis() <= 0L )
        {
            throw new IllegalArgumentException( "defaultDelay can't be <=0. Should be null." );
        }
        this.delay = defaultDelay;
    }

    /**
     * Set node's parameters.
     *
     * @param nodeID Node's identifier
     * @param dispatcher Main dispatcher
     * @param commons commons properties
     */
    public void setParameters( final String nodeID ,
                               final Dispatcher dispatcher ,
                               final List<ConfigProperties> commons )
    {
        this.nodeID = nodeID;
        this.dispatcher = dispatcher;
        this.properties.addCommons( commons );
    }

    public String getNodeID()
    {
        return nodeID;
    }

    public ConfigProperties getProperties()
    {
        return properties;
    }

    public Dispatcher getDispatcher()
    {
        return dispatcher;
    }

    /**
     * Load configuration for this node.
     *
     * @param cNode Configuration
     * @throws Exception
     */
    public void load( final HierarchicalConfiguration cNode )
        throws Exception
    {
        getProperties().readProperties( cNode );

        antiBurst = getProperties().getConfigDuration( "anti-burst" ,
                                                       antiBurst );
        delay = getProperties().getConfigDuration( "delay" ,
                                                   delay );

        // Load transforms IN
        transformsIN.clear();
        final List<HierarchicalConfiguration> cTransformsIN = cNode.configurationsAt( "in.transform" );
        if ( cTransformsIN != null )
        {
            for ( final HierarchicalConfiguration cTransform : cTransformsIN )
            {
                final List<ConfigProperties> commons = new ArrayList<>();

                final String commonsID = cTransform.getString( "[@commons]" );
                if ( commonsID != null )
                {
                    for ( final String commonID : commonsID.split( "," ) )
                    {
                        final ConfigProperties common = getDispatcher().getCommon( commonID );
                        if ( common != null )
                        {
                            commons.add( common );
                        }
                    }
                }

                final A_Transform transform = addTransformIN( cTransform.getString( "[@type]" ) ,
                                                              commons );

                transform.load( cTransform );
            }
        }

        // Load transforms OUT
        transformsOUT.clear();
        final List<HierarchicalConfiguration> cTransformsOUT = cNode.configurationsAt( "out.transform" );
        if ( cTransformsOUT != null )
        {
            for ( final HierarchicalConfiguration cTransform : cTransformsOUT )
            {
                final List<ConfigProperties> commons = new ArrayList<>();

                final String commonsID = cTransform.getString( "[@commons]" );
                if ( commonsID != null )
                {
                    for ( final String commonID : commonsID.split( "," ) )
                    {
                        final ConfigProperties common = getDispatcher().getCommon( commonID );
                        if ( common != null )
                        {
                            commons.add( common );
                        }
                    }
                }

                final A_Transform transform = addTransformOUT( cTransform.getString( "[@type]" ) ,
                                                               commons );

                transform.load( cTransform );
            }
        }
    }

    /**
     * Add a transform to the node input.
     *
     * @param clazz Transform's type class
     * @param commons commons properties
     * @return the transform
     */
    public A_Transform addTransformIN( final Class<?> clazz ,
                                       final List<ConfigProperties> commons )
    {
        return addTransformIN( clazz.getName() ,
                               commons );
    }

    /**
     * Add a transform to the node input.
     *
     * @param type Transform's type
     * @param commons commons properties
     * @return the transform
     */
    public A_Transform addTransformIN( final String type ,
                                       final List<ConfigProperties> commons )
    {
        if ( type == null )
        {
            throw new IllegalArgumentException();
        }

        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace(
                "[" + getClass().getSimpleName() + "] addTransformIN : type=" + type );
        }

        try
        {
            final A_Transform transform = (A_Transform) Class.forName( type ).newInstance();
            transform.setParameters( this ,
                                     commons );

            transformsIN.add( transform );

            return transform;
        }
        catch( final ClassNotFoundException |
                     IllegalAccessException |
                     InstantiationException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Add a transform to the node output.
     *
     * @param clazz Transform's type class
     * @param commons commons properties
     * @return the transform
     */
    public A_Transform addTransformOUT( final Class<?> clazz ,
                                        final List<ConfigProperties> commons )
    {
        return addTransformOUT( clazz.getName() ,
                                commons );
    }

    /**
     * Add a transform to the node output.
     *
     * @param type Transform's type
     * @param commons commons properties
     * @return the transform
     */
    public A_Transform addTransformOUT( final String type ,
                                        final List<ConfigProperties> commons )
    {
        if ( type == null )
        {
            throw new IllegalArgumentException();
        }

        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace(
                "[" + getClass().getSimpleName() + "] addTransformOUT : type=" + type );
        }

        try
        {
            final A_Transform transform = (A_Transform) Class.forName( type ).newInstance();
            transform.setParameters( this ,
                                     commons );

            transformsOUT.add( transform );

            return transform;
        }
        catch( final ClassNotFoundException |
                     IllegalAccessException |
                     InstantiationException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Prepare node's execution. Executed 1 time at the beginning. Generic implementation.
     *
     * @throws Exception
     */
    public void prepare()
        throws Exception
    {
        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( "[" + getNodeID() + "] prepare" );
        }
        prepareImpl();

        for ( final A_Transform transform : transformsIN )
        {
            if ( LOGGER.isTraceEnabled() )
            {
                LOGGER.trace( "[" + getNodeID() + "/IN:" + transform.getClass().getSimpleName() + "] prepare" );
            }
            transform.prepare();
        }

        for ( final A_Transform transform : transformsOUT )
        {
            if ( LOGGER.isTraceEnabled() )
            {
                LOGGER.trace( "[" + getNodeID() + "/OUT:" + transform.getClass().getSimpleName() + "] prepare" );
            }
            transform.prepare();
        }
    }

    @Override
    public void run()
    {
        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( "[" + getNodeID() + "] start thread" );
        }

        try
        {
            while ( isActive() )
            {
                try
                {
                    setMessage( null );
                    loop();
                    setMessage( null );
                }
                catch( final InterruptedException ex )
                {
                    // Ignore
                }
                catch( final Throwable ex )
                {
                    getDispatcher().postError( ex ,
                                               message );
                }

                if ( delay != null )
                {
                    try
                    {
                        Thread.sleep( delay.getMillis() );
                    }
                    catch( final InterruptedException ex )
                    {
                        // Ignore
                    }
                }
            }
        }
        catch( final Throwable th )
        {
            getDispatcher().postError( th ,
                                       null );
        }

        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( "[" + getNodeID() + "] stop thread" );
        }
    }

    /**
     * Close node's execution. Executed 1 time at the ending. Generic implementation.
     *
     * @throws Exception
     */
    public void terminate()
        throws Exception
    {
        for ( final A_Transform transform : transformsOUT )
        {
            if ( LOGGER.isTraceEnabled() )
            {
                LOGGER.trace( "[" + getNodeID() + "/OUT:" + transform.getClass().getSimpleName() + "] terminate" );
            }
            transform.terminate();
        }

        for ( final A_Transform transform : transformsIN )
        {
            if ( LOGGER.isTraceEnabled() )
            {
                LOGGER.trace( "[" + getNodeID() + "/IN:" + transform.getClass().getSimpleName() + "] terminate" );
            }
            transform.terminate();
        }

        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( "[" + getNodeID() + "] terminate" );
        }
        terminateImpl();
    }

    /**
     * Receive a message and stack it.
     *
     * @param message Message
     * @throws java.lang.Exception
     */
    public void receiveMessage( final Message message )
        throws Exception
    {
        if ( message == null )
        {
            throw new IllegalArgumentException();
        }

        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( "[" + getNodeID() + "] receiveMessage : message=" + Message.formatSimple( message ) );
        }

        Message result = SerializationUtils.clone( message );
        for ( final A_Transform transform : transformsIN )
        {
            result = transform.transform( result );
            if ( result == null )
            {
                if ( LOGGER.isDebugEnabled() )
                {
                    LOGGER.debug( "[" + getNodeID() + "] receive but discard message=" + Message.formatSimple(
                        message ) );
                }

                return;
            }
            else
            {
                result = SerializationUtils.clone( result );
            }
        }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "[" + getNodeID() + "] receive and stack message=" + Message.formatSimple( message ) );
        }

        synchronized( internalStack )
        {
            internalStack.addFirst( result );

            internalStack.notifyAll();
        }
    }

    /**
     * Stop the node.
     */
    public void stopMe()
    {
        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( "[" + getNodeID() + "] stopMe" );
        }

        synchronized( this )
        {
            activated = false;
        }

        interrupt();
    }

    // PROTECTED
    /**
     * Prepare node's execution. Executed 1 time at the beginning.
     *
     * @throws Exception
     */
    protected abstract void prepareImpl()
        throws Exception;

    /**
     * Loop execution. The execution is looped until message reception.
     *
     * @throws java.lang.Exception
     */
    protected abstract void loop()
        throws Exception;

    /**
     * Close node's execution. Executed 1 time at the ending.
     *
     * @throws Exception
     */
    protected abstract void terminateImpl()
        throws Exception;

    /**
     * Send actual message to every connected nodes.
     *
     * @throws java.lang.Exception
     */
    protected void sendMessage()
        throws Exception
    {
        if ( message == null )
        {
            throw new IllegalArgumentException( "Message is not set" );
        }

        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( "[" + getNodeID() + "] sendMessage : message=" + Message.formatSimple( message ) );
        }

        Message result = message;
        for ( final A_Transform transform : transformsOUT )
        {
            result = transform.transform( result );
            if ( result == null )
            {
                if ( LOGGER.isDebugEnabled() )
                {
                    LOGGER.
                        debug( "[" + getNodeID() + "] send but discard message=" + Message.formatSimple( message ) );
                }

                return;
            }
            else
            {
                result = SerializationUtils.clone( result );
            }
        }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "[" + getNodeID() + "] send message=" + Message.formatSimple( message ) );
        }

        dispatcher.sendMessage( nodeID ,
                                result );
    }

    /**
     * Is the node alive ?
     *
     * @return True if alive
     */
    protected boolean isActive()
    {
        synchronized( this )
        {
            return activated;
        }
    }

    /**
     * Pop the last message.
     *
     * @return the message
     * @throws InterruptedException
     */
    protected Message getLastMessageOrWait()
        throws InterruptedException
    {
        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( "[" + getNodeID() + "] getLastMessageOrWait" );
        }

        final Message message;
        synchronized( internalStack )
        {
            while ( internalStack.isEmpty() )
            {
                internalStack.wait();
            }

            message = internalStack.pollLast();
        }

        if ( lastPop != null && antiBurst != null )
        {
            // Null for now
            final Duration elapsed = new Duration( lastPop ,
                                                   null );

            final Duration remaining = antiBurst.minus( elapsed );
            if ( remaining.getMillis() > 0L )
            {
                Thread.sleep( remaining.getMillis() );
            }
        }

        lastPop = new DateTime();

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "[" + getNodeID() + "] wait message and get message=" + Message.formatSimple( message ) );
        }

        return message;
    }

    /**
     * Pop the last message.
     *
     * @param timeout max time to wait. If timeout is smaller than antiburst, use antiburst.
     * @return the message (or null if empty)
     * @throws InterruptedException
     */
    protected Message getLastMessageOrWait( final Duration timeout )
        throws InterruptedException
    {
        if ( timeout == null )
        {
            throw new IllegalArgumentException();
        }

        if ( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( "[" + getNodeID() + "] getLastMessageOrWait() : timeout=" + timeout );
        }

        final Message message;
        synchronized( internalStack )
        {
            DateTime start = new DateTime();
            Duration remaining = timeout;
            while ( internalStack.isEmpty()
                    && remaining.getMillis() > 0L )
            {
                internalStack.wait( remaining.getMillis() );

                final DateTime now = new DateTime();

                final Duration elapsed = new Duration( start ,
                                                       now );

                remaining = remaining.minus( elapsed );

                start = now;
            }

            message = internalStack.pollLast();
        }

        if ( lastPop != null && antiBurst != null )
        {
            // Null for now
            final Duration elapsed = new Duration( lastPop ,
                                                   null );

            final Duration remaining = antiBurst.minus( elapsed );
            if ( remaining.getMillis() > 0L )
            {
                Thread.sleep( remaining.getMillis() );
            }
        }

        lastPop = new DateTime();

        if ( LOGGER.isDebugEnabled() )
        {
            if ( message == null )
            {
                LOGGER.debug( "[" + getNodeID() + "] wait message for " + timeout + "ms and get nothing" );
            }
            else
            {
                LOGGER.debug( "[" + getNodeID() + "] wait message for " + timeout + "ms and get message=" + Message.formatSimple(
                    message ) );
            }
        }

        return message;
    }

    protected Message getMessage()
    {
        return message;
    }

    protected void setMessage( final Message message )
    {
        this.message = message;
    }

    // PRIVATE
    private static final Logger LOGGER = LoggerFactory.getLogger( A_Node.class );
    private String nodeID;
    private final ConfigProperties properties;
    private Dispatcher dispatcher;
    private final LinkedList<Message> internalStack;
    private volatile boolean activated;
    private final List<A_Transform> transformsIN;
    private final List<A_Transform> transformsOUT;
    private DateTime lastPop;
    private Duration antiBurst;
    private Duration delay;
    private Message message;

}
