package org.sola.cs.services.ejbs.claim.entities;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "claim_share")
public class ClaimShare extends AbstractVersionedEntity {
    
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "claim_id")
    private String claimId;
    @Column(name = "nominator")
    private Short nominator;
    @Column(name = "denominator")
    private Short denominator;
    @Column(name = "percentage")
    private double percentage;
    @ChildEntityList(parentIdField = "claimShareId", childIdField = "partyId",
    manyToManyClass = ClaimPartyForShare.class)
    private List<ClaimParty> owners;
    
    public ClaimShare(){
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public Short getNominator() {
        return nominator;
    }

    public void setNominator(Short nominator) {
        this.nominator = nominator;
    }

    public Short getDenominator() {
        return denominator;
    }

    public void setDenominator(Short denominator) {
        this.denominator = denominator;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public List<ClaimParty> getOwners() {
        return owners;
    }

    public void setOwners(List<ClaimParty> owners) {
        this.owners = owners;
    }
}
