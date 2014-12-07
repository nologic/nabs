/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: ModelInternal.java,v 1.5.2.2 2008/01/07 15:14:20 cwl Exp $
 */

package com.sleepycat.persist.model;

import com.sleepycat.persist.impl.PersistCatalog;

/**
 * Internal access class that does not appear in the javadoc and should not be
 * used by applications.
 *
 * @author Mark Hayes
 */
public class ModelInternal {

    public static void setCatalog(EntityModel model, PersistCatalog catalog) {
        model.setCatalog(catalog);
    }
}
