/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.cs.services.ejb.search.businesslogic;

import org.sola.cs.services.ejb.search.repository.entities.ClaimSpatialSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.ConfigMapLayer;
import org.sola.cs.services.ejb.search.repository.entities.BrSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSpatialSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.BrSearchResult;
import java.util.List;
import jakarta.ejb.Local;
import java.util.HashMap;
import java.util.Map;
import org.sola.cs.services.ejb.search.repository.entities.AdministrativeBoundarySearchResult;
import org.sola.cs.services.ejb.search.repository.entities.AdministrativeBoundaryWithGeomSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.GeoJsonAdministrativeBoundary;
import org.sola.cs.services.ejb.search.repository.entities.GeoJsonClaim;
import org.sola.cs.services.ejb.search.repository.entities.MapSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.ProjectConfigForClient;
import org.sola.cs.services.ejb.search.repository.entities.ProjectNameSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.PublicDisplaySearchResult;
import org.sola.services.common.ejbs.AbstractEJBLocal;

/**
 * Local interface for the {@linkplain SearchCSEJB}
 */
@Local
public interface SearchCSEJBLocal extends AbstractEJBLocal {
    
    HashMap getResultObjectFromStatement(String sqlStatement, Map params);
            
    /**
     * See {@linkplain SearchCSEJB#getMapLayersByProject(java.lang.String, java.lang.String)
     * SearchCSEJB.getMapLayersByProject}.
     */
    List<ConfigMapLayer> getMapLayersByProject(String projectId, String languageCode);
  
    /**
     * See {@linkplain SearchEJB#searchBr(org.sola.services.ejb.search.repository.entities.BrSearchParams, java.lang.String)
     * SearchEJB.searchBr}.
     */
    List<BrSearchResult> searchBr(BrSearchParams searchParams, String lang);

    /**
     * See {@linkplain SearchEJB#getClaimsByBox(org.sola.services.ejb.search.repository.entities.ClaimSpatialSearchParams)}.
     */
    List<ClaimSpatialSearchResult> getClaimsByBox(ClaimSpatialSearchParams searchParams);
    
    /**
     * See {@linkplain SearchEJB#getClaimsByCoordinates(String, String)}.
     */
    ClaimSearchResult getClaimByCoordinates(String x, String y, String projectId, String langCode);
    
    /**
     * See {@linkplain SearchEJB#getSpatialClaim(String, String)}.
     */
    ClaimSearchResult searchClaimById(String id, String langCode);
    
    /**
     * See {@linkplain SearchCSEJB#getAllClaims(String)}.
     */
    List<ClaimSpatialSearchResult> getAllClaims(String projectId);
    
    /**
     * See {@linkplain SearchEJB#searchClaims(org.sola.services.ejb.search.repository.entities.ClaimSearchParams)}.
     */
    List<ClaimSearchResult> searchClaims(ClaimSearchParams searchParams);
    
    /**
     * See {@linkplain SearchEJB#searchMap(java.lang.String, java.lang.String)}.
     */
    List<MapSearchResult> searchMap(String searchString, String projectId);
    
    /**
     * See {@linkplain SearchCSEJB#searchAssignedClaims(String, String)}.
     */
    List<ClaimSearchResult> searchAssignedClaims(String langCode, String projectId);
    
    /**
     * See {@linkplain SearchCSEJB#searchClaimsForReview(String, String, Boolean)}.
     */
    List<ClaimSearchResult> searchClaimsForReview(String langCode, String projectId, boolean includeAssigned);
    
    /**
     * See {@linkplain SearchCSEJB#searchClaimsForModeration(String, String Boolean)}.
     */
    List<ClaimSearchResult> searchClaimsForModeration(String langCode, String projectId, boolean includeAssigned);
    
    /**
     * See {@linkplain SearchEJB#searchAllAdministrativeBoundaries()}.
     */
    List<AdministrativeBoundarySearchResult> searchAllAdministrativeBoundaries(String projectId, String langCode);
    
    /**
     * See {@linkplain SearchEJB#searchApprovedAdministrativeBoundaries()}.
     */
    List<AdministrativeBoundarySearchResult> searchApprovedAdministrativeBoundaries(String projectId, String langCode);
    
    /**
     * See {@linkplain SearchEJB#searchParentAdministrativeBoundaries()}.
     */
    List<AdministrativeBoundarySearchResult> searchAllParentAdministrativeBoundaries(String projectId, String langCode);
    
    /**
     * See {@linkplain SearchEJB#searchChildAdministrativeBoundaries()}.
     */
    List<AdministrativeBoundarySearchResult> searchChildAdministrativeBoundaries(String parentId, String projectId, String langCode);
    
    /**
     * See {@linkplain SearchEJB#searchParentAdministrativeBoundaries()}.
     */
    List<AdministrativeBoundarySearchResult> searchParentAdministrativeBoundaries(String id, String projectId, String langCode);
    
    /**
     * See {@linkplain SearchEJB#getAdministrativeBoundaryByCoordinates(String, String, String)}.
     */
    AdministrativeBoundaryWithGeomSearchResult getAdministrativeBoundaryByCoordinates(String x, String y, String projectId, String langCode);
    
    /**
     * See {@linkplain SearchEJB#getFullLocation(String, String, String)}.
     */
    String getFullLocation(String boundaryId, String projectId, String langCode);
  
    /**
     * See {@linkplain SearchEJB#getGeoJsonClaimsByBoundary(String)}.
     */
    List<GeoJsonClaim> getGeoJsonClaimsByBoundary(String boundaryId, String projectId);
    
    /**
     * See {@linkplain SearchEJB#getGeoJsonAdministrativeBoundary(String)}.
     */
    GeoJsonAdministrativeBoundary getGeoJsonAdministrativeBoundary(String id, String projectId);
    
    /**
     * See {@linkplain SearchEJB#searchClaimsForPublicDisplay(String, String)}.
     */
    List<PublicDisplaySearchResult> searchClaimsForPublicDisplay(String langCode, String boundaryId, String projectId);
    
    /**
     * See {@linkplain SearchCSEJB#searchCurrentUserProjects()}.
     */
    List<ProjectNameSearchResult> searchCurrentUserProjects();
    
    /**
     * See {@linkplain SearchCSEJB#searchAllProjects()}.
     */
    List<ProjectNameSearchResult> searchAllProjects();
    
    /**
     * See {@link SearchCSEJB#searchCurrentUserProjectConfig()}.
     */
    List<ProjectConfigForClient> searchCurrentUserProjectConfig();
}
