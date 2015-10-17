package org.sola.cs.services.ejbs.claim.entities;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.Localized;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "field_constraint")
public class FieldConstraint extends AbstractVersionedEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column
    private String name;
    @Column(name="display_name")
    @Localized
    private String displayName;
    @Column(name="error_msg")
    @Localized
    private String errorMsg;
    @Column
    private String format;
    @Column(name="min_value")
    private BigDecimal minValue;
    @Column(name="max_value")
    private BigDecimal maxValue;
    @Column(name="field_template_id")
    private String fieldTemplateId;
    @Column(name="field_constraint_type")
    private String fieldConstraintType;
    @ChildEntityList(parentIdField = "fieldConstraintId", cascadeDelete = true)
    private List<FieldConstraintOption> fieldConstraintOptionList;
    
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

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public String getFieldTemplateId() {
        return fieldTemplateId;
    }

    public void setFieldTemplateId(String fieldTemplateId) {
        this.fieldTemplateId = fieldTemplateId;
    }

    public String getFieldConstraintType() {
        return fieldConstraintType;
    }

    public void setFieldConstraintType(String fieldConstraintType) {
        this.fieldConstraintType = fieldConstraintType;
    }

    public List<FieldConstraintOption> getFieldConstraintOptionList() {
        return fieldConstraintOptionList;
    }

    public void setFieldConstraintOptionList(List<FieldConstraintOption> fieldConstraintOptionList) {
        this.fieldConstraintOptionList = fieldConstraintOptionList;
    }
    
    public FieldConstraint(){
    }
}
