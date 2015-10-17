package org.sola.cs.services.ejbs.claim.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "claim_uses_attachment")
public class ClaimUsesAttachment  extends AbstractVersionedEntity {
    @Id
    @Column(name = "claim_id")
    private String claimId;
    @Id
    @Column(name = "attachment_id")
    private String attachmentId;
    
    public ClaimUsesAttachment(){
        super();
    }

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }
}
