package org.sola.opentenure.services.ejbs.claim.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.Localized;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "field_constraint_option")
public class FieldConstraintOption extends AbstractVersionedEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column
    private String name;
    @Column(name="display_name")
    @Localized
    private String displayName;
    @Column(name="field_constraint_id")
    private String fieldConstraintId;

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

    public String getFieldConstraintId() {
        return fieldConstraintId;
    }

    public void setFieldConstraintId(String fieldConstraintId) {
        this.fieldConstraintId = fieldConstraintId;
    }
    
    public FieldConstraintOption(){
    }
}
