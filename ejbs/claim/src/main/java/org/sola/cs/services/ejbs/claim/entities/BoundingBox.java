package org.sola.cs.services.ejbs.claim.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.sola.services.common.repository.entities.AbstractEntity;


/**
 * Represents bounding box
 */
@Entity
public class BoundingBox extends AbstractEntity {

    public static final String PARAM_POINTS = "points";
    public static final String PARAM_SRID = "srid";

    public static final String QUERY = "select '1' as id, ST_X(ST_GeometryN(geom, 1)) as minx, ST_Y(ST_GeometryN(geom, 1)) as miny, ST_X(ST_GeometryN(geom, 2)) as maxx, ST_Y(ST_GeometryN(geom, 2)) as maxy \n"
                    + "from \n"
                    + "(select st_transform(st_setsrid(st_geomfromtext(#{" + PARAM_POINTS + "}),4326), #{" + PARAM_SRID + "}) as geom) sub";

    @Id
    private String id;
    @Column
    private double minx;
    @Column
    private double miny;
    @Column
    private double maxx;
    @Column
    private double maxy;
    
    public BoundingBox() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getMinx() {
        return minx;
    }

    public void setMinx(double minx) {
        this.minx = minx;
    }

    public double getMiny() {
        return miny;
    }

    public void setMiny(double miny) {
        this.miny = miny;
    }

    public double getMaxx() {
        return maxx;
    }

    public void setMaxx(double maxx) {
        this.maxx = maxx;
    }

    public double getMaxy() {
        return maxy;
    }

    public void setMaxy(double maxy) {
        this.maxy = maxy;
    }
}
