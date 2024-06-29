/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations
 * (FAO). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,this
 * list of conditions and the following disclaimer. 2. Redistributions in binary
 * form must reproduce the above copyright notice,this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. 3. Neither the name of FAO nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
import java.util.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.commons.lang.math.NumberUtils;
import org.sola.common.DateUtility;
import org.sola.common.RolesConstants;
import org.sola.common.StringUtility;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.cs.services.ejb.search.repository.entities.AdministrativeBoundarySearchResult;
import org.sola.cs.services.ejb.search.repository.entities.AdministrativeBoundaryWithGeomSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.GeoJsonAdministrativeBoundary;
import org.sola.cs.services.ejb.search.repository.entities.GeoJsonClaim;
import org.sola.cs.services.ejb.search.repository.entities.MapSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.ProjectConfigForClient;
import org.sola.cs.services.ejb.search.repository.entities.ProjectNameSearchResult;
import org.sola.cs.services.ejb.search.repository.entities.PublicDisplaySearchResult;

/**
 * SOLA EJB's have responsibility for managing data in one schema. This can
 * complicate searches that require interrogating data across multiple schemas
 * to obtain a result. If the strict rule to only allow EJBs to manage data in
 * one schema was applied, cross schema searches would require the use of
 * multiple EJBs to obtain several part datasets which would then need to be
 * sorted and filtered based on the users search criteria. That approach is very
 * inefficient compared to using SQL, so the SearchEJB has been created to allow
 * efficient searching for data across multiple schemas.
 *
 * <p>
 * The SearchEJB supports execution of dynamic SQL queries obtained from the
 * system.query table.</p>
 *
 * <p>
 * Note that this EJB has access to all SOLA database tables and it must be
 * treated as read only. It must not be used to persist data changes.</p>
 */
@Stateless
@EJB(name = "java:app/SearchCSEJBLocal", beanInterface = SearchCSEJBLocal.class)
public class SearchCSEJB extends AbstractEJB implements SearchCSEJBLocal {

    /**
     * Returns the first row of the result set obtained from the execution of a
     * dynamic SQL statement. Used for the execution of business rules.
     *
     * @param sqlStatement The SQL statement to execute
     * @param params The parameters for the SQL statement
     */
    @Override
    public HashMap getResultObjectFromStatement(String sqlStatement, Map params) {
        params = params == null ? new HashMap<String, Object>() : params;
        params.put(CommonSqlProvider.PARAM_QUERY, sqlStatement);
        // Returns a single result
        //return getRepository().getScalar(Object.class, params); 
        // To use if more than one result is required. 
        List<HashMap> resultList = getRepository().executeSql(params);
        HashMap result = null;
        if (!resultList.isEmpty()) {
            result = resultList.get(0);
        }
        return result;
    }
    
