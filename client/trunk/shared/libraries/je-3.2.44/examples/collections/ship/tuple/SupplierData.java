/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2007 Oracle.  All rights reserved.
 *
 * $Id: SupplierData.java,v 1.12.2.1 2007/02/01 14:49:36 cwl Exp $
 */

package collections.ship.tuple;

import java.io.Serializable;

/**
 * A SupplierData serves as the value in the key/value pair for a supplier
 * entity.
 *
 * <p> In this sample, SupplierData is used only as the storage data for the
 * value, while the Supplier object is used as the value's object
 * representation.  Because it is used directly as storage data using
 * serial format, it must be Serializable. </p>
 *
 * @author Mark Hayes
 */
public class SupplierData implements Serializable {

    private String name;
    private int status;
    private String city;

    public SupplierData(String name, int status, String city) {

        this.name = name;
        this.status = status;
        this.city = city;
    }

    public final String getName() {

        return name;
    }

    public final int getStatus() {

        return status;
    }

    public final String getCity() {

        return city;
    }

    public String toString() {

        return "[SupplierData: name=" + name +
	    " status=" + status +
	    " city=" + city + ']';
    }
}
