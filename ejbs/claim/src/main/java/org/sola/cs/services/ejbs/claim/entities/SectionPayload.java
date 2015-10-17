package org.sola.cs.services.ejbs.claim.entities;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "section_payload")
@DefaultSorter(sortString = "item_order")
public class SectionPayload extends AbstractVersionedEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column
    private String name;
    @Column(name="form_payload_id")
    private String formPayloadId;
    @Column(name="display_name")
    private String displayName;
    @Column(name="element_name")
    private String elementName;
    @Column(name="element_display_name")
    private String elementDisplayName;
    @Column(name="max_occurrences")
    private int maxOccurrences;
    @Column(name="min_occurrences")
    private int minOccurrences;
    @ChildEntityList(parentIdField = "sectionPayloadId", cascadeDelete = true)
    private List<SectionElementPayload> sectionElementPayloadList;
    @Column(name="item_order")
    private int itemOrder;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormPayloadId() {
        return formPayloadId;
    }

    public void setFormPayloadId(String formPayloadId) {
        this.formPayloadId = formPayloadId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getElementDisplayName() {
        return elementDisplayName;
    }

    public void setElementDisplayName(String elementDisplayName) {
        this.elementDisplayName = elementDisplayName;
    }

    public int getMaxOccurrences() {
        return maxOccurrences;
    }

    public void setMaxOccurrences(int maxOccurrences) {
        this.maxOccurrences = maxOccurrences;
    }

    public int getMinOccurrences() {
        return minOccurrences;
    }

    public void setMinOccurrences(int minOccurrences) {
        this.minOccurrences = minOccurrences;
    }
    
    public List<SectionElementPayload> getSectionElementPayloadList() {
        return sectionElementPayloadList;
    }

    public void setSectionElementPayloadList(List<SectionElementPayload> sectionElementPayloadList) {
        this.sectionElementPayloadList = sectionElementPayloadList;
    }

    public int getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(int itemOrder) {
        this.itemOrder = itemOrder;
    }
    
    public SectionPayload(){
    }
}
