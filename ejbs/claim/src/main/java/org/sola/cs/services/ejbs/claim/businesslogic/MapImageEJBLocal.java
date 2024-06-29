package org.sola.cs.services.ejbs.claim.businesslogic;

import jakarta.ejb.Local;
import org.sola.cs.services.ejbs.claim.entities.MapImageParams;
import org.sola.cs.services.ejbs.claim.entities.MapImageResponse;
import org.sola.services.common.ejbs.AbstractEJBLocal;

@Local
public interface MapImageEJBLocal extends AbstractEJBLocal {

    int getBestScaleForMapImage(String claimId, String projectId, MapImageParams params);

    MapImageResponse getMapImage(String claimId, String projectId, MapImageParams params);
}
