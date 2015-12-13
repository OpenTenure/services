package org.sola.cs.services.ejbs.claim.businesslogic;

import java.awt.image.BufferedImage;
import javax.ejb.Local;
import org.sola.services.common.ejbs.AbstractEJBLocal;

@Local
public interface MapImageEJBLocal extends AbstractEJBLocal {

    int getBestScaleForMapImage(String claimId, int width, int height);

    BufferedImage getMapImage(String claimId, int width, int height, double scale, boolean drawScale, String scaleLabel);

    BufferedImage getMapImage(String claimId, int width, int height, boolean drawScale, String scaleLabel);
}
