package com.vaushell.spipes;

import com.vaushell.spipes.nodes.A_Node;
import com.vaushell.spipes.nodes.stub.N_MessageLogger;
import com.vaushell.spipes.nodes.stub.N_NewsGenerator;
import java.util.Properties;
import javax.naming.ConfigurationException;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test
 *
 * @see Dispatcher cs
 */
public class DispatcherTest
{
    // PUBLIC
    @BeforeClass
    public void setUpClass()
            throws ConfigurationException
    {
    }

    @AfterClass
    public void tearDownClass()
            throws Exception
    {
    }

    @Test
    public void testAdd()
    {
        Dispatcher dispatcher = new Dispatcher();

        dispatcher.addNode( "generator" ,
                            N_NewsGenerator.class ,
                            new Properties() );

        dispatcher.addNode( "receptor" ,
                            N_MessageLogger.class ,
                            new Properties() );

        A_Node node = dispatcher.nodes.get( "generator" );
        assertEquals( node.getClass() ,
                      N_NewsGenerator.class );

        dispatcher.addRoute( "generator" ,
                             "receptor" );

        assertTrue( dispatcher.routes.get( "generator" ).contains( "receptor" ) );

        dispatcher.addRoute( "generator" ,
                             "receptor" );

        assertEquals( dispatcher.routes.get( "generator" ).size() ,
                      1 );
    }

    @Test( expectedExceptions =
    {
        IllegalArgumentException.class
    } )
    public void testNodeDuplicate()
    {
        Dispatcher dispatcher = new Dispatcher();

        dispatcher.addNode( "generator" ,
                            N_NewsGenerator.class ,
                            new Properties() );
        dispatcher.addNode( "generator" ,
                            N_NewsGenerator.class ,
                            new Properties() );
    }

    @Test( expectedExceptions =
    {
        IllegalArgumentException.class
    } )
    public void testAddRouteBeforeNode()
    {
        Dispatcher dispatcher = new Dispatcher();

        dispatcher.addRoute( "generator" ,
                             "receptor" );
    }
}
