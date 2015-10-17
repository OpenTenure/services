package org.sola.cs.services.ejbs.claim.entities;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "form_payload")
public class FormPayload extends AbstractVersionedEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name="form_template_name")
    private String formTemplateName;
    @Column(name="claim_id")
    private String claimId;
    @ChildEntityList(parentIdField = "formPayloadId", cascadeDelete = true)
    private List<SectionPayload> sectionPayloadList;
    
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

    public String getFormTemplateName() {
        return formTemplateName;
    }

    public void setFormTemplateName(String formTemplateName) {
        this.formTemplateName = formTemplateName;
    }

    public List<SectionPayload> getSectionPayloadList() {
        return sectionPayloadList;
    }

    public void setSectionPayloadList(List<SectionPayload> sectionPayloadList) {
        this.sectionPayloadList = sectionPayloadList;
    }
    
    public FormPayload(){
    }
}
