package org.sola.cs.services.ejbs.claim.entities;

import java.util.Date;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "claim_share")
@DefaultSorter(sortString = "status, registration_date")
public class ClaimShare extends AbstractVersionedEntity {
    public static final String STATUS_ACTIVE = "a";
    public static final String STATUS_HISTORIC = "h";
    
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
    @Column
    private String status;
    @Column(name = "registration_date")
    private Date registrationDate;
    @Column(name = "termination_date")
    private Date terminationDate;
    
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(Date terminationDate) {
        this.terminationDate = terminationDate;
    }
}
