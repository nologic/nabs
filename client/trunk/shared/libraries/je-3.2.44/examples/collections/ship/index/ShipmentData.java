/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2007 Oracle.  All rights reserved.
 *
 * $Id: ShipmentData.java,v 1.12.2.1 2007/02/01 14:49:33 cwl Exp $
 */

package collections.ship.index;

import java.io.Serializable;

/**
 * A ShipmentData serves as the data in the key/data pair for a shipment
 * entity.
 *
 * <p> In this sample, ShipmentData is used both as the storage data for the
 * data as well as the object binding to the data.  Because it is used
 * directly as storage data using serial format, it must be Serializable. </p>
 *
 * @author Mark Hayes
 */
public class ShipmentData implements Serializable {

    private int quantity;

    public ShipmentData(int quantity) {

        this.quantity = quantity;
    }

    public final int getQuantity() {

        return quantity;
    }

    public String toString() {

        return "[ShipmentData: quantity=" + quantity + ']';
    }
}
