package org.sola.cs.services.ejb.search.repository.entities;

import javax.persistence.Column;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

public class GeoJsonClaim extends AbstractReadOnlyEntity {

    @Column
    private String id;
    @Column(name = "nr")
    private String nr;
    @Column(name = "status_code")
    private String statusCode;
    @Column(name = "geom")
    private String geom;

    public static final String PARAM_BOUNDARY_ID = "boundaryId";

    private static final String SELECT_PART
            = "select c.id, c.nr, ST_AsGeoJSON(st_transform(st_setsrid(c.mapped_geometry, 4326), 3857), 5, 0) as geom, c.status_code \n"
            + "from opentenure.claim c ";

    public static final String QUERY_SEARCH_ALL = SELECT_PART
            + "WHERE c.status_code NOT IN ('rejected','withdrawn','created') ORDER BY c.nr;";

    public static final String QUERY_SEARCH_BY_BOUNDARY = SELECT_PART
            + "WHERE c.status_code NOT IN ('rejected','withdrawn','created') AND c.boundary_id IN ("
            + "WITH RECURSIVE all_administrative_boundaries AS ( \n"
            + "        SELECT id, parent_id\n"
            + "        FROM opentenure.administrative_boundary \n"
            + "        WHERE id = #{" + PARAM_BOUNDARY_ID + "} \n"
            + "      UNION \n"
            + "	SELECT b.id, b.parent_id \n"
            + "        FROM opentenure.administrative_boundary b inner join all_administrative_boundaries ab on b.parent_id = ab.id \n"
            + ") \n"
            + "SELECT id FROM all_administrative_boundaries"
            + ") \n"
            + "ORDER BY c.nr;";

    public GeoJsonClaim() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNr() {
        return nr;
    }

    public void setNr(String nr) {
        this.nr = nr;
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
