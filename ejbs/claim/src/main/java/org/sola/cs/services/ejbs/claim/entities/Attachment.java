package org.sola.cs.services.ejbs.claim.entities;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import static org.sola.cs.services.ejbs.claim.entities.AttachmentChunk.PARAM_USER_NAME;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "attachment")
public class Attachment extends AbstractVersionedEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "type_code")
    private String typeCode;
    @Column(name = "reference_nr")
    private String referenceNr;
    @Column(name = "size")
    private long size;
    @Column(name = "mime_type")
    private String mimeType;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "file_extension")
    private String fileExtension;
    @Column(name = "user_name", updatable = false)
    private String userName;
    @Column(name = "document_date")
    private Date documentDate;
    @Column(name = "description")
    private String description;
    private String md5;
    
    public static final String QUERY_TOTAL_SIZE_BY_USER_PER_DAY = 
            "select sum(size::int) as total_size from opentenure.attachment where "
            + "user_name = #{" + PARAM_USER_NAME + "} and EXTRACT(epoch from now() - change_time)/60 < 1440";
    
    public Attachment(){
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getReferenceNr() {
        return referenceNr;
    }

    public void setReferenceNr(String referenceNr) {
        this.referenceNr = referenceNr;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(Date documentDate) {
        this.documentDate = documentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
