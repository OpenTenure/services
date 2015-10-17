package org.sola.cs.services.ejbs.claim.entities;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "section_element_payload")
public class SectionElementPayload extends AbstractVersionedEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "section_payload_id")
    private String sectionPayloadId;
    @ChildEntityList(parentIdField = "sectionElementPayloadId", cascadeDelete = true)
    private List<FieldPayload> fieldPayloadList;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<FieldPayload> getFieldPayloadList() {
        return fieldPayloadList;
    }

    public void setFieldPayloadList(List<FieldPayload> fieldPayloadList) {
        this.fieldPayloadList = fieldPayloadList;
    }

    public String getSectionPayloadId() {
        return sectionPayloadId;
    }

    public void setSectionPayloadId(String sectionPayloadId) {
        this.sectionPayloadId = sectionPayloadId;
    }
    
    public SectionElementPayload(){
    }
}
