package org.sola.cs.services.ejbs.claim.entities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.common.ClaimStatusConstants;
import org.sola.services.common.repository.AccessFunctions;
import org.sola.services.common.repository.ChildEntity;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.RepositoryUtility;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;
import org.sola.cs.services.ejb.system.br.Result;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;

@Table(schema = "opentenure", name = "claim")
public class Claim extends AbstractVersionedEntity {

    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "nr", updatable = false)
    private String nr;
    @Column(name = "start_date")
    private Date startDate;
    @Column(name = "land_use_code")
    private String landUseCode;
    @Column
    private String notes;
    @Column(name = "north_adjacency")
    private String northAdjacency;
    @Column(name = "south_adjacency")
    private String southAdjacency;
    @Column(name = "east_adjacency")
    private String eastAdjacency;
    @Column(name = "west_adjacency")
    private String westAdjacency;
    @Column(name = "assignee_name", insertable = false, updatable = false)
    private String assigneeName;
    @Column(name = "lodgement_date", insertable = false, updatable = false)
    private Date lodgementDate;
    @Column(name = "challenge_expiry_date")
    private Date challengeExpiryDate;
    @Column(name = "decision_date", insertable = false, updatable = false)
    private Date decisionDate;
    @Column(name = "description")
    private String description;
    @Column(name = "challenged_claim_id")
    private String challengedClaimId;
    @Column(name = "claimant_id", updatable = false)
    private String claimantId;
    @ChildEntity(insertBeforeParent = true, childIdField = "claimantId")
    private ClaimParty claimant;
    @ChildEntityList(parentIdField = "claimId", cascadeDelete = true)
    private List<ClaimShare> shares;
    @ChildEntityList(parentIdField = "claimId", cascadeDelete = true)
    private List<Restriction> restrictions;
    @ChildEntityList(parentIdField = "claimId", cascadeDelete = true)
    private List<ClaimLocation> locations;
    @ChildEntityList(parentIdField = "claimId", cascadeDelete = true)
    private List<ClaimComment> comments;
    @ChildEntityList(parentIdField = "claimId", childIdField = "attachmentId",
            cascadeDelete = false, manyToManyClass = ClaimUsesAttachment.class)
    private List<Attachment> attachments;
    @ChildEntity(insertBeforeParent = false, parentIdField = "claimId")
    private FormPayload dynamicForm;
    @Column(name = "mapped_geometry")
    @AccessFunctions(onSelect = "ST_AsText(mapped_geometry)",
            onChange = "ST_GeomFromText(#{mappedGeometry})")
    private String mappedGeometry;
    @Column(name = "gps_geometry")
    @AccessFunctions(onSelect = "ST_AsText(gps_geometry)",
            onChange = "ST_GeomFromText(#{gpsGeometry})")
    private String gpsGeometry;
    @Column(name = "status_code", insertable = false, updatable = false)
    private String statusCode;
    @Column(name = "recorder_name", updatable = false)
    private String recorderName;
    @Column(name = "type_code")
    private String typeCode;
    @Column(name = "rejection_reason_code")
    private String rejectionReasonCode;
    @Column(name = "rowversion", updatable = false, insertable = false)
    private int version;
    @Column(name = "claim_area")
    private Long claimArea;
    @Column(name = "issuance_date")
    private Date issuanceDate;
    @Column(name = "termination_date")
    private Date terminationDate;
    @Column(name = "termination_reason_code")
    private String terminationReasonCode;
    @Column(name = "create_transaction")
    private String createTransaction;
    @Column(name = "terminate_transaction")
    private String terminateTransaction;
    private List<Claim> parentClaims;
    private List<Claim> childClaims;

    public static final String PARAM_CHALLENGED_ID = "challengeId";
    public static final String PARAM_CLAIM_NUMBER = "claimNumber";
    public static final String PARAM_TRANSACTION = "transact";
    public static final String WHERE_BY_CHALLENGED_ID = "challenged_claim_id = #{ " + PARAM_CHALLENGED_ID + "}";
    public static final String WHERE_BY_CLAIM_NUMBER = "nr = #{ " + PARAM_CLAIM_NUMBER + "}";
    public static final String WHERE_BY_TERMINTATE_TRANSACTION = "terminate_transaction = #{" + PARAM_TRANSACTION + "}";
    public static final String WHERE_BY_CREATE_TRANSACTION = "create_transaction = #{" + PARAM_TRANSACTION + "}";

    public Claim() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNr() {
        return nr;
    }

    public void setNr(String nr) {
        this.nr = nr;
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

    public Date getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(Date decisionDate) {
        this.decisionDate = decisionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChallengedClaimId() {
        return challengedClaimId;
    }

    public void setChallengedClaimId(String challengedClaimId) {
        this.challengedClaimId = challengedClaimId;
    }

    public String getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(String claimantId) {
        this.claimantId = claimantId;
    }

    public ClaimParty getClaimant() {
        return claimant;
    }

    public void setClaimant(ClaimParty claimant) {
        this.claimant = claimant;
        if (claimant != null) {
            this.setClaimantId(claimant.getId());
        }
    }

    public List<ClaimShare> getShares() {
        return shares;
    }

    public void setShares(List<ClaimShare> shares) {
        this.shares = shares;
    }

    public List<Restriction> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    public List<Attachment> getAttachments() {
        attachments = attachments == null ? new ArrayList<Attachment>() : attachments;
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public FormPayload getDynamicForm() {
        return dynamicForm;
    }

    public void setDynamicForm(FormPayload dynamicForm) {
        this.dynamicForm = dynamicForm;
    }

    public String getMappedGeometry() {
        return mappedGeometry;
    }

    public void setMappedGeometry(String mappedGeometry) {
        this.mappedGeometry = mappedGeometry;
    }

    public String getGpsGeometry() {
        return gpsGeometry;
    }

    public void setGpsGeometry(String gpsGeometry) {
        this.gpsGeometry = gpsGeometry;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getRecorderName() {
        return recorderName;
    }

    public void setRecorderName(String recorderName) {
        this.recorderName = recorderName;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getLandUseCode() {
        return landUseCode;
    }

    public void setLandUseCode(String landUseCode) {
        this.landUseCode = landUseCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNorthAdjacency() {
        return northAdjacency;
    }

    public void setNorthAdjacency(String northAdjacency) {
        this.northAdjacency = northAdjacency;
    }

    public String getSouthAdjacency() {
        return southAdjacency;
    }

    public void setSouthAdjacency(String southAdjacency) {
        this.southAdjacency = southAdjacency;
    }

    public String getEastAdjacency() {
        return eastAdjacency;
    }

    public void setEastAdjacency(String eastAdjacency) {
        this.eastAdjacency = eastAdjacency;
    }

    public String getWestAdjacency() {
        return westAdjacency;
    }

    public void setWestAdjacency(String westAdjacency) {
        this.westAdjacency = westAdjacency;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public List<ClaimLocation> getLocations() {
        return locations;
    }

    public ClaimLocation[] getLocationsArray() {
        if (getLocations() == null) {
            return null;
        }
        return getLocations().toArray(new ClaimLocation[getLocations().size()]);
    }

    public void setLocations(List<ClaimLocation> locations) {
        this.locations = locations;
    }

    public List<ClaimComment> getComments() {
        return comments;
    }

    public void setComments(List<ClaimComment> comments) {
        this.comments = comments;
    }

    public String getRejectionReasonCode() {
        return rejectionReasonCode;
    }

    public void setRejectionReasonCode(String rejectionReasonCode) {
        this.rejectionReasonCode = rejectionReasonCode;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Long getClaimArea() {
        return claimArea;
    }

    public void setClaimArea(Long claimArea) {
        this.claimArea = claimArea;
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

    public List<Claim> getParentClaims() {
        return parentClaims;
    }

    public void setParentClaims(List<Claim> parentClaims) {
        this.parentClaims = parentClaims;
    }

    public List<Claim> getChildClaims() {
        return childClaims;
    }

    public void setChildClaims(List<Claim> childClaims) {
        this.childClaims = childClaims;
    }

    public boolean getIsReadyForReview() {
        return getChallengeExpiryDate() != null && getStatusCode() != null
                && getStatusCode().equalsIgnoreCase(ClaimStatusConstants.UNMODERATED)
                && getChallengeExpiryDate().before(Calendar.getInstance().getTime());
    }

    private String generateNumber() {
        String result = "";
        SystemCSEJBLocal systemEJB = RepositoryUtility.tryGetEJB(SystemCSEJBLocal.class);
        if (systemEJB != null) {
            Result newNumberResult = systemEJB.checkRuleGetResultSingle("generate-claim-nr", null);
            if (newNumberResult != null && newNumberResult.getValue() != null) {
                result = newNumberResult.getValue().toString();
            }
        }
        return result;
    }

    @Override
    public void preSave() {
        if (isNew()) {
            setNr(generateNumber());
        }
        super.preSave();
    }
}
