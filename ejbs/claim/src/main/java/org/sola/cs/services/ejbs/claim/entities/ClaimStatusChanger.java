package org.sola.cs.services.ejbs.claim.entities;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractEntity;

@Table(schema = "opentenure", name = "claim")
public class ClaimStatusChanger extends AbstractEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "status_code")
    private String statusCode;

    @Column(name = "assignee_name")
    private String assigneeName;

    @Column(name = "lodgement_date")
    private Date lodgementDate;

    @Column(name = "challenge_expiry_date")
    private Date challengeExpiryDate;

    @Column(name = "decision_date")
    private Date decisionDate;

    @Column(name = "rejection_reason_code")
    private String rejectionReasonCode;

    @Column(name="issuance_date")
    private Date issuanceDate;
    
    @Column(name = "termination_date")
    private Date terminationDate;
    
    @Column(name = "termination_reason_code")
    private String terminationReasonCode;
    
    @Column(name = "create_transaction")
    private String createTransaction;
    
    @Column(name = "terminate_transaction")
    private String terminateTransaction;
    
    public ClaimStatusChanger() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public Date getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(Date decisionDate) {
        this.decisionDate = decisionDate;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getRejectionReasonCode() {
        return rejectionReasonCode;
    }

    public void setRejectionReasonCode(String rejectionReasonCode) {
        this.rejectionReasonCode = rejectionReasonCode;
    }

    public Date getLodgementDate() {
        return lodgementDate;
    }

    public void setLodgementDate(Date lodgementDate) {
        this.lodgementDate = lodgementDate;
    }

    public Date getChallengeExpiryDate() {
        return challengeExpiryDate;
    }

    public void setChallengeExpiryDate(Date challengeExpiryDate) {
        this.challengeExpiryDate = challengeExpiryDate;
    }

    public Date getIssuanceDate() {
        return issuanceDate;
    }

    public void setIssuanceDate(Date issuanceDate) {
        this.issuanceDate = issuanceDate;
    }

    public Date getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(Date terminationDate) {
        this.terminationDate = terminationDate;
    }

    public String getTerminationReasonCode() {
        return terminationReasonCode;
    }

    public void setTerminationReasonCode(String terminationReasonCode) {
        this.terminationReasonCode = terminationReasonCode;
    }

    public String getCreateTransaction() {
        return createTransaction;
    }

    public void setCreateTransaction(String createTransaction) {
        this.createTransaction = createTransaction;
    }

    public String getTerminateTransaction() {
        return terminateTransaction;
    }

    public void setTerminateTransaction(String terminateTransaction) {
        this.terminateTransaction = terminateTransaction;
    }
}
