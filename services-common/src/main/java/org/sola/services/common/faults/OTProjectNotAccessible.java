package org.sola.services.common.faults;

import org.sola.common.SOLAException;

/**
 * OpenTenure exception of inaccessible project
 */
public class OTProjectNotAccessible extends SOLAException {
    public OTProjectNotAccessible(String messageCode) {
        super(messageCode);
    }
    
    public OTProjectNotAccessible(String messageCode, Throwable cause) {
        super(messageCode, cause);
    }
}