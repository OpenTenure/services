package org.sola.cs.services.ejb.search.repository.entities;

import javax.persistence.Column;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

public class MapSearchResult extends AbstractReadOnlyEntity {

    @Column
    private String id;
    @Column
    private String nr;
    @Column(name = "owner_names")
    private String ownerNames;
    @Column
    private String geom;

    public static final String PARAM_NAME = "claimantName";
    public static final String PARAM_CLAIM_NUMBER = "claimNumber";
    public static final String PARAM_POINT = "pointParam";
    private static final String SELECT_PART
            = "select c.id, c.nr, string_agg(trim(both ' ' from p.name || ' ' || coalesce(p.last_name, '')), ', ') as owner_names, st_astext(mapped_geometry) as geom \n"
            + "from opentenure.claim c \n"
            + "  left join (opentenure.claim_share sh \n"
            + "  inner join (opentenure.party_for_claim_share psh \n"
            + "  inner join opentenure.party p on psh.party_id = p.id) \n"
            + "  on sh.id = psh.claim_share_id) on c.id = sh.claim_id \n";

    public static final String QUERY_SEARCH = SELECT_PART
            + "where (position(lower(#{" + PARAM_NAME + "}) in lower(COALESCE(p.name, '') || ' ' || COALESCE(p.last_name, ''))) > 0 \n"
            + "or position(lower(#{" + PARAM_CLAIM_NUMBER + "}) in lower(COALESCE(c.nr, ''))) > 0) \n"
            + "and (#{" + PARAM_POINT + "} = '' or ST_Contains(c.mapped_geometry, ST_GeomFromText(#{" + PARAM_POINT + "}, St_SRID(c.mapped_geometry)))) \n"
            + "and c.status_code NOT IN ('rejected','withdrawn','created') \n"
            + "group by c.id, c.nr \n"
            + "limit 50;";

    public MapSearchResult() {
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

    public String getOwnerNames() {
        return ownerNames;
    }

    public void setOwnerNames(String ownerNames) {
        this.ownerNames = ownerNames;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }
}
