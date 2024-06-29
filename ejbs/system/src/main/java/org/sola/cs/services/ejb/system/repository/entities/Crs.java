package org.sola.cs.services.ejb.system.repository.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import org.sola.services.common.repository.entities.AbstractEntity;

@Table(name = "crs", schema = "system")
public class Crs extends AbstractEntity {
    @Id
    @Column
    private int srid;
    
    @Column(name="from_long")
    private double fromLong;
    
    @Column(name="to_long")
    private double toLong;
    
    @Column(name="item_order")
    private int itemOrder;

    public int getSrid() {
        return srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public double getFromLong() {
        return fromLong;
    }

    public void setFromLong(double fromLong) {
        this.fromLong = fromLong;
    }

    public double getToLong() {
        return toLong;
    }

    public void setToLong(double toLong) {
        this.toLong = toLong;
    }

    public int getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(int itemOrder) {
        this.itemOrder = itemOrder;
    }
}
