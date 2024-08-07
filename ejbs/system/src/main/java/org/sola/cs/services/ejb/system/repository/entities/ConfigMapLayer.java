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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sola.cs.services.ejb.system.repository.entities;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.sola.services.common.repository.ChildEntityList;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.Localized;
import org.sola.services.common.repository.entities.AbstractEntity;

@Table(name = "config_map_layer", schema = "system")
@DefaultSorter(sortString = "item_order")
public class ConfigMapLayer extends AbstractEntity {

    @Id
    @Column
    private String name;
    
    @Column(name = "type_code")
    private String typeCode;
    
    @Column(name = "active")
    private boolean active;
    
    @Column(name = "url")
    private String url;
    
    @Column(name = "wms_layers")
    private String wmsLayers;
    
    @Column(name = "wms_version")
    private String wmsVersion;
    
    @Column(name = "wms_format")
    private String wmsFormat;
    
    @Column(name = "wms_data_source")
    private String wmsDataSource;
    
    @Column(name = "pojo_query_name")
    private String pojoQueryName;
    
    @Column(name = "pojo_query_name_for_select")
    private String pojoQueryNameForSelect;
    
    @Column(name = "pojo_structure")
    private String pojoStructure;
    
    @Column(name = "shape_location")
    private String shapeLocation;
    
    @Column(name = "style")
    private String style;
    
    @Localized
    @Column(name = "title")
    private String title;
    
    @Column(name = "visible_in_start")
    private boolean visible;
    
    @Column(name = "security_user")
    private String securityUser;
    
    @Column(name = "security_password")
    private String securityPassword;
    
    @Column(name = "use_in_public_display")
    private boolean useInPublicDisplay;
    
    @Column(name = "use_for_ot")
    private boolean useForOpenTenure;
    
    @Column(name = "item_order")
    private int itemOrder;
    
    @Column(name = "added_from_bulk_operation")
    private boolean addedFromBulkOperation;
    
    @ChildEntityList(parentIdField = "nameLayer")
    private List<ConfigMapLayerMetadata> metadataList;

    public ConfigMapLayer() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPojoQueryName() {
        return pojoQueryName;
    }

    public void setPojoQueryName(String pojoQueryName) {
        this.pojoQueryName = pojoQueryName;
    }

    public String getPojoQueryNameForSelect() {
        return pojoQueryNameForSelect;
    }

    public void setPojoQueryNameForSelect(String pojoQueryNameForSelect) {
        this.pojoQueryNameForSelect = pojoQueryNameForSelect;
    }

    public String getPojoStructure() {
        return pojoStructure;
    }

    public void setPojoStructure(String pojoStructure) {
        this.pojoStructure = pojoStructure;
    }

    public String getShapeLocation() {
        return shapeLocation;
    }

    public void setShapeLocation(String shapeLocation) {
        this.shapeLocation = shapeLocation;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getWmsLayers() {
        return wmsLayers;
    }

    public void setWmsLayers(String wmsLayers) {
        this.wmsLayers = wmsLayers;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getSecurityPassword() {
        return securityPassword;
    }

    public void setSecurityPassword(String securityPassword) {
        this.securityPassword = securityPassword;
    }

    public String getSecurityUser() {
        return securityUser;
    }

    public void setSecurityUser(String securityUser) {
        this.securityUser = securityUser;
    }

    public String getWmsFormat() {
        return wmsFormat;
    }

    public void setWmsFormat(String wmsFormat) {
        this.wmsFormat = wmsFormat;
    }

    public String getWmsVersion() {
        return wmsVersion;
    }

    public void setWmsVersion(String wmsVersion) {
        this.wmsVersion = wmsVersion;
    }

    public boolean isUseInPublicDisplay() {
        return useInPublicDisplay;
    }

    public void setUseInPublicDisplay(boolean useInPublicDisplay) {
        this.useInPublicDisplay = useInPublicDisplay;
    }

    public List<ConfigMapLayerMetadata> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<ConfigMapLayerMetadata> metadataList) {
        this.metadataList = metadataList;
    }

    public boolean isUseForOpenTenure() {
        return useForOpenTenure;
    }

    public void setUseForOpenTenure(boolean useForOpenTenure) {
        this.useForOpenTenure = useForOpenTenure;
    }

    public String getWmsDataSource() {
        return wmsDataSource;
    }

    public void setWmsDataSource(String wmsDataSource) {
        this.wmsDataSource = wmsDataSource;
    }

    public int getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(int itemOrder) {
        this.itemOrder = itemOrder;
    }

    public boolean isAddedFromBulkOperation() {
        return addedFromBulkOperation;
    }

    public void setAddedFromBulkOperation(boolean addedFromBulkOperation) {
        this.addedFromBulkOperation = addedFromBulkOperation;
    }
}
