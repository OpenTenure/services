package org.sola.cs.services.ejb.search.repository.entities;

import javax.persistence.Column;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

public class PublicDisplaySearchResult extends AbstractReadOnlyEntity {

    @Column
    private String id;
    @Column(name = "nr")
    private String nr;
    @Column(name = "owner_name")
    private String ownerName;
    @Column(name = "status_code")
    private String statusCode;
    @Column
    private Double percentage;
    @Column(name = "claim_area")
    private Long claimArea;
    @Column(name = "land_use")
    private String landUse;

    public static final String PARAM_BOUNDARY_ID = "boundaryId";

    private static final String SELECT_PART
            = "select c.id, c.nr, c.claim_area, p.name || ' ' || coalesce(p.last_name, '') as owner_name, sh.percentage, get_translation(l.display_value, #{" + CommonSqlProvider.PARAM_LANGUAGE_CODE + "}) as land_use, c.status_code \n"
            + "from (opentenure.claim c left join \n"
            + "  (opentenure.claim_share sh inner join (opentenure.party_for_claim_share psh inner join opentenure.party p on psh.party_id = p.id) on sh.id = psh.claim_share_id) \n"
            + "  on c.id = sh.claim_id) left join cadastre.land_use_type l on c.land_use_code = l.code \n";

    public static final String QUERY_SEARCH_BY_BOUNDARY = SELECT_PART
            + "where c.status_code NOT IN ('rejected','withdrawn','created') and ('' = #{" + PARAM_BOUNDARY_ID + "} or c.boundary_id in "
            + "(WITH RECURSIVE all_administrative_boundaries AS ( \n"
            + "        SELECT id, parent_id \n"
            + "        FROM opentenure.administrative_boundary \n"
            + "        WHERE id = #{" + PARAM_BOUNDARY_ID + "} \n"
            + "      UNION \n"
            + "	SELECT b.id, b.parent_id \n"
            + "        FROM opentenure.administrative_boundary b inner join all_administrative_boundaries ab on b.parent_id = ab.id \n"
            + ") \n"
            + "SELECT id \n"
            + "FROM all_administrative_boundaries \n"
            + ")) \n"
            + "order by c.nr";

    public PublicDisplaySearchResult() {
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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Long getClaimArea() {
        return claimArea;
    }

    public void setClaimArea(Long claimArea) {
        this.claimArea = claimArea;
    }

    public String getLandUse() {
        return landUse;
    }

    public void setLandUse(String landUse) {
        this.landUse = landUse;
    }
}
