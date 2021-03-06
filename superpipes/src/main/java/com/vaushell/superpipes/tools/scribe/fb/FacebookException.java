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

import com.vaushell.superpipes.tools.scribe.OAuthException;

/**
 * Facebook Exception.
 *
 * @author Fabien Vauchelles (fabien_AT_vauchelles_DOT_com)
 */
public final class FacebookException
    extends OAuthException
{
    // PUBLIC
    public FacebookException( final int httpCode ,
                              final int apiCode ,
                              final String message ,
                              final String type )
    {
        super( httpCode ,
               apiCode ,
               message );

        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    @Override
    public String getMessage()
    {
        return super.getMessage() + ", type=" + getType();
    }

    @Override
    public String toString()
    {
        return "FacebookException{" + super.toString() + ", type=" + type + '}';
    }

    // PRIVATE
    private final String type;
}
