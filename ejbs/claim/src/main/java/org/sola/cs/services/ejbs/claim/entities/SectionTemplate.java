package org.sola.cs.services.ejbs.claim.entities;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.Localized;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "section_template")
@DefaultSorter(sortString = "item_order")
public class SectionTemplate extends AbstractVersionedEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column
    private String name;
    @Column(name="form_template_name")
    private String formTemplateName;
    @Column(name="display_name")
    @Localized
    private String displayName;
    @Column(name="max_occurrences")
    private int maxOccurrences;
    @Column(name="min_occurrences")
    private int minOccurrences;
    @Column(name="error_msg")
    @Localized
    private String errorMsg;
    @Column(name="element_name")
    private String elementName;
    @Column(name="element_display_name")
    @Localized
    private String elementDisplayName;
    @ChildEntityList(parentIdField = "sectionTemplateId", cascadeDelete = true)
    private List<FieldTemplate> fieldTemplateList;
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

    public String getFormTemplateName() {
        return formTemplateName;
    }

    public void setFormTemplateName(String formTemplateName) {
        this.formTemplateName = formTemplateName;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public FieldTemplate[] getFieldTemplateArray() {
        if(fieldTemplateList == null){
            return new FieldTemplate[]{};
        }
        return fieldTemplateList.toArray(new FieldTemplate[fieldTemplateList.size()]);
    }
    
    public List<FieldTemplate> getFieldTemplateList() {
        return fieldTemplateList;
    }

    public void setFieldTemplateList(List<FieldTemplate> fieldTemplateList) {
        this.fieldTemplateList = fieldTemplateList;
    }

    public int getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(int itemOrder) {
        this.itemOrder = itemOrder;
    }

    public SectionTemplate(){
    }
}
