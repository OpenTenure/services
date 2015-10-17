package org.sola.cs.services.ejbs.claim.entities;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.Localized;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "field_template")
@DefaultSorter(sortString = "item_order")
public class FieldTemplate extends AbstractVersionedEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column
    private String name;
    @Column(name = "display_name")
    @Localized
    private String displayName;
    @Column(name = "field_type")
    private String fieldType;
    @Column(name = "section_template_id")
    private String sectionTemplateId;
    @Column
    @Localized
    private String hint;
    @ChildEntityList(parentIdField = "fieldTemplateId", cascadeDelete = true)
    private List<FieldConstraint> fieldConstraintList;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getSectionTemplateId() {
        return sectionTemplateId;
    }

    public void setSectionTemplateId(String sectionTemplateId) {
        this.sectionTemplateId = sectionTemplateId;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public List<FieldConstraint> getFieldConstraintList() {
        return fieldConstraintList;
    }

    public void setFieldConstraintList(List<FieldConstraint> fieldConstraintList) {
        this.fieldConstraintList = fieldConstraintList;
    }

    public int getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(int itemOrder) {
        this.itemOrder = itemOrder;
    }

    /** Returns true if field is mandatory, otherwise false. This method interrogates list of constraints on the field. */
    public boolean getIsRequired(){
        if(fieldConstraintList == null || fieldConstraintList.size() < 1){
            return false;
        }
        for(FieldConstraint fc : fieldConstraintList){
            if(fc.getFieldConstraintType() != null && fc.getFieldConstraintType().equalsIgnoreCase(FieldConstraintType.TYPE_NOT_NULL)){
                return true;
            }
        }
        return false;
    }
    
    /** Short way to return all possible options, available for the field. */
    public FieldConstraintOption[] getFieldOptions(){
        List<FieldConstraintOption> options = new ArrayList<FieldConstraintOption>();
        
        if(fieldConstraintList == null || fieldConstraintList.size() < 1){
            return null;
        }
        for(FieldConstraint fc : fieldConstraintList){
            if(fc.getFieldConstraintOptionList() != null && fc.getFieldConstraintOptionList().size() > 0){
                options.addAll(fc.getFieldConstraintOptionList());
            }
        }
        if(options.size() < 1){
            return null;
        } else {
            FieldConstraintOption dummy = new FieldConstraintOption();
            dummy.setName("");
            dummy.setDisplayName("");
            options.add(0, dummy);
            return options.toArray(new FieldConstraintOption[options.size()]);
        }
    }
    
    public FieldTemplate(){
    }
}
