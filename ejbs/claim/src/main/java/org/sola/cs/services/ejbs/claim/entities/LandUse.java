package org.sola.cs.services.ejbs.claim.entities;

import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractCodeEntity;

@Table(name = "land_use", schema = "opentenure")
@DefaultSorter(sortString="display_value")
public class LandUse extends AbstractCodeEntity {
    public LandUse(){
        super();
    }
}
