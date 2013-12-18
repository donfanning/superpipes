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

package com.vaushell.spipes.nodes.buffer;

import java.util.Calendar;
import java.util.TreeSet;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

/**
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public class SlotTest
{
    // PUBLIC
    public SlotTest()
    {
        // Nothing
    }

    /**
     * Test of areWeInside method, of class Slot.
     */
    @Test
    public void testAreWeInside()
    {
        // TUE, SAT with 17:00-18:30
        final TreeSet<Integer> days = new TreeSet<>();
        days.add( Calendar.TUESDAY );
        days.add( Calendar.SATURDAY );

        Calendar minHour = Calendar.getInstance();
        minHour.set( Calendar.HOUR_OF_DAY ,
                     17 );
        minHour.set( Calendar.MINUTE ,
                     0 );
        Calendar maxHour = Calendar.getInstance();
        maxHour.set( Calendar.HOUR_OF_DAY ,
                     18 );
        maxHour.set( Calendar.MINUTE ,
                     30 );

        Slot slot = new Slot( days ,
                              minHour ,
                              maxHour );

        // With TUE 16:59 => false
        Calendar cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.TUESDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 16 );
        cal.set( Calendar.MINUTE ,
                 59 );
        assertFalse( "TUE 16:59 inside TUE, SAT with 17:00-18:30" ,
                     slot.areWeInside( cal ) );

        // With TUE 17:01 => true
        cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.TUESDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 17 );
        cal.set( Calendar.MINUTE ,
                 1 );
        assertTrue( "TUE 17:01 inside TUE, SAT with 17:00-18:30" ,
                    slot.areWeInside( cal ) );

        // With TUE 18:01 => true
        cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.TUESDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 18 );
        cal.set( Calendar.MINUTE ,
                 1 );
        assertTrue( "TUE 18:01 inside TUE, SAT with 17:00-18:30" ,
                    slot.areWeInside( cal ) );

        // With TUE 19:00 => false
        cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.TUESDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 19 );
        cal.set( Calendar.MINUTE ,
                 0 );
        assertFalse( "TUE 19:00 inside TUE, SAT with 17:00-18:30" ,
                     slot.areWeInside( cal ) );

        // With MON 17:30 => false
        cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.MONDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 17 );
        cal.set( Calendar.MINUTE ,
                 30 );
        assertFalse( "MON 17:30 inside TUE, SAT with 17:00-18:30" ,
                     slot.areWeInside( cal ) );

        // With SAT 17:30 => true
        cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.SATURDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 17 );
        cal.set( Calendar.MINUTE ,
                 30 );
        assertTrue( "SAT 17:30 inside TUE, SAT with 17:00-18:30" ,
                    slot.areWeInside( cal ) );

        // MON with 00:00-23:58
        days.clear();
        days.add( Calendar.MONDAY );

        minHour = Calendar.getInstance();
        minHour.set( Calendar.HOUR_OF_DAY ,
                     0 );
        minHour.set( Calendar.MINUTE ,
                     0 );
        maxHour = Calendar.getInstance();
        maxHour.set( Calendar.HOUR_OF_DAY ,
                     23 );
        maxHour.set( Calendar.MINUTE ,
                     58 );

        slot = new Slot( days ,
                         minHour ,
                         maxHour );

        // With MON 00:00 => true
        cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.MONDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 0 );
        cal.set( Calendar.MINUTE ,
                 0 );
        assertTrue( "MON 00:00 inside MON with 00:00-23:58" ,
                    slot.areWeInside( cal ) );

        // With MON 23:58 => true
        cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.MONDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 23 );
        cal.set( Calendar.MINUTE ,
                 58 );
        assertTrue( "MON 23:58 inside MON with 00:00-23:58" ,
                    slot.areWeInside( cal ) );

        // With MON 23:59 => false
        cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.MONDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 23 );
        cal.set( Calendar.MINUTE ,
                 59 );
        assertFalse( "MON 23:59 inside MON with 00:00-23:58" ,
                     slot.areWeInside( cal ) );
    }

    /**
     * Test of getSmallestDiffInMs method, of class Slot.
     */
    @Test
    public void testGetSmallestDiffInMs()
    {
        // TUE, SAT with 17:00-18:30
        final TreeSet<Integer> days = new TreeSet<>();
        days.add( Calendar.TUESDAY );
        days.add( Calendar.SATURDAY );

        final Calendar minHour = Calendar.getInstance();
        minHour.set( Calendar.HOUR_OF_DAY ,
                     17 );
        minHour.set( Calendar.MINUTE ,
                     0 );
        final Calendar maxHour = Calendar.getInstance();
        maxHour.set( Calendar.HOUR_OF_DAY ,
                     18 );
        maxHour.set( Calendar.MINUTE ,
                     30 );

        final Slot slot = new Slot( days ,
                                    minHour ,
                                    maxHour );

        // with TUE, 16:00 => 3600000
        Calendar cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.TUESDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 16 );
        cal.set( Calendar.MINUTE ,
                 0 );
        assertEquals( "TUE, 16:00 inside TUE, SAT with 17:00-18:30" ,
                      3600000 ,
                      slot.getSmallestDiffInMs( cal ) );

        // with MON, 17:00 => 86400000
        cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.MONDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 17 );
        cal.set( Calendar.MINUTE ,
                 0 );
        assertEquals( "MON, 17:00 inside TUE, SAT with 17:00-18:30" ,
                      86400000 ,
                      slot.getSmallestDiffInMs( cal ) );

        // with WED, 17:00 => 259200000
        cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.WEDNESDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 17 );
        cal.set( Calendar.MINUTE ,
                 0 );
        assertEquals( "WED, 17:00 inside TUE, SAT with 17:00-18:30" ,
                      259200000 ,
                      slot.getSmallestDiffInMs( cal ) );

        // with SAT, 18:31 => 253740000
        cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_WEEK ,
                 Calendar.SATURDAY );
        cal.set( Calendar.HOUR_OF_DAY ,
                 18 );
        cal.set( Calendar.MINUTE ,
                 31 );
        assertEquals( "SAT, 18:01 inside TUE, SAT with 17:00-18:30" ,
                      253740000 ,
                      slot.getSmallestDiffInMs( cal ) );
    }
}