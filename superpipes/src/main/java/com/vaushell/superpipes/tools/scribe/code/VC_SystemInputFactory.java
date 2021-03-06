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

package com.vaushell.superpipes.tools.scribe.code;

import java.util.Scanner;

/**
 * Ask to user to enter the code with keyboard.
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public class VC_SystemInputFactory
    implements A_ValidatorCode.I_Factory
{
    // PUBLIC
    public VC_SystemInputFactory()
    {
        // Nothing
    }

    @Override
    public A_ValidatorCode create( final String prefix )
    {
        return new VC_SystemInput( prefix );
    }

    // PRIVATE
    private static class VC_SystemInput
        extends A_ValidatorCode
    {
        // PUBLIC
        public VC_SystemInput( final String prefix )
        {
            super( prefix );
        }

        @Override
        public String getValidationCode( final String authURL )
        {
            System.out.println( getPrefix() + " Use this URL :" );
            System.out.println( authURL );

            System.out.println( getPrefix() + " Enter code :" );

            // Never include System.(in|out) in a try-catch-resources...
            // You could use it only one time !
            final Scanner sc = new Scanner( System.in ,
                                            "UTF-8" );
            final String code = sc.next();

            System.out.println( getPrefix() + " Read code is '" + code + "'" );

            return code;
        }
    }
}