    /**
     * Returns the map layers for the project.
     *
     * @param projectId Project ID
     * @param languageCode The language code to use for localization of display
     * values.
     * @return
     */
    @Override
    public List<ConfigMapLayer> getMapLayersByProject(String projectId, String languageCode) {
        if (StringUtility.isEmpty(projectId)) {
            return null;
        }

        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, ConfigMapLayer.QUERY_BY_PROJECT);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, languageCode);
        params.put(ConfigMapLayer.PARAM_PROJECT_ID, projectId);
        return getRepository().getEntityList(ConfigMapLayer.class, params);
    }

    /**
     * Executes a search across all Business Rules. Partial matches of the br
     * display name are supported.
     * <p>
     * Requires the {@linkplain RolesConstants#ADMIN_MANAGE_BR} role.</p>
     *
     * @param searchParams The parameters to use for the search.
     * @param lang The language code to use for localization of display values
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_BR)
    @Override
    public List<BrSearchResult> searchBr(BrSearchParams searchParams, String lang) {
        Map params = new HashMap<String, Object>();

        if (searchParams.getDisplayName() == null) {
            searchParams.setDisplayName("");
        }
        if (searchParams.getTargetCode() == null) {
            searchParams.setTargetCode("");
        }
        if (searchParams.getTechnicalTypeCode() == null) {
            searchParams.setTechnicalTypeCode("");
        }

        searchParams.setDisplayName(searchParams.getDisplayName().trim());

        params.put(CommonSqlProvider.PARAM_QUERY, BrSearchResult.SELECT_QUERY);
        params.put("lang", lang);
        params.put("displayName", searchParams.getDisplayName());
        params.put("technicalTypeCode", searchParams.getTechnicalTypeCode());
        params.put("targetCode", searchParams.getTargetCode());
        return getRepository().getEntityList(BrSearchResult.class, params);
    }

    /**
     * Returns list of {@link ClaimSpatialSearchResult} objects by provided
     * bounding box.
     *
     * @param searchParams Search parameters
     * @return
     */
    @Override
    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<ClaimSpatialSearchResult> getClaimsByBox(ClaimSpatialSearchParams searchParams) {
        Map params = new HashMap();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, ClaimSpatialSearchResult.WHERE_SEARCH_BY_BOX);
        params.put(CommonSqlProvider.PARAM_LIMIT_PART, searchParams.getLimit());
        params.put(ClaimSearchResult.PARAM_RECORDER, getUserName());
        params.put(ClaimSearchResult.PARAM_PROJECT_ID, searchParams.getProjectId());
        params.put(ClaimSpatialSearchResult.PARAM_ENVELOPE,
                String.format(ClaimSpatialSearchResult.ENVELOPE,
                        searchParams.getMinX(), searchParams.getMinY(),
                        searchParams.getMaxX(), searchParams.getMaxY()));
        return getRepository().getEntityList(ClaimSpatialSearchResult.class, params);
    }

    /**
     * Returns list of {@link ClaimSpatialSearchResult} representing all claims.
     *
     * @param projectId Project id
     * @return
     */
    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<ClaimSpatialSearchResult> getAllClaims(String projectId) {
        Map params = new HashMap();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, ClaimSpatialSearchResult.WHERE_SEARCH_ALL);
        params.put(ClaimSearchResult.PARAM_RECORDER, getUserName());
        params.put(ClaimSearchResult.PARAM_PROJECT_ID, projectId);
        return getRepository().getEntityList(ClaimSpatialSearchResult.class, params);
    }

    /**
     * Returns {@link ClaimSearchResult} by x and y coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param projectId Project id
     * @param langCode Language code
     * @return
     */
    @Override
    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public ClaimSearchResult getClaimByCoordinates(String x, String y, String projectId, String langCode) {
        if (StringUtility.isEmpty(x) || StringUtility.isEmpty(y)) {
            return null;
        }

        String point = "POINT(" + x + " " + y + ")";
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_BY_POINT);
        params.put(ClaimSearchResult.PARAM_POINT, point);
        params.put(ClaimSearchResult.PARAM_PROJECT_ID, projectId);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        List<ClaimSearchResult> list = getRepository().getEntityList(ClaimSearchResult.class, params);
        if (list == null || list.size() < 1) {
            return null;
        } else {
            return list.get(0);
        }
    }
    
    @Override
    public ClaimSearchResult searchClaimById(String id, String langCode) {
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_BY_ID);
        params.put(ClaimSearchResult.PARAM_ID, id);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        return getRepository().getEntity(ClaimSearchResult.class, params);
    }
    

    /**
     * Searched and returns list of {@link ClaimSearchResult}.
     *
     * @param searchParams Search parameters
     * @return
     */
    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<ClaimSearchResult> searchClaims(ClaimSearchParams searchParams) {
        Map params = new HashMap<String, Object>();

        // prepare params
        if (searchParams.getLodgementDateFrom() != null || searchParams.getLodgementDateTo() != null) {
            searchParams.setLodgementDateFrom(DateUtility.minimizeDate(searchParams.getLodgementDateFrom()));
            searchParams.setLodgementDateTo(DateUtility.maximizeDate(searchParams.getLodgementDateTo()));
        }
        searchParams.setDescription(StringUtility.empty(searchParams.getDescription()));
        searchParams.setClaimNumber(StringUtility.empty(searchParams.getClaimNumber()));
        searchParams.setClaimantName(StringUtility.empty(searchParams.getClaimantName()));
        searchParams.setChallengeType(StringUtility.empty(searchParams.getChallengeType()));
        searchParams.setStatusCode(StringUtility.empty(searchParams.getStatusCode()));
        searchParams.setLanguageCode(StringUtility.empty(searchParams.getLanguageCode()));

        // put params into map
        params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, searchParams.getLanguageCode());
        params.put(ClaimSearchResult.PARAM_DATE_FROM, searchParams.getLodgementDateFrom());
        params.put(ClaimSearchResult.PARAM_DATE_TO, searchParams.getLodgementDateTo());
        params.put(ClaimSearchResult.PARAM_DESCRIPTION, searchParams.getDescription());
        params.put(ClaimSearchResult.PARAM_CLAIM_NUMBER, searchParams.getClaimNumber());
        params.put(ClaimSearchResult.PARAM_NAME, searchParams.getClaimantName());
        params.put(ClaimSearchResult.PARAM_SEARCH_BY_USER, searchParams.isSearchByUser());
        params.put(ClaimSearchResult.PARAM_RECORDER, getUserName());
        params.put(ClaimSearchResult.PARAM_PROJECT_ID, searchParams.getProjectId());
        params.put(ClaimSearchResult.PARAM_STATUS_CODE, searchParams.getStatusCode());
        params.put(ClaimSearchResult.PARAM_CHALLENGE_TYPE, searchParams.getChallengeType());

        return getRepository().getEntityList(ClaimSearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<MapSearchResult> searchMap(String searchString, String projectId) {
        Map params = new HashMap<String, Object>();
        String name = "";
        String point = "";
        String claimNumber = "";

        // prepare params
        if (!StringUtility.isEmpty(searchString)) {
            searchString = searchString.trim().replace("#", "");
            // Try to guess parameters
            String numbers[] = searchString.replace(" ", "").split(",");
            if (numbers != null && numbers.length > 1) {
                if (NumberUtils.isNumber(numbers[0]) && NumberUtils.isNumber(numbers[1])) {
                    point = String.format("POINT(%s %s)", numbers[0], numbers[1]);
                }
            } else {
                name = searchString;
                claimNumber = searchString;
            }
        }

        params.put(CommonSqlProvider.PARAM_QUERY, MapSearchResult.QUERY_SEARCH);
        params.put(MapSearchResult.PARAM_NAME, name);
        params.put(MapSearchResult.PARAM_PROJECT_ID, projectId);
        params.put(MapSearchResult.PARAM_CLAIM_NUMBER, claimNumber);
        params.put(MapSearchResult.PARAM_POINT, point);

        return getRepository().getEntityList(MapSearchResult.class, params);
    }

    @Override
    public List<ClaimSearchResult> searchAssignedClaims(String langCode, String projectId) {
        Map params = new HashMap<String, Object>();

        params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_ASSIGNED_TO_USER);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(ClaimSearchResult.PARAM_USERNAME, getUserName());
        params.put(ClaimSearchResult.PARAM_PROJECT_ID, projectId);
        return getRepository().getEntityList(ClaimSearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<ClaimSearchResult> searchClaimsForReview(String langCode, String projectId, boolean includeAssigned) {
        Map params = new HashMap<String, Object>();

        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(ClaimSearchResult.PARAM_PROJECT_ID, projectId);
        
        if (includeAssigned) {
            params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_FOR_REVIEW_ALL);
        } else {
            params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_FOR_REVIEW);
        }

        return getRepository().getEntityList(ClaimSearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<ClaimSearchResult> searchClaimsForModeration(String langCode, String projectId, boolean includeAssigned) {
        Map params = new HashMap<String, Object>();

        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(ClaimSearchResult.PARAM_PROJECT_ID, projectId);
        
        if (includeAssigned) {
            params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_FOR_MODERATION_ALL);
        } else {
            params.put(CommonSqlProvider.PARAM_QUERY, ClaimSearchResult.QUERY_SEARCH_FOR_MODERATION);
        }

        return getRepository().getEntityList(ClaimSearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<AdministrativeBoundarySearchResult> searchAllAdministrativeBoundaries(String projectId, String langCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundarySearchResult.QUERY_GET_ALL);
        params.put(AdministrativeBoundarySearchResult.PARAM_PROJECT_ID, projectId);
        return getRepository().getEntityList(AdministrativeBoundarySearchResult.class, params);
    }
    
    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<AdministrativeBoundarySearchResult> searchApprovedAdministrativeBoundaries(String projectId, String langCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(AdministrativeBoundarySearchResult.PARAM_PROJECT_ID, projectId);
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundarySearchResult.QUERY_GET_APPROVED);
        return getRepository().getEntityList(AdministrativeBoundarySearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<AdministrativeBoundarySearchResult> searchAllParentAdministrativeBoundaries(String projectId, String langCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(AdministrativeBoundarySearchResult.PARAM_PROJECT_ID, projectId);
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundarySearchResult.QUERY_GET_ALL_PARENTS);
        return getRepository().getEntityList(AdministrativeBoundarySearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<AdministrativeBoundarySearchResult> searchChildAdministrativeBoundaries(String projectId, String parentId, String langCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(AdministrativeBoundarySearchResult.PARAM_PARENT_ID, parentId);
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundarySearchResult.QUERY_GET_CHILDREN);
        return getRepository().getEntityList(AdministrativeBoundarySearchResult.class, params);
    }

    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<AdministrativeBoundarySearchResult> searchParentAdministrativeBoundaries(String id, String projectId, String langCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(AdministrativeBoundarySearchResult.PARAM_ID, id);
        params.put(AdministrativeBoundarySearchResult.PARAM_PROJECT_ID, projectId);
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundarySearchResult.QUERY_GET_PARENTS);
        return getRepository().getEntityList(AdministrativeBoundarySearchResult.class, params);
    }

    @Override
    public String getFullLocation(String boundaryId, String projectId, String langCode) {
        List<AdministrativeBoundarySearchResult> boundaries = searchParentAdministrativeBoundaries(boundaryId, projectId ,langCode);
        if (boundaries == null || boundaries.size() < 1) {
            return "";
        }
        String location = "";
        for (AdministrativeBoundarySearchResult boundary : boundaries) {
            if (location.equals("")) {
                location = boundary.getName();
            } else {
                location = location + ", " + boundary.getName();
            }
        }
        return location;
    }

    /**
     * Returns {@link AdministrativeBoundaryWithGeomSearchResult} by x and y
     * coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param projectId
     * @param langCode Language code
     * @return
     */
    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    @Override
    public AdministrativeBoundaryWithGeomSearchResult getAdministrativeBoundaryByCoordinates(String x, String y, String projectId, String langCode) {
        if (StringUtility.isEmpty(x) || StringUtility.isEmpty(y)) {
            return null;
        }

        String point = "POINT(" + x + " " + y + ")";
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, AdministrativeBoundaryWithGeomSearchResult.QUERY_SEARCH_BY_POINT);
        params.put(AdministrativeBoundaryWithGeomSearchResult.PARAM_POINT, point);
        params.put(AdministrativeBoundarySearchResult.PARAM_PROJECT_ID, projectId);
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        List<AdministrativeBoundaryWithGeomSearchResult> list = getRepository().getEntityList(AdministrativeBoundaryWithGeomSearchResult.class, params);
        if (list == null || list.size() < 1) {
            return null;
        } else {
            return list.get(0);
        }
    }

    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    @Override
    public List<GeoJsonClaim> getGeoJsonClaimsByBoundary(String boundaryId, String projectId) {
        HashMap params = new HashMap();
        if (StringUtility.isEmpty(boundaryId)) {
            params.put(CommonSqlProvider.PARAM_QUERY, GeoJsonClaim.QUERY_SEARCH_ALL);
        } else {
            params.put(CommonSqlProvider.PARAM_QUERY, GeoJsonClaim.QUERY_SEARCH_BY_BOUNDARY);
            params.put(GeoJsonClaim.PARAM_BOUNDARY_ID, boundaryId);
            params.put(GeoJsonClaim.PARAM_PROJECT_ID, projectId);
        }
        return getRepository().getEntityList(GeoJsonClaim.class, params);
    }

    //@RolesAllowed({RolesConstants.CS_ACCESS_CS})
    @Override
    public GeoJsonAdministrativeBoundary getGeoJsonAdministrativeBoundary(String id, String projectId) {
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, GeoJsonAdministrativeBoundary.QUERY_SEARCH_BY_ID);
        params.put(GeoJsonAdministrativeBoundary.PARAM_ID, id);
        params.put(GeoJsonAdministrativeBoundary.PARAM_PROJECT_ID, projectId);
        return getRepository().getEntity(GeoJsonAdministrativeBoundary.class, params);
    }
    
    @RolesAllowed({RolesConstants.CS_VIEW_REPORTS})
    @Override
    public List<PublicDisplaySearchResult> searchClaimsForPublicDisplay(String langCode, String boundaryId, String projectId){
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, langCode);
        params.put(PublicDisplaySearchResult.PARAM_BOUNDARY_ID, StringUtility.empty(boundaryId));
        params.put(PublicDisplaySearchResult.PARAM_PROJECT_ID, StringUtility.empty(projectId));
        params.put(CommonSqlProvider.PARAM_QUERY, PublicDisplaySearchResult.QUERY_SEARCH_BY_BOUNDARY);
        return getRepository().getEntityList(PublicDisplaySearchResult.class, params);
    }
    
    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<ProjectNameSearchResult> searchCurrentUserProjects() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, ProjectNameSearchResult.WHERE_BY_USER);
        params.put(ProjectNameSearchResult.PARAM_USER_NAME, getUserName());
        return getRepository().getEntityList(ProjectNameSearchResult.class, params);
    }

    @Override
    public List<ProjectNameSearchResult> searchAllProjects() {
        return getRepository().getEntityList(ProjectNameSearchResult.class);
    }
    
    @Override
    @RolesAllowed({RolesConstants.CS_ACCESS_CS})
    public List<ProjectConfigForClient> searchCurrentUserProjectConfig(){
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_QUERY, ProjectConfigForClient.QUERY_BY_USER);
        params.put(ProjectNameSearchResult.PARAM_USER_NAME, getUserName());
        return getRepository().getEntityList(ProjectConfigForClient.class, params);
    }
}
