package org.sola.cs.services.ejb.search.repository.entities;

import javax.persistence.Column;

public class AdministrativeBoundaryWithGeomSearchResult extends AdministrativeBoundarySearchResult {

    @Column
    private String geom;
    @Column(name = "parent_name")
    private String parentName;
    
    public static final String PARAM_POINT = "point";

    public static final String QUERY_SEARCH_BY_POINT
            = "SELECT '1' as path, b.id, st_astext(b.geom) as geom, b.name, bp.name as parent_name, b.type_code, get_translation(bt.display_value, null) as type_name, b.authority_name, b.parent_id, b.status_code, get_translation(bs.display_value, null) as status_name, bt.level \n"
            + "FROM ((opentenure.administrative_boundary b INNER JOIN opentenure.administrative_boundary_type bt ON b.type_code = bt.code) \n"
            + "  INNER JOIN opentenure.administrative_boundary_status bs ON b.status_code = bs.code) LEFT JOIN opentenure.administrative_boundary bp on b.parent_id = bp.id "
            + "WHERE ST_Contains(b.geom, ST_GeomFromText(#{" + PARAM_POINT + "}, St_SRID(b.geom))) "
            + "ORDER BY bt.level DESC";

    public AdministrativeBoundaryWithGeomSearchResult() {
        super();
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
}
