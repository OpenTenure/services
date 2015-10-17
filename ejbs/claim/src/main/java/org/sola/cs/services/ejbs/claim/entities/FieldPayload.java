package org.sola.cs.services.ejbs.claim.entities;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.common.StringUtility;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "field_payload")
@DefaultSorter(sortString = "item_order")
public class FieldPayload extends AbstractVersionedEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column
    private String name;
    @Column(name="display_name")
    private String displayName;
    @Column(name="field_type")
    private String fieldType;
    @Column(name="section_element_payload_id")
    private String sectionElementPayloadId;
    @Column(name="string_payload")
    private String stringPayload;
    @Column(name="big_decimal_payload")
    private BigDecimal bigDecimalPayload;
    @Column(name="boolean_payload")
    private boolean booleanPayload;
    @Column(name="field_value_type")
    private String fieldValueType;
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

    public String getSectionElementPayloadId() {
        return sectionElementPayloadId;
    }

    public void setSectionElementPayloadId(String sectionElementPayloadId) {
        this.sectionElementPayloadId = sectionElementPayloadId;
    }

    public String getStringPayload() {
        return stringPayload;
    }

    public void setStringPayload(String stringPayload) {
        this.stringPayload = stringPayload;
    }

    public BigDecimal getBigDecimalPayload() {
        return bigDecimalPayload;
    }

    public void setBigDecimalPayload(BigDecimal bigDecimalPayload) {
        this.bigDecimalPayload = bigDecimalPayload;
    }

    public boolean isBooleanPayload() {
        return booleanPayload;
    }

    public void setBooleanPayload(boolean booleanPayload) {
        this.booleanPayload = booleanPayload;
    }

    public String getFieldValueType() {
        return fieldValueType;
    }

    public void setFieldValueType(String fieldValueType) {
        this.fieldValueType = fieldValueType;
    }

    public int getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(int itemOrder) {
        this.itemOrder = itemOrder;
    }
    
    public FieldPayload(){
    }
    
    public void validate(FieldTemplate fTempl) throws Exception {
        if (fTempl.getFieldConstraintList() == null || StringUtility.isEmpty(fTempl.getFieldType())) {
            return;
        }

        String fType = fTempl.getFieldType();
        String strValue = this.getStringPayload();
        boolean boolValue = this.isBooleanPayload();
        BigDecimal numValue = this.getBigDecimalPayload();

        for (FieldConstraint fConstr : fTempl.getFieldConstraintList()) {
            String constrType = fConstr.getFieldConstraintType();
            if (constrType == null) {
                continue;
            }

            boolean success = true;

            if (constrType.equalsIgnoreCase(FieldConstraintType.TYPE_NOT_NULL)) {
                if (((fType.equalsIgnoreCase(FieldType.TYPE_DECIMAL)
                        || fType.equalsIgnoreCase(FieldType.TYPE_INTEGER))
                        && numValue == null)
                        || ((fType.equalsIgnoreCase(FieldType.TYPE_DATE)
                        || fType.equalsIgnoreCase(FieldType.TYPE_TEXT))
                        && StringUtility.isEmpty(strValue))) {
                    success = false;
                }
            }

            if (constrType.equalsIgnoreCase(FieldConstraintType.TYPE_DATETIME)) {
                if (!StringUtility.isEmpty(strValue) && !StringUtility.isEmpty(fConstr.getFormat())) {
                    SimpleDateFormat formatter = new SimpleDateFormat(fConstr.getFormat());
                    try {
                        formatter.parse(strValue);
                    } catch (ParseException ex) {
                        success = false;
                    }
                }
            }

            if (constrType.equalsIgnoreCase(FieldConstraintType.TYPE_DOUBLE_RANGE)) {
                if (numValue != null) {
                    if ((fConstr.getMinValue() != null && numValue.compareTo(fConstr.getMinValue()) < 0)
                            || (fConstr.getMaxValue() != null && numValue.compareTo(fConstr.getMaxValue()) > 0)) {
                        success = false;
                    }
                }
            }

            if (constrType.equalsIgnoreCase(FieldConstraintType.TYPE_INTEGER)) {
                if (numValue != null) {
                    try {
                        numValue.intValueExact();
                    } catch (ArithmeticException ae) {
                        success = false;
                    }
                }
            }

            if (constrType.equalsIgnoreCase(FieldConstraintType.TYPE_INTEGER_RANGE)) {
                if (numValue != null) {
                    try {
                        int intValue = numValue.intValueExact();
                        if (fConstr.getMinValue() != null) {
                            int intMinValue = fConstr.getMinValue().intValueExact();
                            if (intValue < intMinValue) {
                                success = false;
                            }
                        }

                        if (fConstr.getMaxValue() != null) {
                            int intMaxValue = fConstr.getMaxValue().intValueExact();
                            if (intValue > intMaxValue) {
                                success = false;
                            }
                        }
                    } catch (ArithmeticException ae) {
                        success = false;
                    }
                }
            }

            if (constrType.equalsIgnoreCase(FieldConstraintType.TYPE_LENGTH)) {
                if (!StringUtility.isEmpty(strValue)) {
                    try {
                        if (fConstr.getMinValue() != null) {
                            int intMinValue = fConstr.getMinValue().intValueExact();
                            if (strValue.length() < intMinValue) {
                                success = false;
                            }
                        }

                        if (fConstr.getMaxValue() != null) {
                            int intMaxValue = fConstr.getMaxValue().intValueExact();
                            if (strValue.length() > intMaxValue) {
                                success = false;
                            }
                        }
                    } catch (ArithmeticException ae) {
                        success = false;
                    }
                }
            }

            if (constrType.equalsIgnoreCase(FieldConstraintType.TYPE_OPTION)) {
                if (!StringUtility.isEmpty(strValue) && fConstr.getFieldConstraintOptionList() != null) {
                    boolean foundOption = false;
                    for (FieldConstraintOption option : fConstr.getFieldConstraintOptionList()) {
                        if (strValue.equalsIgnoreCase(option.getName())) {
                            foundOption = true;
                            break;
                        }
                    }
                    if (!foundOption) {
                        success = false;
                    }
                }
            }

            if (constrType.equalsIgnoreCase(FieldConstraintType.TYPE_REGEXP)) {
                if (!StringUtility.isEmpty(strValue) && !StringUtility.isEmpty(fConstr.getFormat())) {
                    Pattern pattern = Pattern.compile(fConstr.getFormat());
                    if (!pattern.matcher(strValue).matches()) {
                        success = false;
                    }
                }
            }

            if (!success) {
                //addException(fConstr.getErrorMsg());
                throw new Exception(fConstr.getErrorMsg());
                //return false;
            }
        }
    }
}
