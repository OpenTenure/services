package org.sola.cs.services.ejbs.claim.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "party_for_claim_share")
public class ClaimPartyForShare extends AbstractVersionedEntity {
    @Id
    @Column(name="party_id")
    String partyId;
    
    @Id
    @Column(name="claim_share_id")
    String claimShareId;
    
    public ClaimPartyForShare(){
        super();
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getClaimShareId() {
        return claimShareId;
    }

    public void setClaimShareId(String claimShareId) {
        this.claimShareId = claimShareId;
    }
}
