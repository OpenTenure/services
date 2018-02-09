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
    @Column(name = "recorder_name")
    private String recorderName;
    @Column(name = "status_code")
    private String statusCode;
    @Column(name = "geom")
    @AccessFunctions(onSelect = "ST_AsText(geom)",
            onChange = "ST_GeomFromText(#{geom})")
    private String geom;

    public static final String QUERY_SELECT_APPROVED = "WITH RECURSIVE all_administrative_boundaries AS (\n"
            + " SELECT id, name, type_code, authority_name, parent_id, recorder_name, status_code, ST_AsText(geom) as geom, rowversion, change_user, rowidentifier, 1 as level, array[ROW_NUMBER() OVER (ORDER BY name)] AS path \n"
            + " FROM opentenure.administrative_boundary \n"
            + " WHERE parent_id is null and status_code = 'approved' \n"
            + "UNION \n"
            + " SELECT b.id, b.name, b.type_code, b.authority_name, b.parent_id, b.recorder_name, b.status_code, ST_AsText(b.geom) as geom, b.rowversion, b.change_user, b.rowidentifier, ab.level + 1 as level, ab.path || (ROW_NUMBER() OVER (ORDER BY b.name)) AS path \n"
            + " FROM opentenure.administrative_boundary b inner join all_administrative_boundaries ab on b.parent_id = ab.id \n"
            + " WHERE b.status_code = 'approved' \n"
            + ")\n"
            + "SELECT id, name, type_code, authority_name, parent_id, recorder_name, status_code, geom, rowversion, change_user, rowidentifier \n"
            + "FROM all_administrative_boundaries \n"
            + "ORDER BY path, level;";

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

    public String getRecorderName() {
        return recorderName;
    }

    public void setRecorderName(String recorderName) {
        this.recorderName = recorderName;
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
