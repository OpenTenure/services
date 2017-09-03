package org.sola.cs.services.ejbs.claim.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "party_for_restriction")
public class RestrictionParty extends AbstractVersionedEntity {
    @Id
    @Column(name="party_id")
    String partyId;
    
    @Id
    @Column(name="restriction_id")
    String restrictionId;
    
    public RestrictionParty(){
        super();
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getRestrictionId() {
        return restrictionId;
    }

    public void setRestrictionId(String restrictionId) {
        this.restrictionId = restrictionId;
    }
}
