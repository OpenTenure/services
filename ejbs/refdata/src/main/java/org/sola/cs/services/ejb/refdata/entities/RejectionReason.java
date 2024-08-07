package org.sola.cs.services.ejb.refdata.entities;

import jakarta.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

@Table(name = "rejection_reason", schema = "opentenure")
@DefaultSorter(sortString="display_value")
public class RejectionReason extends AbstractCodeEntity  {
    public RejectionReason(){
        super();
    }
}
