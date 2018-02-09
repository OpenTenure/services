package org.sola.cs.services.ejbs.claim.businesslogic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.sola.common.ClaimStatusConstants;
import org.sola.common.ConfigConstants;
import org.sola.common.DateUtility;
import org.sola.common.DynamicFormException;
import org.sola.common.EmailVariables;
import org.sola.common.RolesConstants;
import org.sola.common.SOLAException;
import org.sola.common.SOLAMD5Exception;
import org.sola.common.SOLANoDataException;
import org.sola.common.StringUtility;
import org.sola.cs.common.messaging.MessageUtility;
import org.sola.cs.common.messaging.ServiceMessage;
import org.sola.cs.services.ejb.refdata.businesslogic.RefDataCSEJBLocal;
import org.sola.cs.services.ejb.refdata.entities.AdministrativeBoundaryStatus;
import org.sola.cs.services.ejb.refdata.entities.AdministrativeBoundaryType;
import org.sola.cs.services.ejb.refdata.entities.SourceType;
import org.sola.cs.services.ejb.search.businesslogic.SearchCSEJBLocal;
import org.sola.cs.services.ejb.search.repository.entities.AdministrativeBoundarySearchResult;
import org.sola.cs.services.ejbs.claim.entities.Attachment;
import org.sola.cs.services.ejbs.claim.entities.AttachmentBinary;
import org.sola.cs.services.ejbs.claim.entities.AttachmentChunk;
import org.sola.cs.services.ejbs.claim.entities.Claim;
import org.sola.cs.services.ejbs.claim.entities.ClaimComment;
import org.sola.cs.services.ejbs.claim.entities.ClaimShare;
import org.sola.cs.services.ejbs.claim.entities.ClaimStatus;
import org.sola.cs.services.ejbs.claim.entities.ClaimStatusChanger;
import org.sola.cs.services.ejbs.claim.entities.ClaimParty;
import org.sola.cs.services.ejbs.claim.entities.ClaimPermissions;
import org.sola.cs.services.ejbs.claim.entities.ClaimUsesAttachment;
import org.sola.cs.services.ejbs.claim.entities.FieldConstraintType;
import org.sola.cs.services.ejbs.claim.entities.FieldPayload;
import org.sola.cs.services.ejbs.claim.entities.FieldTemplate;
import org.sola.cs.services.ejbs.claim.entities.FieldType;
import org.sola.cs.services.ejbs.claim.entities.FieldValueType;
import org.sola.cs.services.ejbs.claim.entities.FormPayload;
import org.sola.cs.services.ejbs.claim.entities.FormTemplate;
import org.sola.cs.services.ejbs.claim.entities.LandUse;
import org.sola.cs.services.ejbs.claim.entities.RejectionReason;
import org.sola.cs.services.ejbs.claim.entities.SectionElementPayload;
import org.sola.cs.services.ejbs.claim.entities.SectionPayload;
import org.sola.cs.services.ejbs.claim.entities.SectionTemplate;
import org.sola.services.common.EntityAction;
import org.sola.services.common.LocalInfo;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.faults.OTMissingAttachmentsException;
import org.sola.services.common.faults.SOLAObjectExistsException;
import org.sola.services.common.logging.LogUtility;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJBLocal;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.User;
import org.sola.cs.services.ejbs.claim.entities.AdministrativeBoundary;
import org.sola.cs.services.ejbs.claim.entities.Restriction;
import org.sola.cs.services.ejbs.claim.entities.TerminationReason;

/**
 * Implements methods to manage the claim and it's related objects
 */
@Stateless
@EJB(name = "java:app/ClaimEJBLocal", beanInterface = ClaimEJBLocal.class)
public class ClaimEJB extends AbstractEJB implements ClaimEJBLocal {

    @EJB
    SystemCSEJBLocal systemEjb;

    @EJB
    AdminCSEJBLocal adminEjb;

    @EJB
    RefDataCSEJBLocal refDataEjb;

    @EJB
    SearchCSEJBLocal searchEjb;

    private static final int DPI = 96;
    private static final String resourcesPath = "/styles/";
    private final int mapMargin = 30;
    private final int minGridCuts = 1;
    private final int coordWidth = 67;
    private final int roundNumber = 5;

    /**
     * Sets the entity package for the EJB to
     * Claim.class.getPackage().getName(). This is used to restrict the save and
     * retrieval of Code Entities.
     *
     * @see AbstractEJB#getCodeEntity(java.lang.Class, java.lang.String,
     * java.lang.String) AbstractEJB.getCodeEntity
     * @see AbstractEJB#getCodeEntityList(java.lang.Class, java.lang.String)
     * AbstractEJB.getCodeEntityList
     * @see
     * AbstractEJB#saveCodeEntity(org.sola.services.common.repository.entities.AbstractCodeEntity)
     * AbstractEJB.saveCodeEntity
     */
    @Override
    protected void postConstruct() {
        setEntityPackage(Claim.class.getPackage().getName());
    }

    /**
     * Returns list of claim statuses
     *
     * @param languageCode
     * @return
     */
    @Override
    public List<ClaimStatus> getClaimStatuses(String languageCode) {
        return getRepository().getCodeList(ClaimStatus.class, languageCode);
    }

