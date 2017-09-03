package org.sola.cs.services.ejbs.claim.entities;

import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

@Table(name = "termination_reason", schema = "opentenure")
@DefaultSorter(sortString="display_value")
public class TerminationReason extends AbstractCodeEntity {
    public static final String CODE_MERGE = "merge";
    public static final String CODE_SPLIT = "split";
    
    public TerminationReason(){
        super();
    }
}