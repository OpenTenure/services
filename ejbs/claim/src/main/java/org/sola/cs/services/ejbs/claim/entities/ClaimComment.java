package org.sola.cs.services.ejbs.claim.entities;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "claim_comment")
@DefaultSorter(sortString = "creation_time")
public class ClaimComment extends AbstractVersionedEntity {
    @Id
    @Column
    private String id;

    @Column(name="claim_id")
    private String claimId;
    
    @Column
    private String comment;
    
    @Column(name="comment_user", updatable = false)
    private String commentUser;
    
    @Column(name="creation_time", updatable = false, insertable = false)
    private Date creationTime;
    
    public ClaimComment(){
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentUser() {
        return commentUser;
    }

    public void setCommentUser(String commentUser) {
        this.commentUser = commentUser;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
