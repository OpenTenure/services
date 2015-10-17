package org.sola.cs.services.ejbs.claim.entities;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.entities.AbstractEntity;

@Table(schema = "opentenure", name = "attachment_chunk")
public class AttachmentChunk extends AbstractEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "attachment_id")
    private String attachmentId;
    @Column(name = "claim_id")
    private String claimId;
    @Column(name = "start_position")
    private long startPosition;
    @Column(name = "size")
    private long size;
    @Column(name = "body")
    private byte[] body;
    @Column(name = "md5")
    private String md5;
    @Column(name = "user_name", updatable = false)
    private String userName;
    @Column(name = "creation_time", updatable = false, insertable = false)
    private Date creationTime;
    
    public static final String PARAM_ATTACHMENT_ID = "attachmentId";
    public static final String PARAM_CLAIM_ID = "claimId";
    public static final String PARAM_USER_NAME = "userName";
    public static final String PARAM_START_POSITION = "start_position";
    public static final String DELETE_CHUNKS_BY_ATTACHMENT = "delete from opentenure.attachment_chunk WHERE attachment_id = #{" + PARAM_ATTACHMENT_ID + "}";
    public static final String DELETE_CHUNKS_BY_CLAIM = "delete from opentenure.attachment_chunk WHERE claim_id = #{" + PARAM_CLAIM_ID + "}";
    public static final String WHERE_BY_ATTACHMENT_ID = "attachment_id = #{" + PARAM_ATTACHMENT_ID + "}";
    public static final String WHERE_BY_CLAIM_ID = "claim_id = #{" + PARAM_CLAIM_ID + "}";
    public static final String QUERY_TOTAL_SIZE_BY_USER_PER_DAY = 
            "select sum(size::int) as total_size from opentenure.attachment_chunk where "
            + "user_name = #{" + PARAM_USER_NAME + "} and EXTRACT(epoch from now() - creation_time)/60 < 1440";
    public AttachmentChunk(){
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
