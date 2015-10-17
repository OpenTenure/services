package org.sola.cs.services.ejbs.claim.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.sola.services.common.repository.AccessFunctions;
import org.sola.services.common.repository.entities.AbstractVersionedEntity;

@Table(schema = "opentenure", name = "claim_location")
public class ClaimLocation extends AbstractVersionedEntity {
    @Id
    @Column
    private String id;

    @Column(name="claim_id")
    private String claimId;
    
    @Column(name = "mapped_location")
    @AccessFunctions(onSelect = "ST_AsText(mapped_location)",
    onChange = "ST_GeomFromText(#{mappedLocation})")
    private String mappedLocation;
    
    @Column(name = "gps_location")
    @AccessFunctions(onSelect = "ST_AsText(gps_location)",
    onChange = "ST_GeomFromText(#{gpsLocation})")
    private String gpsLocation;
    
    @Column
    private String description;
    
    public ClaimLocation(){
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public String getMappedLocation() {
        return mappedLocation;
    }

    public void setMappedLocation(String mappedLocation) {
        this.mappedLocation = mappedLocation;
    }

    public String getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
