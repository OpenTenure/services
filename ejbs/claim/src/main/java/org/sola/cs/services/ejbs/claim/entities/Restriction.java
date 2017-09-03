package org.sola.cs.services.ejbs.claim.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "restriction")
@DefaultSorter(sortString = "status, registration_date")
public class Restriction extends AbstractVersionedEntity {
    public static final String STATUS_ACTIVE = "a";
    public static final String STATUS_HISTORIC = "h";
    
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "claim_id")
    private String claimId;
    @Column(name = "type_code")
    private String typeCode;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "interest_rate")
    private BigDecimal interestRate;
    @ChildEntityList(parentIdField = "restrictionId", childIdField = "partyId",
    manyToManyClass = RestrictionParty.class)
    private List<ClaimParty> restrictingParties;
    @Column
    private String status;
    @Column(name = "registration_date")
    private Date registrationDate;
    @Column(name = "start_date")
    private Date startDate;
    @Column(name = "end_date")
    private Date endDate;
    @Column(name = "termination_date")
    private Date terminationDate;
    
    public Restriction(){
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

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public List<ClaimParty> getRestrictingParties() {
        return restrictingParties;
    }

    public void setRestrictingParties(List<ClaimParty> restrictingParties) {
        this.restrictingParties = restrictingParties;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(Date terminationDate) {
        this.terminationDate = terminationDate;
    }
}
