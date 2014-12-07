/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2007 Oracle.  All rights reserved.
 *
 * $Id: PartKey.java,v 1.10.2.1 2007/02/01 14:49:34 cwl Exp $
 */

package collections.ship.sentity;

/**
 * A PartKey serves as the key in the key/data pair for a part entity.
 *
 * <p> In this sample, PartKey is bound to the key's tuple storage entry using
 * a TupleBinding.  Because it is not used directly as storage data, it does
 * not need to be Serializable. </p>
 *
 * @author Mark Hayes
 */
public class PartKey {

    private String number;

    public PartKey(String number) {

        this.number = number;
    }

    public final String getNumber() {

        return number;
    }

    public String toString() {

        return "[PartKey: number=" + number + ']';
    }
}
