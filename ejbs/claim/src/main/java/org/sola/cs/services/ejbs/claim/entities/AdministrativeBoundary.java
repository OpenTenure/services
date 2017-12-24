package org.sola.cs.services.ejbs.claim.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.AccessFunctions;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "administrative_boundary")
public class AdministrativeBoundary extends AbstractVersionedEntity {

    @Id
    @Column
    private String id;
    @Column
    private String name;
    @Column(name = "type_code")
    private String typeCode;
    @Column(name = "authority_name")
    private String authorityName;
    @Column(name = "parent_id")
    private String parentId;
    @Column(name = "status_code")
    private String statusCode;
    @Column(name = "geom")
    @AccessFunctions(onSelect = "ST_AsText(geom)",
            onChange = "ST_GeomFromText(#{geom})")
    private String geom;

    public AdministrativeBoundary() {
        super();
    }

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

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }
}
