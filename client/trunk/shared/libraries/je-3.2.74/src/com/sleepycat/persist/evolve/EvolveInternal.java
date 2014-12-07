/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: EvolveInternal.java,v 1.2.2.2 2008/01/07 15:14:19 cwl Exp $
 */

package com.sleepycat.persist.evolve;

/**
 * Internal access class that does not appear in the javadoc and should not be
 * used by applications.
 *
 * @author Mark Hayes
 */
public class EvolveInternal {

    public static EvolveEvent newEvent() {
        return new EvolveEvent();
    }

    public static void updateEvent(EvolveEvent event,
                                   String entityClassName,
                                   int nRead,
                                   int nConverted) {
        event.update(entityClassName);
        event.getStats().add(nRead, nConverted);
    }
}