    /**
     * Returns claim status by code
     *
     * @param code Code of status
     * @param languageCode Locale code
     * @return
     */
    @Override
    public ClaimStatus getClaimStatus(String code, String languageCode) {
        return getRepository().getCode(ClaimStatus.class, code, languageCode);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public Claim getClaim(String id) {
        Claim result = null;
        if (id != null) {
            result = getRepository().getEntity(Claim.class, id);
            // Populate parent and child lists
            if (result != null && !StringUtility.isEmpty(result.getCreateTransaction())) {
                // Get parents
                HashMap params = new HashMap();
                params.put(CommonSqlProvider.PARAM_WHERE_PART, Claim.WHERE_BY_TERMINTATE_TRANSACTION);
                params.put(Claim.PARAM_TRANSACTION, result.getCreateTransaction());
                result.setParentClaims(getRepository().getEntityList(Claim.class, params));
            }
            if (result != null && !StringUtility.isEmpty(result.getTerminateTransaction())) {
                // Get children
                HashMap params = new HashMap();
                params.put(CommonSqlProvider.PARAM_WHERE_PART, Claim.WHERE_BY_CREATE_TRANSACTION);
                params.put(Claim.PARAM_TRANSACTION, result.getTerminateTransaction());
                result.setChildClaims(getRepository().getEntityList(Claim.class, params));
            }
        }
        return result;
    }

    /**
     * Returns challenging claims by challenged claim id
     *
     * @param challengedId Claim ID that is challenged
     * @return
     */
    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<Claim> getChallengingClaimsByChallengedId(String challengedId) {
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, Claim.WHERE_BY_CHALLENGED_ID);
        params.put(Claim.PARAM_CHALLENGED_ID, challengedId);
        return getRepository().getEntityList(Claim.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM, RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM})
    public Claim transferClaim(Claim claim, String languageCode) {
        if (claim == null) {
            throw new SOLAException(ServiceMessage.GENERAL_OBJECT_IS_NULL);
        }
        claim.setVersion(claim.getVersion() + 1);
        Date currentTime = Calendar.getInstance().getTime();
        String userName = getUserName();

        // Set share registration and termination date
        if (claim.getShares() != null) {
            for (ClaimShare share : claim.getShares()) {
                // Assign user name if empty
                if (share.getOwners() != null) {
                    for (ClaimParty party : share.getOwners()) {
                        if (StringUtility.isEmpty(party.getUserName())) {
                            party.setUserName(userName);
                        }
                    }
                }

                if (StringUtility.empty(share.getStatus()).equalsIgnoreCase(ClaimShare.STATUS_HISTORIC) && share.getTerminationDate() == null) {
                    share.setTerminationDate(currentTime);
                }
                if ((StringUtility.isEmpty(share.getStatus())
                        || StringUtility.empty(share.getStatus()).equalsIgnoreCase(ClaimShare.STATUS_ACTIVE))
                        && share.getRegistrationDate() == null) {
                    share.setRegistrationDate(currentTime);
                }
            }
        }
        return getRepository().saveEntity(claim);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM, RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM})
    public Claim registerMortgage(Claim claim, String languageCode) {
        if (claim == null) {
            throw new SOLAException(ServiceMessage.GENERAL_OBJECT_IS_NULL);
        }

        String userName = getUserName();

        if (claim.getRestrictions() != null) {
            for (Restriction restriction : claim.getRestrictions()) {
                if (restriction.getRestrictingParties() != null) {
                    for (ClaimParty party : restriction.getRestrictingParties()) {
                        if (StringUtility.isEmpty(party.getUserName())) {
                            party.setUserName(userName);
                        }
                    }
                }
            }
        }

        claim.setVersion(claim.getVersion() + 1);
        return getRepository().saveEntity(claim);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM, RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM})
    public Restriction terminateRestriction(String restrictionId) {
        if (StringUtility.isEmpty(restrictionId)) {
            return null;
        }
        Restriction restriction = getRepository().getEntity(Restriction.class, restrictionId);
        if (restriction == null || !StringUtility.empty(restriction.getStatus()).equalsIgnoreCase(Restriction.STATUS_ACTIVE)) {
            return null;
        }
        restriction.setStatus(Restriction.STATUS_HISTORIC);
        restriction.setTerminationDate(Calendar.getInstance().getTime());
        getRepository().saveEntity(restriction);
        return getRepository().getEntity(Restriction.class, restrictionId);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_MODERATE_CLAIM})
    public void mergeClaims(List<Claim> oldClaims, Claim newClaim) {
        if (oldClaims == null || oldClaims.size() < 1 || newClaim == null) {
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
        }

        // Make checks
        checkClaimToAdd(newClaim);
        if (oldClaims.size() < 2) {
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_MERGE_WRONG_COUNT);
        }

        Date today = Calendar.getInstance().getTime();
        String transactionId = UUID.randomUUID().toString();

        // Update new claim to set creations transaction
        ClaimStatusChanger claimChanger = getRepository().getEntity(ClaimStatusChanger.class, newClaim.getId());
        if (claimChanger != null) {
            claimChanger.setCreateTransaction(transactionId);
            getRepository().saveEntity(claimChanger);
        }

        // Make historic old claims
        for (Claim claim : oldClaims) {
            checkClaimToAdd(claim);
            claimChanger = getRepository().getEntity(ClaimStatusChanger.class, claim.getId());
            if (claimChanger != null) {
                claimChanger.setTerminationDate(today);
                claimChanger.setTerminateTransaction(transactionId);
                claimChanger.setTerminationReasonCode(TerminationReason.CODE_MERGE);
                claimChanger.setAssigneeName(null);
                claimChanger.setStatusCode(ClaimStatusConstants.HISTORIC);
                getRepository().saveEntity(claimChanger);
            }
        }
    }

    @Override
    @RolesAllowed({RolesConstants.CS_MODERATE_CLAIM})
    public void splitClaim(Claim oldClaim, List<Claim> newClaims) {
        if (newClaims == null || newClaims.size() < 1 || oldClaim == null) {
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
        }

        // Make checks
        checkClaimToAdd(oldClaim);
        if (newClaims.size() < 2) {
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_SPLIT_WRONG_COUNT);
        }

        Date today = Calendar.getInstance().getTime();
        String transactionId = UUID.randomUUID().toString();

        // Make historic old claim
        ClaimStatusChanger claimChanger = getRepository().getEntity(ClaimStatusChanger.class, oldClaim.getId());
        if (claimChanger != null) {
            claimChanger.setTerminationDate(today);
            claimChanger.setTerminateTransaction(transactionId);
            claimChanger.setTerminationReasonCode(TerminationReason.CODE_SPLIT);
            claimChanger.setAssigneeName(null);
            claimChanger.setStatusCode(ClaimStatusConstants.HISTORIC);
            getRepository().saveEntity(claimChanger);
        }

        // Update new claims to set creations transaction
        for (Claim claim : newClaims) {
            checkClaimToAdd(claim);
            claimChanger = getRepository().getEntity(ClaimStatusChanger.class, claim.getId());
            if (claimChanger != null) {
                claimChanger.setCreateTransaction(transactionId);
                getRepository().saveEntity(claimChanger);
            }
        }
    }

    private boolean checkClaimToAdd(Claim claim) {
        // Check status 
        if (!claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.MODERATED)) {
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_MUST_BE_MODERATED, new Object[]{claim.getNr()});
        }
        // Check restrictions
        if (claim.getRestrictions() != null) {
            for (Restriction restriction : claim.getRestrictions()) {
                if (restriction.getStatus().equalsIgnoreCase("a")) {
                    throw new SOLAException(ServiceMessage.OT_WS_CLAIM_HAS_RESTRICTIONS, new Object[]{claim.getNr()});
                }
            }
        }
        return true;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM, RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM})
    public Claim saveClaim(Claim claim, String languageCode) {
        if (claim == null) {
            throw new SOLAException(ServiceMessage.GENERAL_OBJECT_IS_NULL);
        }

        boolean newClaim = claim.isNew();
        boolean fullValidation = true;
        if (newClaim || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.CREATED)) {
            fullValidation = false;
        }

        // Clean up claim geometry from Myanmar characters
        claim.setGpsGeometry(cleanupGeometry(claim.getGpsGeometry()));
        claim.setMappedGeometry(cleanupGeometry(claim.getMappedGeometry()));

        validateClaim(claim, languageCode, fullValidation, true);
        String userName = getUserName();
        Claim challengedClaim = null;

        // If geometry is empty, set it to null
        if (StringUtility.isEmpty(claim.getMappedGeometry())) {
            claim.setMappedGeometry(null);
        }

        // If claim type is empty, set it to null
        if (StringUtility.isEmpty(claim.getTypeCode())) {
            claim.setTypeCode(null);
        }

        // If land use is empty, set it to null
        if (StringUtility.isEmpty(claim.getLandUseCode())) {
            claim.setLandUseCode(null);
        }

        if (!StringUtility.isEmpty(claim.getChallengedClaimId())) {
            challengedClaim = getRepository().getEntity(Claim.class, claim.getChallengedClaimId());
        }

        // Restrict DELETE or DISASSOSIATE actions
        if (claim.getEntityAction() == EntityAction.DELETE || claim.getEntityAction() == EntityAction.DISASSOCIATE) {
            claim.setEntityAction(EntityAction.UPDATE);
        }

        // Assign user name and expiration (if new)
        if (newClaim) {
            claim.setRecorderName(userName);
        }

        // Make sure claim version will be increased even if no changes on the core claim. 
        // We increase read only version field to make persistence framework to think there are changes
        if (!newClaim) {
            claim.setVersion(claim.getVersion() + 1);
        }

        if (!newClaim) {
            // Get old claim
            Claim oldClaim = getRepository().getEntity(Claim.class, claim.getId());
            if (oldClaim != null) {
                // Check challenge expiration date
                if (oldClaim.getChallengeExpiryDate() != null && claim.getChallengeExpiryDate() != null
                        && !oldClaim.getChallengeExpiryDate().equals(claim.getChallengeExpiryDate())) {
                    // Allow change of expiration date only for unmoderated claims
                    if (!oldClaim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.UNMODERATED)) {
                        claim.setChallengeExpiryDate(oldClaim.getChallengeExpiryDate());
                    } else {
                        // Allow change of expiration date only for unmoderated claims and users with Reviewer/Moderator roles
                        if (!isInRole(RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM)) {
                            claim.setChallengeExpiryDate(oldClaim.getChallengeExpiryDate());
                        }
                        // Don't allow change of expiration date if it's already expired
                        if (oldClaim.getChallengeExpiryDate().before(Calendar.getInstance().getTime())) {
                            claim.setChallengeExpiryDate(oldClaim.getChallengeExpiryDate());
                        }
                    }
                }
            }
        }

        // Save claim
        claim = getRepository().saveEntity(claim);

        // Clean up chunks just in case
        deleteClaimChunks(claim.getId());

        // send notifications only if claim is not new
        if (!newClaim) {
            String bodyName;
            String subjectName;

            if (challengedClaim != null) {
                bodyName = ConfigConstants.EMAIL_MSG_CLAIM_CHALLENGE_UPDATED_BODY;
                subjectName = ConfigConstants.EMAIL_MSG_CLAIM_CHALLENGE_UPDATED_SUBJECT;
            } else {
                bodyName = ConfigConstants.EMAIL_MSG_CLAIM_UPDATED_BODY;
                subjectName = ConfigConstants.EMAIL_MSG_CLAIM_UPDATED_SUBJECT;
            }

            // send notification
            sendNotification(claim, getChallengingClaimsByChallengedId(claim.getId()), challengedClaim, bodyName, subjectName);
        }

        return getRepository().getEntity(Claim.class, claim.getId());
    }

    private String cleanupGeometry(String geom) {
        if (geom == null) {
            return null;
        }
        return geom.replace("၀", "0").replace("၁", "1").replace("၂", "2").replace("၃", "3")
                .replace("၄", "4").replace("၅", "5").replace("၆", "6").replace("၇", "7")
                .replace("၈", "8").replace("၉", "9");
    }

    private boolean validateClaim(Claim claim, String languageCode, boolean fullValidation, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.GENERAL_OBJECT_IS_NULL);
            } else {
                return false;
            }
        }

        boolean newClaim = claim.isNew();

        // Check claim fields
        if (StringUtility.isEmpty(claim.getId())) {
            if (!newClaim) {
                if (throwException) {
                    throw new SOLAException(ServiceMessage.OT_WS_CLAIM_ID_REQUIERD);
                } else {
                    return false;
                }
            } else {
                claim.setId(UUID.randomUUID().toString());
            }
        }

        String requireSpatial = systemEjb.getSetting(ConfigConstants.REQUIRES_SPATIAL, "1");

        if (fullValidation && requireSpatial.equals("1")
                && StringUtility.isEmpty(claim.getMappedGeometry())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_GEOMETRY_REQUIERD);
            } else {
                return false;
            }
        }

        // Check claim type
        if (fullValidation && StringUtility.isEmpty(claim.getTypeCode())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_TYPE_REQUIERD);
            } else {
                return false;
            }
        }

        // Check land use
        if (fullValidation && StringUtility.isEmpty(claim.getLandUseCode())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_LAND_USE_REQUIERD);
            } else {
                return false;
            }
        }

        // Check area of intereset
        if (!StringUtility.isEmpty(claim.getMappedGeometry())
                && !claimWithinCommunityArea(claim.getMappedGeometry())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_OUTSIDE_COMMUNITY);
            } else {
                return false;
            }
        }

        // Check claimant
        if (claim.getClaimant() == null
                || (claim.getClaimant().getEntityAction() != null
                && (claim.getClaimant().getEntityAction().equals(EntityAction.DELETE)
                || claim.getClaimant().getEntityAction().equals(EntityAction.DISASSOCIATE)))) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CLAIMANT_REQUIERD);
            } else {
                return false;
            }
        }

        if (StringUtility.isEmpty(claim.getClaimant().getId())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CLAIMANT_ID_REQUIERD);
            } else {
                return false;
            }
        }

        if (StringUtility.isEmpty(claim.getClaimant().getName())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CLAIMANT_NAME_REQUIERD);
            } else {
                return false;
            }
        }

        if (newClaim) {
            // Check claimant doesn't exist
            if (getRepository().getEntity(ClaimParty.class, claim.getClaimant().getId()) != null) {
                if (throwException) {
                    throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CLAIMANT_EXISTS);
                } else {
                    return false;
                }
            }
        }

        // Only recorders can submit new claims
        if (newClaim && !isInRole(RolesConstants.CS_RECORD_CLAIM)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            } else {
                return false;
            }
        }

        String userName = getUserName();
        boolean canEditOtherClaims = isInRole(RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM);

        // Check claim status and expiration date
        Claim oldClaim = null;

        if (!newClaim) {
            // get claim from DB to have real expiration date
            oldClaim = getRepository().getEntity(Claim.class, claim.getId());
            if (oldClaim != null) {
                // check claim is editable
                if (!canEditClaim(oldClaim, throwException)) {
                    return false;
                }

                // If claim is a challenge claim, restrcit transforming it to the claim. 
                if (!StringUtility.isEmpty(oldClaim.getChallengedClaimId())
                        && StringUtility.isEmpty(claim.getChallengedClaimId())) {
                    claim.setChallengedClaimId(oldClaim.getChallengedClaimId());
                }
            }
        }

        // Check user name on the claimant record
        if (!claim.getClaimant().isNew()) {
            if (!canEditOtherClaims && !claim.getClaimant().getUserName().equalsIgnoreCase(userName)) {
                if (throwException) {
                    throw new SOLAException(ServiceMessage.EXCEPTION_OBJECT_ACCESS_RIGHTS);
                } else {
                    return false;
                }
            }
        } else {
            claim.getClaimant().setUserName(userName);
        }

        // Check user name on the owners record
        if (claim.getShares() != null && claim.getShares().size() > 0) {
            for (ClaimShare share : claim.getShares()) {
                if (share.getOwners() != null && share.getOwners().size() > 0) {
                    for (ClaimParty owner : share.getOwners()) {
                        if (!owner.isNew()) {
                            if (!canEditOtherClaims && !owner.getUserName().equalsIgnoreCase(userName)) {
                                if (throwException) {
                                    throw new SOLAException(ServiceMessage.EXCEPTION_OBJECT_ACCESS_RIGHTS);
                                } else {
                                    return false;
                                }
                            }
                        } else {
                            owner.setUserName(userName);
                        }
                    }
                }
            }
        }

        // If challenged claim ID exists, check existance of chalennged claim, its expiration time
        String challengedClaimUser = "";
        Claim challengedClaim;

        if (!StringUtility.isEmpty(claim.getChallengedClaimId())) {
            challengedClaim = getRepository().getEntity(Claim.class, claim.getChallengedClaimId());
            if (newClaim || (oldClaim != null && StringUtility.empty(oldClaim.getChallengedClaimId()).equalsIgnoreCase(claim.getChallengedClaimId()))) {
                if (!canChallengeClaim(challengedClaim, throwException)) {
                    return false;
                }
            }
            challengedClaimUser = challengedClaim.getRecorderName();
        }

        // Check attachments exist
        if (claim.getAttachments() != null) {
            List<String> missingAttachments = new ArrayList<String>();

            for (Attachment claimAttch : claim.getAttachments()) {
                if (!claimAttch.isLoaded()) {
                    // This fix is required, because attachments already exist, 
                    // while persistence framework thinks they are new
                    claimAttch.setLoaded(true);
                    claimAttch.resetEntityAction();
                }
                Attachment attch = getRepository().getEntity(Attachment.class, claimAttch.getId());
                if (attch == null) {
                    missingAttachments.add(claimAttch.getId());
                } else // Check user name on attachment
                 if (!canEditOtherClaims && !attch.getUserName().equalsIgnoreCase(userName)
                            && !attch.getUserName().equalsIgnoreCase(challengedClaimUser)) {
                        if (throwException) {
                            throw new SOLAException(ServiceMessage.EXCEPTION_OBJECT_ACCESS_RIGHTS);
                        } else {
                            return false;
                        }
                    }
            }

            if (missingAttachments.size() > 0) {
                if (throwException) {
                    throw new OTMissingAttachmentsException(ServiceMessage.OT_WS_MISSING_SERVER_ATTACHMENTS, missingAttachments);
                } else {
                    return false;
                }
            }
        }

        // Check shares
        if (fullValidation && (claim.getShares() == null || getEntityListSize(claim.getShares()) < 1)) {
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_SHARES_REQUIRED);
        }

        if (claim.getShares() != null) {
            double totalShare = 0;

            for (ClaimShare claimShare : claim.getShares()) {
                if (claimShare.getEntityAction() == null || !claimShare.getEntityAction().equals(EntityAction.DELETE)) {
                    totalShare += (double) claimShare.getPercentage();

                    if (claimShare.getPercentage() <= 0) {
                        if (throwException) {
                            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_SHARE_ZERO_PERCENTAGE);
                        } else {
                            return false;
                        }
                    }

                    if (claimShare.getOwners() == null || getEntityListSize(claimShare.getOwners()) < 1) {
                        if (throwException) {
                            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_SHARE_OWNER_REQUIRED);
                        } else {
                            return false;
                        }
                    } else {
                        // Validate owners
                        for (ClaimParty claimParty : claimShare.getOwners()) {
                            if (claimParty.noAction() || !claimParty.getEntityAction().equals(EntityAction.DELETE)) {
                                if (StringUtility.isEmpty(claimParty.getId())) {
                                    if (throwException) {
                                        throw new SOLAException(ServiceMessage.OT_WS_CLAIM_OWNER_ID_REQUIERD);
                                    } else {
                                        return false;
                                    }
                                }

                                if (StringUtility.isEmpty(claimParty.getName())) {
                                    if (throwException) {
                                        throw new SOLAException(ServiceMessage.OT_WS_CLAIM_OWNER_NAME_REQUIERD);
                                    } else {
                                        return false;
                                    }
                                }

                                if (newClaim) {
                                    // Check owner doesn't exist
                                    if (getRepository().getEntity(ClaimParty.class, claimParty.getId()) != null) {
                                        if (throwException) {
                                            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_OWNER_EXISTS);
                                        } else {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (totalShare - 100 > 0.01 || totalShare - 100 < -0.01) {
                if (throwException) {
                    throw new SOLAException(ServiceMessage.OT_WS_CLAIM_SHARE_TOTAL_SHARE_WRONG);
                } else {
                    return false;
                }
            }
        }

        // Validate dynamic form
        if (claim.getDynamicForm() != null && claim.getDynamicForm().getFormTemplateName() != null) {
            // Get form template
            FormTemplate fTempl = getFormTemplate(claim.getDynamicForm().getFormTemplateName(), languageCode);
            if (fTempl != null) {
                if (!validateForm(fTempl, claim.getDynamicForm(), throwException)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateForm(FormTemplate formTemplate, FormPayload formPayload, boolean throwException) {
        if (formTemplate.getSectionTemplateList() != null) {
            // Check sections
            if (formPayload.getSectionPayloadList() == null
                    || formPayload.getSectionPayloadList().size() != formTemplate.getSectionTemplateList().size()) {
                if (throwException) {
                    throw new SOLAException(ServiceMessage.OT_WS_CLAIM_FORM_PAYLOAD_WRONG_SECTIONS_NUMBER);
                } else {
                    return false;
                }
            }

            for (SectionTemplate secTempl : formTemplate.getSectionTemplateList()) {
                boolean success = false;
                for (SectionPayload secPayload : formPayload.getSectionPayloadList()) {
                    if (StringUtility.empty(secTempl.getName()).equals(secPayload.getName())) {

                        int elementsCount = 0;
                        if (secPayload.getSectionElementPayloadList() != null) {
                            for (SectionElementPayload sp : secPayload.getSectionElementPayloadList()) {
                                if (sp.getEntityAction() == null || sp.getEntityAction() == EntityAction.INSERT
                                        || sp.getEntityAction() == EntityAction.UPDATE) {
                                    elementsCount += 1;
                                }
                            }
                        }

                        // Check minimum occurence 
                        if (secTempl.getMinOccurrences() > 0) {
                            if (elementsCount < secTempl.getMinOccurrences()) {
                                if (throwException) {
                                    throw new DynamicFormException(secTempl.getErrorMsg());
                                } else {
                                    return false;
                                }
                            }
                        }

                        // Check maximum occurence 
                        if (elementsCount > secTempl.getMaxOccurrences()) {
                            if (throwException) {
                                throw new DynamicFormException(secTempl.getErrorMsg());
                            } else {
                                return false;
                            }
                        }

                        // Check fields
                        if (secPayload.getSectionElementPayloadList() != null && secTempl.getFieldTemplateList() != null) {
                            for (SectionElementPayload secElement : secPayload.getSectionElementPayloadList()) {
                                if (secElement.getEntityAction() != null && (secElement.getEntityAction() == EntityAction.DELETE
                                        || secElement.getEntityAction() == EntityAction.DISASSOCIATE)) {
                                    // Skip records marked for deletion
                                    continue;
                                }

                                if (secElement.getFieldPayloadList() == null
                                        || secElement.getFieldPayloadList().size() != secTempl.getFieldTemplateList().size()) {
                                    if (throwException) {
                                        throw new SOLAException(ServiceMessage.OT_WS_CLAIM_PAYLOAD_SECTION_HAS_WRONG_FIELDS, new Object[]{StringUtility.empty(secElement.getId()), StringUtility.empty(secTempl.getName())});
                                    } else {
                                        return false;
                                    }
                                }
                                // Check field
                                for (FieldTemplate fTempl : secTempl.getFieldTemplateList()) {
                                    boolean fieldFound = false;
                                    for (FieldPayload fPayload : secElement.getFieldPayloadList()) {
                                        if (StringUtility.empty(fTempl.getName()).equals(fPayload.getName())) {
                                            try {
                                                fPayload.validate(fTempl);
                                            } catch (Exception e) {
                                                if (throwException) {
                                                    throw new DynamicFormException(e.getMessage());
                                                } else {
                                                    return false;
                                                }
                                            }
                                            fieldFound = true;
                                            break;
                                        }
                                    }
                                    if (!fieldFound) {
                                        if (throwException) {
                                            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_FIELD_TEMPLATE_NOT_FOUND_ON_ELEMENT, new Object[]{StringUtility.empty(fTempl.getName()), StringUtility.empty(secElement.getId())});
                                        } else {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }

                        success = true;
                        break;
                    }
                }
                if (!success) {
                    if (throwException) {
                        throw new SOLAException(ServiceMessage.OT_WS_CLAIM_SECTION_NOT_FOUND_ON_PAYLOAD, new Object[]{StringUtility.empty(secTempl.getName())});
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private long getClaimArea(String geom) {
        if (StringUtility.isEmpty(geom)) {
            return 0;
        }

        String sql = "SELECT ST_Area(ST_Transform(ST_SetSRID(ST_GeomFromText('%s'), 4326), 900913)) as result";
        sql = String.format(sql, geom);

        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, sql);

        ArrayList<HashMap> result = getRepository().executeSql(params);
        if (result == null || result.size() < 1) {
            return 0;
        } else {
            long area = (long) Double.parseDouble(result.get(0).get("result").toString());
            if (area % 5 >= 4) {
                return (area - (area % 5)) + 5;
            }
            return area - (area % 5);
        }
    }

    private boolean claimWithinCommunityArea(String geom) {
        if (StringUtility.isEmpty(geom)) {
            return false;
        }

        String communityArea = systemEjb.getSetting(ConfigConstants.OT_COMMUNITY_AREA, "");

        String sql = "select (ST_Contains(st_geomfromtext('%s'), st_geomfromtext('%s'))) as result";
        sql = String.format(sql, communityArea, geom);

        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, sql);

        ArrayList<HashMap> result = getRepository().executeSql(params);
        if (result == null || result.size() < 1) {
            return false;
        } else {
            return result.get(0).get("result") != null && Boolean.parseBoolean(result.get(0).get("result").toString());
        }
    }

    @Override
    public boolean canChallengeClaim(String claimId) {
        return canChallengeClaim(getRepository().getEntity(Claim.class, claimId), false);
    }

    private boolean canChallengeClaim(Claim claim, boolean throwException) {
        // Check claim exists
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        if (!isInRole(RolesConstants.CS_RECORD_CLAIM)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }

        // Check challenged claim status
        if (!claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.UNMODERATED)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CHALLENGED_CLAIM_IS_READ_ONLY);
            } else {
                return false;
            }
        }

        // Restrict submitting challenges on claim challenge
        if (!StringUtility.isEmpty(claim.getChallengedClaimId())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CHALLENGE_TO_CHALLENGE_CLAIM);
            } else {
                return false;
            }
        }

        // Restrict editing if claim expiration time elapsed 
        if (claim.getChallengeExpiryDate().before(Calendar.getInstance().getTime())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CHALLENGED_CLAIM_LOCKED);
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canSubmitClaim(String claimId) {
        return canSubmitClaim(getRepository().getEntity(Claim.class, claimId), false);
    }

    private boolean canSubmitClaim(Claim claim, boolean throwException) {
        // Check claim exists
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        if (!isInRole(RolesConstants.CS_RECORD_CLAIM)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }

        // Check claim status and owner
        if (!claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.CREATED)
                || !StringUtility.empty(claim.getRecorderName()).equalsIgnoreCase(getUserName())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CANT_SUBMIT);
            }
            return false;
        }

        return true;
    }

    @Override
    public List<SourceType> getDocumentTypesForIssuance(String langaugeCode) {
        String docTypesString = systemEjb.getSetting(ConfigConstants.DOCUMENTS_FOR_ISSUING_CERT, "");
        List<SourceType> docTypes = new ArrayList<SourceType>();

        if (!StringUtility.isEmpty(docTypesString)) {
            String[] docTypeCodes = docTypesString.replace(" ", "").split(",");
            if (docTypeCodes != null && docTypeCodes.length > 0) {

                List<SourceType> allDocTypes = refDataEjb.getCodeEntityList(SourceType.class, langaugeCode);

                if (allDocTypes != null && allDocTypes.size() > 0) {
                    for (SourceType docType : allDocTypes) {
                        for (String docTypeCode : docTypeCodes) {
                            if (docType.getCode().equalsIgnoreCase(docTypeCode)) {
                                docTypes.add(docType);
                            }
                        }
                    }
                }
            }
        }
        return docTypes;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_MODERATE_CLAIM, RolesConstants.CS_PRINT_CERTIFICATE})
    public boolean issueClaim(String claimId, final String langaugeCode) {
        if (StringUtility.isEmpty(claimId)) {
            return false;
        }

        Claim claim = getRepository().getEntity(Claim.class, claimId);
        canIssueClaim(claim, true);

        // Check documents 
        List<SourceType> docTypes = getDocumentTypesForIssuance(langaugeCode);

        if (docTypes.size() > 0) {
            String missingDocs = "";

            for (SourceType docType : docTypes) {
                boolean found = false;

                if (claim.getAttachments() != null && claim.getAttachments().size() > 0) {
                    for (Attachment attach : claim.getAttachments()) {
                        if (!StringUtility.isEmpty(attach.getTypeCode()) && attach.getTypeCode().equalsIgnoreCase(docType.getCode())) {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    // Get missing document type for error
                    if (missingDocs.length() > 0) {
                        missingDocs += ", ";
                    }
                    missingDocs += docType.getDisplayValue();
                }
            }

            if (missingDocs.length() > 0) {
                // Throw error
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CANT_ISSUE_FOUND_MISSING_DOCS, new Object[]{missingDocs});
            }
        }

        return changeClaimStatus(claimId, null, ClaimStatusConstants.ISSUED, null);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM})
    public boolean submitClaim(String claimId, String languageCode) {
        if (StringUtility.isEmpty(claimId)) {
            return false;
        }

        Claim claim = getRepository().getEntity(Claim.class, claimId);

        // Do validations
        canSubmitClaim(claim, true);
        validateClaim(claim, languageCode, true, true);

        Claim challengedClaim = null;
        if (!StringUtility.isEmpty(claim.getChallengedClaimId())) {
            challengedClaim = getRepository().getEntity(Claim.class, claim.getChallengedClaimId());
        }

        boolean result = changeClaimStatus(claimId, challengedClaim, ClaimStatusConstants.UNMODERATED, null);

        // send notifications
        if (result) {
            String bodyName;
            String subjectName;

            if (challengedClaim != null) {
                bodyName = ConfigConstants.EMAIL_MSG_CLAIM_CHALLENGE_SUBMITTED_BODY;
                subjectName = ConfigConstants.EMAIL_MSG_CLAIM_CHALLENGE_SUBMITTED_SUBJECT;
            } else {
                bodyName = ConfigConstants.EMAIL_MSG_CLAIM_SUBMITTED_BODY;
                subjectName = ConfigConstants.EMAIL_MSG_CLAIM_SUBMITTED_SUBJECT;
            }
            sendNotification(claim, getChallengingClaimsByChallengedId(claim.getId()), challengedClaim, bodyName, subjectName);
        }
        return result;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_MODERATE_CLAIM, RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_RECORD_CLAIM})
    public void deleteClaim(String claimId) {
        if (StringUtility.isEmpty(claimId)) {
            return;
        }
        // Check claim exists and has status, other than moderated
        Claim claim = getRepository().getEntity(Claim.class, claimId);
        if (!canDeleteClaim(claim, true)) {
            return;
        }

        // Delete claim challenges if any
        List<Claim> challenges = getChallengingClaimsByChallengedId(claimId);
        if (challenges != null && challenges.size() > 0) {
            for (Claim challenge : challenges) {
                // Delete attachments
                if (challenge.getAttachments() != null && challenge.getAttachments().size() > 0) {
                    for (Attachment att : challenge.getAttachments()) {
                        att.setEntityAction(EntityAction.DELETE);
                        getRepository().saveEntity(att);
                    }
                }
                // Delete challenge
                challenge.setEntityAction(EntityAction.DELETE);
                getRepository().saveEntity(challenge);
            }
        }

        // Delete attachments
        if (claim.getAttachments() != null && claim.getAttachments().size() > 0) {
            for (Attachment att : claim.getAttachments()) {
                att.setEntityAction(EntityAction.DELETE);
                getRepository().saveEntity(att);
            }
        }

        // delete claim
        claim.setEntityAction(EntityAction.DELETE);
        getRepository().saveEntity(claim);
    }

    @Override
    public boolean canDeleteClaim(String claimId) {
        return canDeleteClaim(getRepository().getEntity(Claim.class, claimId), false);
    }

    private boolean canDeleteClaim(Claim claim, boolean throwException) {
        // Check claim exists and has status, other than moderated
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        // Return true if claim has CREATED status and recorder is current user
        if (claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.CREATED)
                && StringUtility.empty(claim.getRecorderName()).equalsIgnoreCase(getUserName())) {
            return true;
        }

        if (!isInRole(RolesConstants.CS_MODERATE_CLAIM, RolesConstants.CS_REVIEW_CLAIM)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }

        if (!claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.UNMODERATED)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_MUST_BE_UNMODERATED);
            }
            return false;
        }

        // if claim is assigned to some different user, restrict deletion
        if (!StringUtility.isEmpty(claim.getAssigneeName())
                && !StringUtility.empty(claim.getAssigneeName()).equalsIgnoreCase(getUserName())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_ASSIGNED_TO_OTHER_USER);
            }
            return false;
        }
        return true;
    }

    @Override
    @RolesAllowed({
        RolesConstants.CS_RECORD_CLAIM,
        RolesConstants.CS_REVIEW_CLAIM,
        RolesConstants.CS_MODERATE_CLAIM,
        RolesConstants.CS_PRINT_CERTIFICATE})
    public AttachmentBinary saveAttachment(AttachmentBinary attachment) {
        if (attachment == null) {
            throw new SOLAException(ServiceMessage.GENERAL_OBJECT_IS_NULL);
        }

        // Check required fields
        if (attachment.getBody() == null || attachment.getBody().length < 1
                || StringUtility.isEmpty(attachment.getId())
                || StringUtility.isEmpty(attachment.getFileExtension())
                || StringUtility.isEmpty(attachment.getFileName())
                || StringUtility.isEmpty(attachment.getMimeType())
                || StringUtility.isEmpty(attachment.getTypeCode())
                || StringUtility.isEmpty(attachment.getMd5())
                || attachment.getSize() < 1) {
            throw new SOLAException(ServiceMessage.OT_WS_EMPTY_REQUIRED_FIELDS);
        }

        // Check attachment doesn't exist 
        if (getRepository().getEntity(Attachment.class, attachment.getId()) != null) {
            throw new SOLAObjectExistsException(ServiceMessage.GENERAL_OBJECT_EXIST);
        }

        // Check file size with provided value
        if (attachment.getBody().length != attachment.getSize()) {
            throw new SOLAException(ServiceMessage.GENERAL_WRONG_FILE_SIZE);
        }

        // Check MD5
        if (!StringUtility.getMD5(attachment.getBody()).equalsIgnoreCase(attachment.getMd5())) {
            throw new SOLAMD5Exception(ServiceMessage.GENERAL_WRONG_MD5);
        }

        // Set defalut values
        attachment.setUserName(getUserName());

        // Save attachment
        attachment = getRepository().saveEntity(attachment);
        deleteAttachmentChunks(attachment.getId());
        return attachment;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM, RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM})
    public AttachmentBinary saveAttachmentFromChunks(AttachmentBinary attachment) {
        if (attachment == null) {
            throw new SOLAException(ServiceMessage.GENERAL_OBJECT_IS_NULL);
        }

        // Check required fields
        if (StringUtility.isEmpty(attachment.getId())) {
            throw new SOLAException(ServiceMessage.OT_WS_EMPTY_REQUIRED_FIELDS);
        }

        // Get chunks
        ArrayList<AttachmentChunk> chunks = (ArrayList<AttachmentChunk>) getAttachmentChunks(attachment.getId());
        if (chunks == null || chunks.size() < 1) {
            throw new SOLANoDataException(ServiceMessage.OT_WS_CHUNKS_NOT_FOUND);
        }

        // Check user name
        String userName = getUserName();
        for (AttachmentChunk chunk : chunks) {
            if (!chunk.getUserName().equalsIgnoreCase(userName)) {
                throw new SOLAException(ServiceMessage.OT_WS_CHUNK_ATTACHMENT_OWNED_BY_OTHER_USER);
            }
        }

        // Merge all chunks into 1 file
        Collections.sort(chunks, new AttchmentChunkSorter());

        // Calculate array size
        int length = 0;
        for (AttachmentChunk chunk : chunks) {
            length += chunk.getBody().length;
        }

        byte[] attachmentBody = new byte[length];

        // Link chunks together
        length = 0;
        for (AttachmentChunk chunk : chunks) {
            System.arraycopy(chunk.getBody(), 0, attachmentBody, length, chunk.getBody().length);
            length += chunk.getBody().length;
        }

        attachment.setBody(attachmentBody);

        return saveAttachment(attachment);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM, RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM})
    public AttachmentChunk saveAttachmentChunk(AttachmentChunk chunk) {
        if (chunk == null || chunk.getBody() == null || chunk.getBody().length < 1) {
            throw new SOLAException(ServiceMessage.OT_WS_CHUNK_EMPTY);
        }

        // Check required fields
        if (StringUtility.isEmpty(chunk.getClaimId()) || StringUtility.isEmpty(chunk.getAttachmentId())
                || StringUtility.isEmpty(chunk.getId()) || StringUtility.isEmpty(chunk.getMd5())) {
            throw new SOLAException(ServiceMessage.OT_WS_EMPTY_REQUIRED_FIELDS);
        }

        // Check chunk doesn't exist
        if (getRepository().getEntity(AttachmentChunk.class, chunk.getId()) != null) {
            throw new SOLAObjectExistsException(ServiceMessage.OT_WS_CHUNK_EXISTS);
        }

        // Check file size with provided value
        if (chunk.getBody().length != chunk.getSize()) {
            throw new SOLAException(ServiceMessage.OT_WS_CHUNK_SIZE_WRONG);
        }

        // Check MD5
        if (!StringUtility.getMD5(chunk.getBody()).equalsIgnoreCase(chunk.getMd5())) {
            throw new SOLAMD5Exception(ServiceMessage.OT_WS_CHUNK_MD5_WRONG);
        }

        // Check chunk doesn't belong to attachment owned by different user
        AttachmentChunk lastChunk = getAttachmentLastChunk(chunk.getAttachmentId());
        if (lastChunk != null && !lastChunk.getUserName().equalsIgnoreCase(getUserName())) {
            throw new SOLAException(ServiceMessage.OT_WS_CHUNK_ATTACHMENT_OWNED_BY_OTHER_USER);
        }

        // Check chunk has no gaps and not overlapping
        if (lastChunk != null) {
            if ((lastChunk.getStartPosition() + lastChunk.getSize()) != chunk.getStartPosition()) {
                throw new SOLAException(ServiceMessage.OT_WS_CHUNK_WRONG_START_POSITION);
            }
        } else if (chunk.getStartPosition() != 0) {
            throw new SOLAException(ServiceMessage.OT_WS_CHUNK_START_POSITION_ZERO);
        }

        // Check size limit
        int maxFileSize = getMaxFileSize();
        if (maxFileSize > 0) {
            if (chunk.getSize() / 1024 > maxFileSize) {
                throw new SOLAException(ServiceMessage.OT_WS_CHUNK_LARGE_SIZE);
            }
        }

        // Check overall limit of loaded chunks/attachment size for the user.
        int maxUploadingSize = getUploadLimit();
        if (maxUploadingSize > 0) {
            long totalSize = chunk.getSize();

            HashMap params = new HashMap();
            params.put(CommonSqlProvider.PARAM_QUERY, AttachmentChunk.QUERY_TOTAL_SIZE_BY_USER_PER_DAY);
            params.put(AttachmentChunk.PARAM_USER_NAME, getUserName());

            Long chunksSize = getRepository().getScalar(Long.class, params);
            if (chunksSize != null) {
                totalSize += chunksSize;
            }

            params = new HashMap();
            params.put(CommonSqlProvider.PARAM_QUERY, Attachment.QUERY_TOTAL_SIZE_BY_USER_PER_DAY);
            params.put(AttachmentChunk.PARAM_USER_NAME, getUserName());

            Long attachmentsSize = getRepository().getScalar(Long.class, params);
            if (attachmentsSize != null) {
                totalSize += attachmentsSize;
            }

            if (totalSize / 1024 > maxUploadingSize) {
                throw new SOLAException(ServiceMessage.OT_WS_FILES_LOADING_LIMIT);
            }
        }

        // Assign values
        chunk.setUserName(getUserName());

        return getRepository().saveEntity(chunk);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM, RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM})
    public AttachmentChunk getAttachmentLastChunk(String attachmentId) {
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, AttachmentChunk.WHERE_BY_ATTACHMENT_ID);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, AttachmentChunk.PARAM_START_POSITION + " desc ");
        params.put(CommonSqlProvider.PARAM_LIMIT_PART, "1");
        params.put(AttachmentChunk.PARAM_ATTACHMENT_ID, attachmentId);
        return getRepository().getEntity(AttachmentChunk.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM, RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM})
    public List<AttachmentChunk> getAttachmentChunks(String attachmentId) {
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, AttachmentChunk.WHERE_BY_ATTACHMENT_ID);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, AttachmentChunk.PARAM_START_POSITION + " asc ");
        params.put(AttachmentChunk.PARAM_ATTACHMENT_ID, attachmentId);
        return getRepository().getEntityList(AttachmentChunk.class, params);
    }

    @Override
    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public AttachmentBinary getAttachment(String attachmentId) {
        return getRepository().getEntity(AttachmentBinary.class, attachmentId);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM, RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM})
    public boolean deleteAttachmentChunks(String attachmentId) {
        if (StringUtility.isEmpty(attachmentId)) {
            return false;
        }

        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, AttachmentChunk.WHERE_BY_ATTACHMENT_ID);
        params.put(AttachmentChunk.PARAM_ATTACHMENT_ID, attachmentId);
        List<AttachmentChunk> chunks = getRepository().getEntityList(AttachmentChunk.class, params);

        if (chunks != null && chunks.size() > 0) {
            String userName = getUserName();
            for (AttachmentChunk chunk : chunks) {
                if (!chunk.getUserName().equalsIgnoreCase(userName)) {
                    throw new SOLAException(ServiceMessage.OT_WS_CHUNK_ATTACHMENT_OWNED_BY_OTHER_USER);
                }
            }
            params = new HashMap();
            params.put(CommonSqlProvider.PARAM_QUERY, AttachmentChunk.DELETE_CHUNKS_BY_ATTACHMENT);
            params.put(AttachmentChunk.PARAM_ATTACHMENT_ID, attachmentId);
            getRepository().executeSql(params);
        }
        return true;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM, RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM})
    public boolean deleteClaimChunks(String claimId) {
        if (StringUtility.isEmpty(claimId)) {
            return false;
        }

        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, AttachmentChunk.WHERE_BY_CLAIM_ID);
        params.put(AttachmentChunk.PARAM_CLAIM_ID, claimId);
        List<AttachmentChunk> chunks = getRepository().getEntityList(AttachmentChunk.class, params);

        if (chunks != null && chunks.size() > 0) {
            String userName = getUserName();
            for (AttachmentChunk chunk : chunks) {
                if (!chunk.getUserName().equalsIgnoreCase(userName)) {
                    throw new SOLAException(ServiceMessage.OT_WS_CHUNK_ATTACHMENT_OWNED_BY_OTHER_USER);
                }
            }
            params = new HashMap();
            params.put(CommonSqlProvider.PARAM_QUERY, AttachmentChunk.DELETE_CHUNKS_BY_CLAIM);
            params.put(AttachmentChunk.PARAM_CLAIM_ID, claimId);
            getRepository().executeSql(params);
        }
        return true;
    }

    /**
     * Returns maximum file size in KB, that can be uploaded to the server.
     *
     * @return
     */
    @Override
    public int getMaxFileSize() {
        String maxUploadingSizeString = systemEjb.getSetting(ConfigConstants.MAX_FILE_SIZE, "10000");
        int maxUploadingSize = 0;
        if (maxUploadingSizeString != null && !maxUploadingSizeString.equals("")) {
            maxUploadingSize = Integer.parseInt(maxUploadingSizeString);
        }
        return maxUploadingSize;
    }

    /**
     * Returns maximum upload size in KB per day.
     *
     * @return
     */
    @Override
    public int getUploadLimit() {
        String maxFileSizeString = systemEjb.getSetting(ConfigConstants.MAX_UPLOADING_DAILY_LIMIT, "10000");
        int maxFileSize = 0;
        if (maxFileSizeString != null && !maxFileSizeString.equals("")) {
            maxFileSize = Integer.parseInt(maxFileSizeString);
        }
        return maxFileSize;
    }

    @Override
    public List<LandUse> getLandUses(String languageCode) {
        return getRepository().getCodeList(LandUse.class, languageCode);
    }

    @Override
    public List<RejectionReason> getRejectionReasons(String languageCode) {
        return getRepository().getCodeList(RejectionReason.class, languageCode);
    }

    /**
     * Indicate if claim can be withdrawn by the user.
     *
     * @param id Claim ID
     * @return
     */
    @Override
    public boolean canWithdrawClaim(String id) {
        return canWithdrawClaim(getRepository().getEntity(Claim.class, id), false);
    }

    private boolean canWithdrawClaim(Claim claim, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        // Check user role
        if (!isInRole(RolesConstants.CS_RECORD_CLAIM)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }

        // Check user is owner
        String userName = getUserName();
        if (!claim.getRecorderName().equalsIgnoreCase(userName)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_OBJECT_ACCESS_RIGHTS);
            }
            return false;
        }

        // Check expiration time and forbid withdrawal if expired
        if (claim.getChallengeExpiryDate() != null
                && claim.getChallengeExpiryDate().before(Calendar.getInstance().getTime())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_MODERATION_EXPIRED);
            }
            return false;
        }

        // Check claim status
        if (!claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.UNMODERATED)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_MUST_BE_UNMODERATED);
            }
            return false;
        }
        return true;
    }

    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM})
    @Override
    public boolean withdrawClaim(String id) {
        if (StringUtility.isEmpty(id)) {
            return false;
        }

        Claim claim = getRepository().getEntity(Claim.class, id);

        if (!canWithdrawClaim(claim, true)) {
            return false;
        }

        // Withdraw
        if (changeClaimStatus(id, null, ClaimStatusConstants.WITHDRAWN, null)) {
            // Get claim challenges if any and withdraw them as well
            List<Claim> challenges = getChallengingClaimsByChallengedId(id);
            if (challenges != null && challenges.size() > 0) {
                for (Claim challenge : challenges) {
                    if (!challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.WITHDRAWN)
                            && !challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.REJECTED)
                            && !challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.CREATED)) {
                        changeClaimStatus(challenge.getId(), null, ClaimStatusConstants.WITHDRAWN, null);
                    }
                }
            }

            // Send notifications
            Claim challengedClaim = null;
            String body = ConfigConstants.EMAIL_MSG_CLAIM_WITHDRAW_BODY;
            String subject = ConfigConstants.EMAIL_MSG_CLAIM_WITHDRAW_SUBJECT;

            if (!StringUtility.isEmpty(claim.getChallengedClaimId())) {
                challengedClaim = getClaim(claim.getChallengedClaimId());
                body = ConfigConstants.EMAIL_MSG_CLAIM_CHALLENGE_WITHDRAWAL_BODY;
                subject = ConfigConstants.EMAIL_MSG_CLAIM_CHALLENGE_WITHDRAWAL_SUBJECT;
            }
            sendNotification(claim, challenges, challengedClaim, body, subject);

            return true;
        }
        return false;
    }

    // Send notification to specific email
    private void sendNotification(String email, String userFullName,
            String userFirstName, String bodyName, String subjectName,
            Claim claim, Claim claimChallenge, String partyRole) {
        try {
            if (systemEjb.isEmailServiceEnabled()) {
                if (!StringUtility.isEmpty(email)) {
                    String msgBody = systemEjb.getSetting(bodyName, "");
                    String msgSubject = systemEjb.getSetting(subjectName, "");
                    msgBody = msgBody.replace(EmailVariables.FULL_USER_NAME, userFullName);
                    msgBody = msgBody.replace(EmailVariables.USER_FIRST_NAME, userFirstName);
                    msgBody = msgBody.replace(EmailVariables.CLAIM_PARTY_ROLE, partyRole);
                    msgSubject = msgSubject.replace(EmailVariables.FULL_USER_NAME, userFullName);
                    msgSubject = msgSubject.replace(EmailVariables.USER_FIRST_NAME, userFirstName);

                    if (claim != null) {
                        String claimLink = StringUtility.empty(LocalInfo.getBaseUrl()) + "/claim/ViewClaim.xhtml?id=" + claim.getId();
                        msgBody = msgBody.replace(EmailVariables.CLAIM_LINK, claimLink);
                        msgBody = msgBody.replace(EmailVariables.CLAIM_NUMBER, claim.getNr());
                        msgSubject = msgSubject.replace(EmailVariables.CLAIM_LINK, claimLink);
                        msgSubject = msgSubject.replace(EmailVariables.CLAIM_NUMBER, claim.getNr());
                        msgBody = msgBody.replace(EmailVariables.CLAIM_REJECTION_REASON, getRejectionReasonText(claim.getRejectionReasonCode()));

                        String comments = "";
                        if (claim.getComments() != null && claim.getComments().size() > 0) {
                            comments = "<ul>";
                            for (ClaimComment comment : claim.getComments()) {
                                comments += "<li>" + comment.getComment()
                                        + ".  <small>("
                                        + DateUtility.getShortDateString(comment.getCreationTime(), true)
                                        + "</small>)</li>";
                            }
                            comments += "</ul>";
                        }
                        msgBody = msgBody.replace(EmailVariables.CLAIM_COMMENTS, comments);
                    }

                    if (claimChallenge != null) {
                        String claimChallengeLink = StringUtility.empty(LocalInfo.getBaseUrl()) + "/claim/ViewClaim.xhtml?id=" + claimChallenge.getId();
                        msgBody = msgBody.replace(EmailVariables.CLAIM_CHALLENGE_LINK, claimChallengeLink);
                        msgBody = msgBody.replace(EmailVariables.CLAIM_CHALLENGE_NUMBER, claimChallenge.getNr());
                        msgSubject = msgSubject.replace(EmailVariables.CLAIM_CHALLENGE_LINK, claimChallengeLink);
                        msgSubject = msgSubject.replace(EmailVariables.CLAIM_CHALLENGE_NUMBER, claimChallenge.getNr());
                        msgBody = msgBody.replace(EmailVariables.CLAIM_CHALLENGE_REJECTION_REASON, getRejectionReasonText(claimChallenge.getRejectionReasonCode()));

                        String comments = "";
                        if (claimChallenge.getComments() != null && claimChallenge.getComments().size() > 0) {
                            comments = "<ul>";
                            for (ClaimComment comment : claimChallenge.getComments()) {
                                comments += "<li>" + comment.getComment()
                                        + ".  <small>("
                                        + DateUtility.getShortDateString(comment.getCreationTime(), true)
                                        + "</small>)</li>";
                            }
                            comments += "</ul>";
                        }
                        msgBody = msgBody.replace(EmailVariables.CLAIM_CHALLENGE_COMMENTS, comments);
                    }
                    systemEjb.sendEmail(userFullName, email, msgBody, msgSubject);
                }
            }
        } catch (Exception e) {
            LogUtility.log("Failed to send email", e);
        }
    }

    private String getRejectionReasonText(String rejectionReasonCode) {
        if (!StringUtility.isEmpty(rejectionReasonCode)) {
            for (RejectionReason reason : getRejectionReasons(null)) {
                if (reason.getCode().equalsIgnoreCase(rejectionReasonCode)) {
                    return reason.getDisplayValue();
                }
            }
        }
        return "";
    }

    // Send notification to all parties involved in the claim
    private void sendNotification(Claim claim, List<Claim> challenges, Claim challengedClaim, String bodyName, String subjectName) {
        if (challengedClaim == null) {
            // This is normal claim

            // Send notification to recorder
            User user = adminEjb.getUserInfo(claim.getRecorderName());
            if (user != null && !StringUtility.isEmpty(user.getEmail())) {
                sendNotification(user.getEmail(), user.getFullName(), user.getFirstName(),
                        bodyName, subjectName, claim, null,
                        MessageUtility.getLocalizedMessageText(ServiceMessage.OT_WS_CLAIM_RECORDER));
            }

            // Send notification to claimant
            if (claim.getClaimant() != null && !StringUtility.isEmpty(claim.getClaimant().getEmail())) {
                sendNotification(claim.getClaimant().getEmail(),
                        claim.getClaimant().getName() + (StringUtility.isEmpty(claim.getClaimant().getLastName()) ? "" : " " + claim.getClaimant().getLastName()),
                        claim.getClaimant().getName(), bodyName, subjectName, claim, null,
                        MessageUtility.getLocalizedMessageText(ServiceMessage.OT_WS_CLAIM_CLAIMANT));
            }

            // Send to challengers
            if (challenges != null && challenges.size() > 0) {
                for (Claim challenge : challenges) {
                    if (!challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.WITHDRAWN)
                            && !challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.REJECTED)) {
                        // Send to recorder
                        User challengeRecorder = adminEjb.getUserInfo(challenge.getRecorderName());
                        if (challengeRecorder != null && !StringUtility.isEmpty(challengeRecorder.getEmail())) {
                            sendNotification(challengeRecorder.getEmail(), challengeRecorder.getFullName(),
                                    challengeRecorder.getFirstName(), bodyName, subjectName, claim, null,
                                    MessageUtility.getLocalizedMessageText(ServiceMessage.OT_WS_CLAIM_CHALLENGE_RECORDER));
                        }

                        // Send to cliamant
                        if (challenge.getClaimant() != null && !StringUtility.isEmpty(challenge.getClaimant().getEmail())) {
                            sendNotification(challenge.getClaimant().getEmail(),
                                    challenge.getClaimant().getName() + (StringUtility.isEmpty(challenge.getClaimant().getLastName()) ? "" : " " + challenge.getClaimant().getLastName()),
                                    challenge.getClaimant().getName(), bodyName, subjectName, claim, null,
                                    MessageUtility.getLocalizedMessageText(ServiceMessage.OT_WS_CLAIM_CHALLENGE_CLAIMANT));
                        }
                    }
                }
            }
        } else {
            // This is claim challenge

            // send notification to challenging parties
            User user = adminEjb.getUserInfo(claim.getRecorderName());
            if (user != null && !StringUtility.isEmpty(user.getEmail())) {
                sendNotification(user.getEmail(), user.getFullName(), user.getFirstName(),
                        bodyName, subjectName, challengedClaim, claim,
                        MessageUtility.getLocalizedMessageText(ServiceMessage.OT_WS_CLAIM_CHALLENGE_RECORDER));
            }

            if (claim.getClaimant() != null && !StringUtility.isEmpty(claim.getClaimant().getEmail())) {
                sendNotification(claim.getClaimant().getEmail(),
                        claim.getClaimant().getName() + (StringUtility.isEmpty(claim.getClaimant().getLastName()) ? "" : " " + claim.getClaimant().getLastName()),
                        claim.getClaimant().getName(), bodyName, subjectName, challengedClaim, claim,
                        MessageUtility.getLocalizedMessageText(ServiceMessage.OT_WS_CLAIM_CHALLENGE_CLAIMANT));
            }

            // send notification to challenged parties
            user = adminEjb.getUserInfo(challengedClaim.getRecorderName());
            if (user != null && !StringUtility.isEmpty(user.getEmail())) {
                sendNotification(user.getEmail(), user.getFullName(), user.getFirstName(),
                        bodyName, subjectName, challengedClaim, claim,
                        MessageUtility.getLocalizedMessageText(ServiceMessage.OT_WS_CLAIM_CHALLENGED_CLAIM_RECORDER));
            }

            if (challengedClaim.getClaimant() != null && !StringUtility.isEmpty(challengedClaim.getClaimant().getEmail())) {
                sendNotification(challengedClaim.getClaimant().getEmail(),
                        challengedClaim.getClaimant().getName() + (StringUtility.isEmpty(challengedClaim.getClaimant().getLastName()) ? "" : " " + challengedClaim.getClaimant().getLastName()),
                        challengedClaim.getClaimant().getName(), bodyName, subjectName, challengedClaim, claim,
                        MessageUtility.getLocalizedMessageText(ServiceMessage.OT_WS_CLAIM_CHALLENGED_CLAIM_CLAIMANT));
            }
        }
    }

    @Override
    public boolean canPrintClaimCertificate(String claimId, String languageCode) {
        return canPrintClaimCertificate(getRepository().getEntity(Claim.class, claimId), false);

    }

    private boolean canPrintClaimCertificate(Claim claim, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        if (claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.MODERATED)
                && isInRole(RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_PRINT_CERTIFICATE)) {
            return true;
        } else {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CERT_PRINT_NOT_ALLOWED);
            }
            return false;
        }
    }

    @Override
    public boolean canEditClaim(String claimId) {
        return canEditClaim(getRepository().getEntity(Claim.class, claimId), false);
    }

    private boolean canEditClaim(Claim claim, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        // Restrict editing of claims if they are in the final status
        if (claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.MODERATED)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.ISSUED)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.REJECTED)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.WITHDRAWN)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.HISTORIC)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_IS_READ_ONLY);
            }
            return false;
        }

        // Restrict editing if claim expiration time elapsed and user has no reviewer or moderator role
        if (claim.getChallengeExpiryDate() != null
                && claim.getChallengeExpiryDate().before(Calendar.getInstance().getTime())) {
            if (!isInRole(RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM)) {
                if (throwException) {
                    throw new SOLAException(ServiceMessage.OT_WS_CLAIM_LOCKED);
                }
                return false;
            }

            // Claim must be assigned
            // Since claim is in review or moderation state, check it's assigned to the modifying user
            if (StringUtility.isEmpty(claim.getAssigneeName())) {
                if (throwException) {
                    throw new SOLAException(ServiceMessage.OT_WS_CLAIM_MUST_BE_ASSIGNED);
                }
                return false;
            }

            // Since claim is in review or moderation state, check it's assigned to the modifying user
            if (!StringUtility.isEmpty(claim.getAssigneeName())
                    && !claim.getAssigneeName().equalsIgnoreCase(getUserName())) {
                if (throwException) {
                    throw new SOLAException(ServiceMessage.OT_WS_CLAIM_ASSIGNED_TO_OTHER_USER);
                }
                return false;
            }
        } else {
            // Allow editing of claim to Reviewers before public display expires
            if (isInRole(RolesConstants.CS_REVIEW_CLAIM)) {
                return true;
            }

            // Moderation time is not expired yet and only claim with CREATED status can be changed
            if (!claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.CREATED)) {
                if (throwException) {
                    throw new SOLAException(ServiceMessage.OT_WS_CLAIM_IS_READ_ONLY);
                }
                return false;
            }
            // If claim has CRAETED status, check its recorder
            if (!claim.getRecorderName().equalsIgnoreCase(getUserName())) {
                if (throwException) {
                    throw new SOLAException(ServiceMessage.EXCEPTION_OBJECT_ACCESS_RIGHTS);
                }
                return false;
            }
        }
        return true;
    }

    private boolean isClaimExpired(Claim claim) {
        return claim.getChallengeExpiryDate() != null
                && claim.getChallengeExpiryDate().before(Calendar.getInstance().getTime());
    }

    private boolean changeClaimStatus(String claimId, Claim challengedClaim, String statusCode, String rejectionCode) {
        ClaimStatusChanger claimStatusChanger = getRepository().getEntity(ClaimStatusChanger.class, claimId);
        if (claimStatusChanger == null) {
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
        }

        // Return true if status already the same as the one passed to be assigned.
        if (claimStatusChanger.getStatusCode().equalsIgnoreCase(statusCode)) {
            return true;
        }

        if (statusCode.equalsIgnoreCase(ClaimStatusConstants.UNMODERATED)
                && claimStatusChanger.getLodgementDate() == null) {
            claimStatusChanger.setLodgementDate(Calendar.getInstance().getTime());
            claimStatusChanger.setChallengeExpiryDate(Calendar.getInstance().getTime());

            // Assign same expiration date to challenging claim
            if (challengedClaim != null) {
                claimStatusChanger.setChallengeExpiryDate(challengedClaim.getChallengeExpiryDate());
            } else {
                String challengeExpiryDateString = systemEjb.getSetting(ConfigConstants.MODERATION_DATE, "");
                Date challengeExpiryDate = null;

                if (!StringUtility.isEmpty(challengeExpiryDateString)) {
                    challengeExpiryDate = DateUtility.convertToDate(challengeExpiryDateString, "yyyy-MM-dd");
                }

                if (challengeExpiryDate != null && challengeExpiryDate.after(Calendar.getInstance().getTime())) {
                    claimStatusChanger.setChallengeExpiryDate(challengeExpiryDate);
                } else {
                    int days = Integer.parseInt(systemEjb.getSetting(ConfigConstants.MODERATION_DAYS, "30"));
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, days);
                    claimStatusChanger.setChallengeExpiryDate(cal.getTime());
                }
            }
        }

        if (statusCode.equalsIgnoreCase(ClaimStatusConstants.MODERATED)) {
            claimStatusChanger.setDecisionDate(Calendar.getInstance().getTime());
        }

        if (statusCode.equalsIgnoreCase(ClaimStatusConstants.REJECTED) && !StringUtility.isEmpty(rejectionCode)) {
            claimStatusChanger.setRejectionReasonCode(rejectionCode);
        }

        if (statusCode.equalsIgnoreCase(ClaimStatusConstants.ISSUED)) {
            claimStatusChanger.setIssuanceDate(Calendar.getInstance().getTime());
        }

        claimStatusChanger.setStatusCode(statusCode);
        getRepository().saveEntity(claimStatusChanger);
        return true;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_MODERATE_CLAIM, RolesConstants.CS_REVIEW_CLAIM})
    public boolean rejectClaim(String id, String rejectionReasonCode) {
        if (StringUtility.isEmpty(id)) {
            return false;
        }

        Claim claim = getRepository().getEntity(Claim.class, id);
        if (!canRejectClaim(claim, true)) {
            return false;
        }

        // Reject
        if (changeClaimStatus(id, null, ClaimStatusConstants.REJECTED, rejectionReasonCode)) {
            // Get claim challenges if any and reject them as well
            List<Claim> challenges = getChallengingClaimsByChallengedId(id);
            if (challenges != null && challenges.size() > 0) {
                for (Claim challenge : challenges) {
                    if (!challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.WITHDRAWN)
                            && !challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.CREATED)
                            && !challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.REJECTED)) {
                        changeClaimStatus(challenge.getId(), null, ClaimStatusConstants.REJECTED, null);
                    }
                }
            }

            // Send notifications
            Claim challengedClaim = null;
            String body = ConfigConstants.EMAIL_MSG_CLAIM_REJECT_BODY;
            String subject = ConfigConstants.EMAIL_MSG_CLAIM_REJECT_SUBJECT;

            if (!StringUtility.isEmpty(claim.getChallengedClaimId())) {
                challengedClaim = getClaim(claim.getChallengedClaimId());
                body = ConfigConstants.EMAIL_MSG_CLAIM_CHALLENGE_REJECTION_BODY;
                subject = ConfigConstants.EMAIL_MSG_CLAIM_CHALLENGE_REJECTION_SUBJECT;
            }
            sendNotification(claim, challenges, challengedClaim, body, subject);

            return true;
        }
        return false;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_REVIEW_CLAIM})
    public boolean approveClaimReview(String id) {
        if (StringUtility.isEmpty(id)) {
            return false;
        }

        Claim claim = getRepository().getEntity(Claim.class, id);
        if (!canApproveClaimReview(claim, true)) {
            return false;
        }

        // Approve claim review
        if (changeClaimStatus(id, null, ClaimStatusConstants.REVIEWED, null)) {
            // Get claim challenges if any and approve them as well
            List<Claim> challenges = getChallengingClaimsByChallengedId(id);
            if (challenges != null && challenges.size() > 0) {
                for (Claim challenge : challenges) {
                    if (!challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.WITHDRAWN)
                            && !challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.CREATED)
                            && !challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.REJECTED)) {
                        changeClaimStatus(challenge.getId(), null, ClaimStatusConstants.REVIEWED, null);
                    }
                }
            }

            // Send notifications
            Claim challengedClaim = null;
            String body = ConfigConstants.EMAIL_MSG_CLAIM_REVIEW_APPROVE_BODY;
            String subject = ConfigConstants.EMAIL_MSG_CLAIM_REVIEW_APPROVE_SUBJECT;

            if (!StringUtility.isEmpty(claim.getChallengedClaimId())) {
                challengedClaim = getClaim(claim.getChallengedClaimId());
                body = ConfigConstants.EMAIL_MSG_CLAIM_CHALLENGE_REVIEW_BODY;
                subject = ConfigConstants.EMAIL_MSG_CLAIM_CHALLENGE_REVIEW_SUBJECT;
            }
            sendNotification(claim, challenges, challengedClaim, body, subject);

            return true;
        }
        return false;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_MODERATE_CLAIM})
    public boolean revertClaimReview(String claimId) {
        if (StringUtility.isEmpty(claimId)) {
            return false;
        }

        Claim claim = getRepository().getEntity(Claim.class, claimId);
        if (!canRevertClaimReview(claim, true)) {
            return false;
        }

        // Revert claim review
        if (changeClaimStatus(claimId, null, ClaimStatusConstants.UNMODERATED, null)) {
            List<Claim> challenges = getChallengingClaimsByChallengedId(claimId);
            if (challenges != null && challenges.size() > 0) {
                for (Claim challenge : challenges) {
                    if (challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.REVIEWED)) {
                        changeClaimStatus(challenge.getId(), null, ClaimStatusConstants.UNMODERATED, null);
                    }
                }
            }
        }
        return false;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_MODERATE_CLAIM})
    public boolean approveClaimModeration(String id) {
        if (StringUtility.isEmpty(id)) {
            return false;
        }

        Claim claim = getRepository().getEntity(Claim.class, id);
        if (!canApproveClaimModeration(claim, true)) {
            return false;
        }

        // Set registration date and approve claim moderation
        if (claim.getShares() != null) {
            for (ClaimShare share : claim.getShares()) {
                share.setRegistrationDate(Calendar.getInstance().getTime());
            }
        }

        if (changeClaimStatus(id, null, ClaimStatusConstants.MODERATED, null)) {
            List<Claim> challenges = getChallengingClaimsByChallengedId(id);

            // Send notifications
            Claim challengedClaim = null;
            String body = ConfigConstants.EMAIL_MSG_CLAIM_MODERATION_APPROVE_BODY;
            String subject = ConfigConstants.EMAIL_MSG_CLAIM_MODERATION_APPROVE_SUBJECT;

            if (!StringUtility.isEmpty(claim.getChallengedClaimId())) {
                challengedClaim = getClaim(claim.getChallengedClaimId());
                body = ConfigConstants.EMAIL_MSG_CLAIM_CHALLENGE_MODERATION_BODY;
                subject = ConfigConstants.EMAIL_MSG_CLAIM_CHALLENGE_MODERATION_SUBJECT;
            }
            sendNotification(claim, challenges, challengedClaim, body, subject);

            return true;
        }
        return false;
    }

    @Override
    public boolean canRejectClaim(String id) {
        return canRejectClaim(getRepository().getEntity(Claim.class, id), false);
    }

    private boolean canRejectClaim(Claim claim, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        // Check user role
        if (!isInRole(RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }

        // Check user is assigned to the claim
        String userName = getUserName();
        if (!StringUtility.empty(claim.getAssigneeName()).equalsIgnoreCase(userName)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_ASSIGNED_TO_OTHER_USER);
            }
            return false;
        }

        // Check claim can be rejected
        if (!isClaimExpired(claim)
                || !(claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.REVIEWED)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.UNMODERATED))) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CANT_REJECT);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean canApproveClaimReview(String id) {
        return canApproveClaimReview(getRepository().getEntity(Claim.class, id), false);
    }

    private boolean canApproveClaimReview(Claim claim, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        // Check user role
        if (!isInRole(RolesConstants.CS_REVIEW_CLAIM)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }

        // Check user is assigned to the claim
        String userName = getUserName();
        if (!StringUtility.empty(claim.getAssigneeName()).equalsIgnoreCase(userName)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_ASSIGNED_TO_OTHER_USER);
            }
            return false;
        }

        // Check claim review can be approved
        if (!isClaimExpired(claim) || !claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.UNMODERATED)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CANT_APPROVE);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean canIssueClaim(String id) {
        return canIssueClaim(getRepository().getEntity(Claim.class, id), false);
    }

    private boolean canIssueClaim(Claim claim, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        // Check user role
        if (!isInRole(RolesConstants.CS_MODERATE_CLAIM, RolesConstants.CS_PRINT_CERTIFICATE)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }

        // Check claim to be moderated
        if (!claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.MODERATED)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CANT_ISSUE);
            }
            return false;
        }

        return true;
    }

    @Override
    public boolean canApproveClaimModeration(String id) {
        return canApproveClaimModeration(getRepository().getEntity(Claim.class, id), false);
    }

    private boolean canApproveClaimModeration(Claim claim, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        // Check user role
        if (!isInRole(RolesConstants.CS_MODERATE_CLAIM)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }

        // Check user is assigned to the claim
        String userName = getUserName();
        if (!StringUtility.empty(claim.getAssigneeName()).equalsIgnoreCase(userName)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_ASSIGNED_TO_OTHER_USER);
            }
            return false;
        }

        // Check claim review can be approved
        if (!claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.REVIEWED)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CANT_APPROVE);
            }
            return false;
        }

        // Check existing challenges
        List<Claim> challenges = getChallengingClaimsByChallengedId(claim.getId());

        if (challenges != null && challenges.size() > 0) {
            for (Claim challenge : challenges) {
                if (challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.UNMODERATED)
                        || challenge.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.REVIEWED)) {
                    if (throwException) {
                        throw new SOLAException(ServiceMessage.OT_WS_CLAIM_HAS_CHALLENGES);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean canTransferClaim(String claimId) {
        return canTransferClaim(getRepository().getEntity(Claim.class, claimId), false);
    }

    private boolean canTransferClaim(Claim claim, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        // Check user role
        if (!isInRole(RolesConstants.CS_MODERATE_CLAIM)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }

        // Check claim status
        if (!claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.MODERATED)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CANT_TRANSFER);
            }
            return false;
        }
        return true;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_MODERATE_CLAIM, RolesConstants.CS_REVIEW_CLAIM})
    public boolean assignClaim(String claimId) {
        if (StringUtility.isEmpty(claimId)) {
            return false;
        }

        Claim claim = getRepository().getEntity(Claim.class, claimId);
        if (!canAssignClaim(claim, true)) {
            return false;
        }

        // Assign claim 
        ClaimStatusChanger claimStatusChanger = getRepository().getEntity(ClaimStatusChanger.class, claimId);

        claimStatusChanger.setAssigneeName(getUserName());
        getRepository().saveEntity(claimStatusChanger);
        return true;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_MODERATE_CLAIM, RolesConstants.CS_REVIEW_CLAIM})
    public boolean unAssignClaim(String claimId) {
        if (StringUtility.isEmpty(claimId)) {
            return false;
        }

        Claim claim = getRepository().getEntity(Claim.class, claimId);
        if (!canUnAssignClaim(claim, true)) {
            return false;
        }

        // UnAssign claim 
        ClaimStatusChanger claimStatusChanger = getRepository().getEntity(ClaimStatusChanger.class, claimId);

        claimStatusChanger.setAssigneeName(null);
        getRepository().saveEntity(claimStatusChanger);
        return true;
    }

    @Override
    public boolean canAssignClaim(String claimId) {
        return canAssignClaim(getRepository().getEntity(Claim.class, claimId), false);
    }

    private boolean canAssignClaim(Claim claim, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        // Check user role
        if (!isInRole(RolesConstants.CS_MODERATE_CLAIM, RolesConstants.CS_REVIEW_CLAIM)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }

        // Check claim can be assigned
        if (!isClaimExpired(claim) || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.REJECTED)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.CREATED)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.WITHDRAWN)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.MODERATED)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.HISTORIC)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.ISSUED)
                || !StringUtility.isEmpty(claim.getAssigneeName())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CANT_ASSIGN);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean canAddDocumentsToClaim(String claimId) {
        return canAddDocumentsToClaim(getRepository().getEntity(Claim.class, claimId), false);
    }

    private boolean canAddDocumentsToClaim(Claim claim, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        // Forbid adding documents for historic claims
        if (claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.HISTORIC)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_IS_READ_ONLY);
            }
            return false;
        }

        // Check claim status and ownership
        if (canIssueClaim(claim, throwException)) {
            return true;
        }

        if (!claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.UNMODERATED)
                || !StringUtility.empty(claim.getRecorderName()).equalsIgnoreCase(getUserName())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean canUnAssignClaim(String claimId) {
        return canUnAssignClaim(getRepository().getEntity(Claim.class, claimId), false);
    }

    private boolean canUnAssignClaim(Claim claim, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        // Check user role
        if (!isInRole(RolesConstants.CS_MODERATE_CLAIM, RolesConstants.CS_REVIEW_CLAIM)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }

        // Check claim can be assigned
        if (!isClaimExpired(claim) || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.REJECTED)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.WITHDRAWN)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.HISTORIC)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.MODERATED)
                || claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.ISSUED)
                || StringUtility.isEmpty(claim.getAssigneeName())) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CANT_UNASSIGN);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean canRevertClaimReview(String claimId) {
        return canRevertClaimReview(getRepository().getEntity(Claim.class, claimId), false);
    }

    private boolean canRevertClaimReview(Claim claim, boolean throwException) {
        if (claim == null) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_NOT_FOUND);
            }
            return false;
        }

        // Check user role
        if (!isInRole(RolesConstants.CS_MODERATE_CLAIM)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS);
            }
            return false;
        }

        // Check user is assigned to the claim
        String userName = getUserName();
        if (!StringUtility.empty(claim.getAssigneeName()).equalsIgnoreCase(userName)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_ASSIGNED_TO_OTHER_USER);
            }
            return false;
        }

        // Check claim review can be approved
        if (!claim.getStatusCode().equalsIgnoreCase(ClaimStatusConstants.REVIEWED)) {
            if (throwException) {
                throw new SOLAException(ServiceMessage.OT_WS_CLAIM_CANT_REVERT);
            }
            return false;
        }
        return true;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM})
    public void addClaimAttachment(String claimId, String attachmentId) {
        Claim claim = getRepository().getEntity(Claim.class, claimId);
        canAddDocumentsToClaim(claim, true);

        Attachment attch = getRepository().getEntity(Attachment.class, attachmentId);
        if (attch == null) {
            throw new SOLAException(ServiceMessage.OT_WS_MISSING_SERVER_ATTACHMENTS);
        } else // Check user name on attachment
         if (!isInRole(RolesConstants.CS_REVIEW_CLAIM, RolesConstants.CS_MODERATE_CLAIM, RolesConstants.CS_PRINT_CERTIFICATE)
                    && !attch.getUserName().equalsIgnoreCase(getUserName())) {
                throw new SOLAException(ServiceMessage.EXCEPTION_OBJECT_ACCESS_RIGHTS);
            }

        // Incraese claim row version by save without changes. This is required 
        // to indicate that there are changes on the claim in general
        claim.setVersion(claim.getVersion() + 1);
        getRepository().saveEntity(claim);

        // Craete relation between claim and attachment
        ClaimUsesAttachment claimAttach = new ClaimUsesAttachment();
        claimAttach.setClaimId(claimId);
        claimAttach.setAttachmentId(attachmentId);
        getRepository().saveEntity(claimAttach);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_MODERATE_CLAIM, RolesConstants.CS_PRINT_CERTIFICATE})
    public Attachment saveClaimAttachment(Attachment attachment, String languageCode) {
        // TODO: Make additional checks to allow saving only for moderated claims
        return getRepository().saveEntity(attachment);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public ClaimPermissions getClaimPermissions(String claimId) {
        ClaimPermissions permissions = new ClaimPermissions();

        if (StringUtility.isEmpty(claimId)) {
            return permissions;
        }

        Claim claim = getRepository().getEntity(Claim.class, claimId);

        if (claim == null) {
            return permissions;
        }

        permissions.setCanApproveModeration(canApproveClaimModeration(claim, false));
        permissions.setCanApproveReview(canApproveClaimReview(claim, false));
        permissions.setCanAssign(canAssignClaim(claim, false));
        permissions.setCanEdit(canEditClaim(claim, false));
        permissions.setCanReject(canRejectClaim(claim, false));
        permissions.setCanUnAssign(canUnAssignClaim(claim, false));
        permissions.setCanWithdraw(canWithdrawClaim(claim, false));
        permissions.setCanDelete(canDeleteClaim(claim, false));
        permissions.setCanAddDocumentsToClaim(canAddDocumentsToClaim(claim, false));
        permissions.setCanSubmitClaim(canSubmitClaim(claim, false));
        permissions.setCanChallengeClaim(canChallengeClaim(claim, false));
        permissions.setCanRevert(canRevertClaimReview(claim, false));
        permissions.setCanPrintCertificate(canPrintClaimCertificate(claim, false));
        permissions.setCanIssue(canIssueClaim(claim, false));
        permissions.setCanTransfer(canTransferClaim(claim, false));
        return permissions;
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public Claim getClaimByNumber(String nr) {
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, Claim.WHERE_BY_CLAIM_NUMBER);
        params.put(Claim.PARAM_CLAIM_NUMBER, nr);
        return getRepository().getEntity(Claim.class, params);
    }

    @Override
    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<FormTemplate> getFormTemplates(String languageCode) {
        HashMap params = new HashMap();
        if (languageCode != null) {
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, languageCode);
        }
        return getRepository().getEntityList(FormTemplate.class, params);
    }

    @Override
    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public FormTemplate getFormTemplate(String templateName, String languageCode) {
        if (languageCode != null) {
            return getRepository().getEntity(FormTemplate.class, templateName, languageCode);
        } else {
            return getRepository().getEntity(FormTemplate.class, templateName);
        }
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public boolean checkFormTemplateHasPayload(String formName) {
        if (formName == null) {
            return false;
        }

        String sql = "select count(1) > 0 as result from opentenure.form_payload where form_template_name=#{formName}";
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, sql);
        params.put("formName", formName);

        ArrayList<HashMap> result = getRepository().executeSql(params);
        if (result == null || result.size() < 1) {
            return false;
        } else {
            return result.get(0).get("result") != null && Boolean.parseBoolean(result.get(0).get("result").toString());
        }
    }

    @Override
    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public FormTemplate getDefaultFormTemplate(String languageCode) {
        HashMap params = new HashMap();
        if (languageCode != null) {
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, languageCode);
        }
        return getRepository().getEntity(FormTemplate.class, "is_default = 't'", params);
    }

    @Override
    public List<FieldType> getFieldTypes(String languageCode) {
        return getRepository().getCodeList(FieldType.class, languageCode);
    }

    @Override
    public List<FieldValueType> getFieldValueTypes(String languageCode) {
        return getRepository().getCodeList(FieldValueType.class, languageCode);
    }

    @Override
    public List<FieldConstraintType> getFieldConstraintTypes(String languageCode) {
        return getRepository().getCodeList(FieldConstraintType.class, languageCode);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public AdministrativeBoundary getAdministrativeBoundary(String id) {
        if (id != null) {
            return getRepository().getEntity(AdministrativeBoundary.class, id);
        }
        return null;
    }

    @Override
    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<AdministrativeBoundary> getApprovedAdministrativeBoundaries(){
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundary.QUERY_SELECT_APPROVED);
        return getRepository().getEntityList(AdministrativeBoundary.class, params);
    }
    
    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM})
    public AdministrativeBoundary saveAdministrativeBoundary(AdministrativeBoundary boundary) {
        if (boundary == null) {
            throw new SOLAException(ServiceMessage.GENERAL_OBJECT_IS_NULL);
        }

        // Get db boundary
        AdministrativeBoundary dbBoundary = getAdministrativeBoundary(boundary.getId());

        // Check name
        if (StringUtility.isEmpty(boundary.getName())) {
            throw new SOLAException(ServiceMessage.OT_WS_BOUNDARY_NAME_EMPTY);
        }

        // Check type
        if (StringUtility.isEmpty(boundary.getTypeCode())) {
            throw new SOLAException(ServiceMessage.OT_WS_BOUNDARY_TYPE_EMPTY);
        }

        // Check status
        if (dbBoundary != null && dbBoundary.getStatusCode().equalsIgnoreCase(AdministrativeBoundaryStatus.STATUS_APPROVED)) {
            throw new SOLAException(ServiceMessage.OT_WS_BOUNDARY_APPROVED, new Object[]{StringUtility.empty(boundary.getName())});
        }
        
        // Set recorder
        if(dbBoundary == null){
            boundary.setRecorderName(getUserName());
        } else {
            boundary.setRecorderName(dbBoundary.getRecorderName());
        }

        // Set status
        boundary.setStatusCode(AdministrativeBoundaryStatus.STATUS_PENDING);

        // Geom
        if (StringUtility.isEmpty(boundary.getGeom())) {
            boundary.setGeom(null);
        }
        
        // Parent
        if (StringUtility.isEmpty(boundary.getParentId())) {
            boundary.setParentId(null);
        } else {
            // Check parent
            if (boundary.getId().equalsIgnoreCase(boundary.getParentId())) {
                throw new SOLAException(ServiceMessage.OT_WS_BOUNDARY_SELF_PARENT);
            }
            // Get child records
            List<AdministrativeBoundarySearchResult> childBoundaries = searchEjb.searchChildAdministrativeBoundaries(boundary.getId(), null);
            if (childBoundaries != null && childBoundaries.size() > 0) {
                for (AdministrativeBoundarySearchResult child : childBoundaries) {
                    if (child.getId().equalsIgnoreCase(boundary.getParentId())) {
                        throw new SOLAException(ServiceMessage.OT_WS_BOUNDARY_CHILD_PARENT);
                    }
                }
            }
        }

        // Save       
        return getRepository().saveEntity(boundary);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_RECORD_CLAIM})
    public void deleteAdministrativeBoundary(String boundaryId) {
        if (StringUtility.isEmpty(boundaryId)) {
            throw new SOLAException(ServiceMessage.GENERAL_OBJECT_IS_NULL);
        }

        // Get db boundary
        AdministrativeBoundary dbBoundary = getAdministrativeBoundary(boundaryId);
        if(dbBoundary == null){
            throw new SOLAException(ServiceMessage.GENERAL_OBJECT_IS_NULL);
        }
        
        // Check status
        if (dbBoundary.getStatusCode().equalsIgnoreCase(AdministrativeBoundaryStatus.STATUS_APPROVED)) {
            throw new SOLAException(ServiceMessage.OT_WS_BOUNDARY_APPROVED, new Object[]{StringUtility.empty(dbBoundary.getName())});
        }

        // Check children
        List<AdministrativeBoundarySearchResult> childBoundaries = searchEjb.searchChildAdministrativeBoundaries(boundaryId, null);
        if (childBoundaries != null && childBoundaries.size() > 0) {
            throw new SOLAException(ServiceMessage.OT_WS_BOUNDARY_HAS_CHILD);
        }

        // Delete
        dbBoundary.setEntityAction(EntityAction.DELETE);
        getRepository().saveEntity(dbBoundary);
    }
    
    @Override
    @RolesAllowed({RolesConstants.CS_MODERATE_CLAIM})
    public boolean approveAdministrativeBoundary(String boundaryId){
        if (StringUtility.isEmpty(boundaryId)) {
            throw new SOLAException(ServiceMessage.GENERAL_OBJECT_IS_NULL);
        }

        // Get db boundary
        AdministrativeBoundary dbBoundary = getAdministrativeBoundary(boundaryId);
        if(dbBoundary == null){
            throw new SOLAException(ServiceMessage.GENERAL_OBJECT_IS_NULL);
        }
        
        // Check status
        if (dbBoundary.getStatusCode().equalsIgnoreCase(AdministrativeBoundaryStatus.STATUS_APPROVED)) {
            throw new SOLAException(ServiceMessage.OT_WS_BOUNDARY_APPROVED, new Object[]{StringUtility.empty(dbBoundary.getName())});
        }

        dbBoundary.setStatusCode(AdministrativeBoundaryStatus.STATUS_APPROVED);
        
        // Save
        getRepository().saveEntity(dbBoundary);
        
        return true;
    }
}
