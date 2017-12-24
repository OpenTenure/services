package org.sola.cs.services.ejb.search.repository.entities;

import javax.persistence.Column;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

public class GeoJsonAdministrativeBoundary extends AbstractReadOnlyEntity {

    @Column
    private String id;
    @Column
    private String name;
    @Column(name = "type_code")
    private String typeCode;
    @Column(name = "status_code")
    private String statusCode;
    @Column
    private String geom;

    public static final String PARAM_ID = "id";

    private static final String SELECT_PART
            = "select b.id, b.type_code, b.name, ST_AsGeoJSON(st_transform(st_setsrid(b.geom, 4326), 3857), 5, 0) as geom, b.status_code \n"
            + "from opentenure.administrative_boundary b ";

    public static final String QUERY_SEARCH_BY_ID = SELECT_PART
            + "WHERE b.id = #{" + PARAM_ID + "};";

    public GeoJsonAdministrativeBoundary() {
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
