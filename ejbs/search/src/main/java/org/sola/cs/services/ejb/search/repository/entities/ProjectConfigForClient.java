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
package org.sola.cs.services.ejb.search.repository.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

public class ProjectConfigForClient extends AbstractReadOnlyEntity {

    public static final String PARAM_USER_NAME = "userName";
    
    private static final String SELECT_PART = "select p.id, p.display_name, ST_AsText(p.boundary) as boundary,\n"
            + "(coalesce((select vl from system.project_setting where project_id = p.id and name = 'project-language'), (select vl from system.setting where name = 'project-language'), 'en-US')) as language_code,\n"
            + "(coalesce((select vl from system.project_setting where project_id = p.id and name = 'client-tiles-server-type'), (select vl from system.setting where name = 'client-tiles-server-type'), '')) as tiles_server_type,\n"
            + "(coalesce((select vl from system.project_setting where project_id = p.id and name = 'client-tiles-server-url'), (select vl from system.setting where name = 'client-tiles-server-url'), '')) as tiles_server_url,\n"
            + "(coalesce((select vl from system.project_setting where project_id = p.id and name = 'client-tiles-layer-name'), (select vl from system.setting where name = 'client-tiles-layer-name'), '')) as tiles_layer_name, \n"
            + "(coalesce((select vl from system.project_setting where project_id = p.id and name = 'requires_spatial'), (select vl from system.setting where name = 'requires_spatial'), '1')) = '1' as geom_required \n"
            + "from system.project p \n";
    
    private static final String WHERE_BY_USER = "where id in ("
            + "select project_id from system.project_appuser pu inner join "
            + "system.appuser u on pu.appuser_id = u.id "
            + "where u.username = #{" + PARAM_USER_NAME + "}) \n";
    
    private static final String ORDER_BY = "order by display_name";
    
    public static final String QUERY_BY_USER = SELECT_PART + WHERE_BY_USER + ORDER_BY;

    @Id
    @Column
    private String id;
    @Column(name = "display_name")
    private String displayName;
    
    @Column
    private String boundary;
    
    @Column(name = "language_code")
    private String languageCode;
    
    @Column(name = "tiles_server_type")
    private String tilesServerType;
    
    @Column(name = "tiles_server_url")
    private String tilesServerUrl;
    
    @Column(name = "tiles_layer_name")
    private String tilesLayerName;
    
    @Column(name = "geom_required")
    private boolean geomRequired;
    
    public ProjectConfigForClient() {
        super();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBoundary() {
        return boundary;
    }

    public void setBoundary(String boundary) {
        this.boundary = boundary;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getTilesServerType() {
        return tilesServerType;
    }

    public void setTilesServerType(String tilesServerType) {
        this.tilesServerType = tilesServerType;
    }

    public String getTilesServerUrl() {
        return tilesServerUrl;
    }

    public void setTilesServerUrl(String tilesServerUrl) {
        this.tilesServerUrl = tilesServerUrl;
    }

    public String getTilesLayerName() {
        return tilesLayerName;
    }

    public void setTilesLayerName(String tilesLayerName) {
        this.tilesLayerName = tilesLayerName;
    }

    public boolean isGeomRequired() {
        return geomRequired;
    }

    public void setGeomRequired(boolean geomRequired) {
        this.geomRequired = geomRequired;
    }
}
