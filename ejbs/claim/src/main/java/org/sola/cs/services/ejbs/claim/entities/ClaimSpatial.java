package org.sola.cs.services.ejbs.claim.entities;

import javax.persistence.Column;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

/**
 * Spatial representation of the claim
 */
public class ClaimSpatial extends AbstractReadOnlyEntity {

    public static final String PARAM_CUSTOM_SRID = "customSrid";
    public static final String PARAM_CLAIM_ID = "claimId";
    public static final String QUERY_GET_BY_ID = "WITH target_geom AS ("
            + "  select st_astext(st_buffer(mapped_geometry, 0.0001)) as geom "
            + "  from opentenure.claim where id =#{ " + PARAM_CLAIM_ID + "} and mapped_geometry is not null "
            + ") "
            + "select id, nr, status_code, "
            + "st_astext(case when coalesce(#{ " + PARAM_CUSTOM_SRID + "},0) = 0 then mapped_geometry else st_transform(st_setsrid(mapped_geometry,4326),#{ " + PARAM_CUSTOM_SRID + "}) end) as geom, "
            + "(case when id =#{ " + PARAM_CLAIM_ID + "} then true else false end) as target "
            + "from opentenure.claim "
            + "where mapped_geometry is not null and ST_Intersects(mapped_geometry,(select geom from target_geom)::geometry)";

    @Column
    private String id;
    @Column
    private String nr;
    @Column(name = "status_code")
    private String statusCode;
    @Column
    private boolean target;
    @Column
    private String geom;

    public ClaimSpatial() {
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

    public boolean isTarget() {
        return target;
    }

    public void setTarget(boolean target) {
        this.target = target;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }
}
