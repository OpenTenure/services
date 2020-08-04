package org.sola.cs.services.ejbs.claim.entities;

/**
 * Helper class to summarize all permissions on the claim
 */
public class ClaimPermissions {
    boolean canWithdraw = false;
    boolean canReject = false;
    boolean canApproveReview = false;
    boolean canApproveModeration = false;
    boolean canAssign = false;
    boolean canUnAssign = false;
    boolean canEdit = false;
    boolean canDelete = false;
    boolean canAddDocumentsToClaim = false;
    boolean canSubmitClaim = false;
    boolean canChallengeClaim = false;
    boolean canRevert = false;
    boolean canPrintCertificate = true;
    boolean canIssue = false;
    boolean canTransfer = false;

    public boolean isCanIssue() {
        return canIssue;
    }
          
    public void setCanIssue(boolean canIssue) {
        this.canIssue = canIssue;
    }

    public boolean isCanPrintCertificate() {
        return canPrintCertificate;
    }
    
    public void setCanPrintCertificate(boolean canPrintCertificate) {
        this.canPrintCertificate = canPrintCertificate;
    }

    public boolean isCanSubmitClaim() {
        return canSubmitClaim;
    }

    public void setCanSubmitClaim(boolean canSubmitClaim) {
        this.canSubmitClaim = canSubmitClaim;
    }

    public boolean isCanWithdraw() {
        return canWithdraw;
    }

    public void setCanWithdraw(boolean canWithdraw) {
        this.canWithdraw = canWithdraw;
    }

    public boolean isCanReject() {
        return canReject;
    }

    public void setCanReject(boolean canReject) {
        this.canReject = canReject;
    }

    public boolean isCanApproveReview() {
        return canApproveReview;
    }

    public void setCanApproveReview(boolean canApproveReview) {
        this.canApproveReview = canApproveReview;
    }

    public boolean isCanApproveModeration() {
        return canApproveModeration;
    }

    public void setCanApproveModeration(boolean canApproveModeration) {
        this.canApproveModeration = canApproveModeration;
    }

    public boolean isCanAssign() {
        return canAssign;
    }

    public void setCanAssign(boolean canAssign) {
        this.canAssign = canAssign;
    }

    public boolean isCanUnAssign() {
        return canUnAssign;
    }

    public void setCanUnAssign(boolean canUnAssign) {
        this.canUnAssign = canUnAssign;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean isCanAddDocumentsToClaim() {
        return canAddDocumentsToClaim;
    }

    public void setCanAddDocumentsToClaim(boolean canAddDocumentsToClaim) {
        this.canAddDocumentsToClaim = canAddDocumentsToClaim;
    }

    public boolean isCanChallengeClaim() {
        return canChallengeClaim;
    }

    public void setCanChallengeClaim(boolean canChallengeClaim) {
        this.canChallengeClaim = canChallengeClaim;
    }

    public boolean isCanRevert() {
        return canRevert;
    }

    public void setCanRevert(boolean canRevert) {
        this.canRevert = canRevert;
    }
        
    public boolean isCanTransfer() {
        return canTransfer;
    }
    
    public void setCanTransfer(boolean canTransfer) {
        this.canTransfer = canTransfer;
    }

}
