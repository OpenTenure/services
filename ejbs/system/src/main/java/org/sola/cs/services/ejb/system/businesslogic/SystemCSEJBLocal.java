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
package org.sola.cs.services.ejb.system.businesslogic;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import jakarta.ejb.Local;
import java.sql.Connection;
import org.apache.ibatis.session.SqlSession;
import org.sola.services.common.br.ValidationResult;
import org.sola.services.common.ejbs.AbstractEJBLocal;
import org.sola.cs.services.ejb.system.br.Result;
import org.sola.cs.services.ejb.system.repository.entities.Br;
import org.sola.cs.services.ejb.system.repository.entities.BrReport;
import org.sola.cs.services.ejb.system.repository.entities.BrValidation;
import org.sola.cs.services.ejb.system.repository.entities.ConfigMapLayer;
import org.sola.cs.services.ejb.system.repository.entities.Crs;
import org.sola.cs.services.ejb.system.repository.entities.EmailTask;
import org.sola.cs.services.ejb.system.repository.entities.Project;
import org.sola.cs.services.ejb.system.repository.entities.Query;
import org.sola.cs.services.ejb.system.repository.entities.ReportDescription;
import org.sola.cs.services.ejb.system.repository.entities.Setting;

/**
 * The EJB local interface for the {@linkplain SystemCSEJB}. The SystemCSEJB provides access to SOLA
 * System data including business rules.
 */
@Local
public interface SystemCSEJBLocal extends AbstractEJBLocal {

    /**
     * See {@linkplain SystemCSEJB#getAllSettings() SystemCSEJB.getAllSettings}
     */
    List<Setting> getAllSettings();

    /**
     * See {@linkplain SystemCSEJB#getSetting(java.lang.String, java.lang.String, java.lang.String)  SystemCSEJB.getSetting}
     */
    String getSetting(String name, String projectId, String defaultValue);
    
    /** 
     * Returns setting object, by given name and project id
     * @param name Setting name
     * @return 
     */
    Setting getSetting(String name, String projectId);
        
    /** 
     * Saves system setting
     * @param setting Setting object to save
     * @return  
     */
    Setting saveSetting(Setting setting);

    /**
     * See {@linkplain org.sola.services.ejb.system.businesslogic.SystemCSEJB#getBr(java.lang.String, java.lang.String)
     * SystemCSEJB.getBr}
     */
    Br getBr(String id, String lang);

    /**
     * See {@linkplain org.sola.services.ejb.system.businesslogic.SystemCSEJB#saveBr(org.sola.services.ejb.system.repository.entities.Br)
     * SystemCSEJB.saveBr}
     */
    Br saveBr(Br br);
    
    boolean deleteBr(String brId);

    /**
     * See {@linkplain SystemCSEJB#getBrs(java.util.List)
     * SystemCSEJB.getBrs}
     */
    List<BrReport> getBrs(List<String> ids);

    /**
     * See {@linkplain SystemCSEJB#getAllBrs()
     * SystemCSEJB.getAllBrs}
     */
    List<BrReport> getAllBrs();
    
    /**
     * See {@linkplain SystemCSEJB#getAllReports(java.lang.String)
     * SystemCSEJB.getAllReports}
     * @param locale
     * @return 
     */
    List<ReportDescription> getAllReports(String locale);

    /**
     * See {@linkplain SystemCSEJB#getReports(java.lang.String, boolean) 
     * SystemCSEJB.getReports}
     * @param locale
     * @param isForMenu
     * @return 
     */
    List<ReportDescription> getReports(String locale, boolean isForMenu);
        
    /**
     * See {@linkplain SystemCSEJB#getReportById(java.lang.String, java.lang.String)
     * SystemCSEJB.getReportById}
     */
    ReportDescription getReportById(String id, String locale);
    
    /**
     * See {@linkplain SystemCSEJB#getBrReport(java.lang.String)
     * SystemCSEJB.getBrReport}
     */
    BrReport getBrReport(String id);

    /**
     * See {@linkplain SystemCSEJB#getBrForValidatingApplication(java.lang.String)
     * SystemCSEJB.getBrForValidatingApplication}
     */
    List<BrValidation> getBrForValidatingApplication(String momentCode);

    /**
     * See {@linkplain SystemCSEJB#getBrForValidatingService(java.lang.String, java.lang.String)
     * SystemCSEJB.getBrForValidatingService}
     */
    List<BrValidation> getBrForValidatingService(String momentCode, String requestTypeCode);

    /**
     * See {@linkplain SystemCSEJB#getBrForValidatingTransaction(java.lang.String, java.lang.String, java.lang.String)
     * SystemCSEJB.getBrForValidatingTransaction}
     */
    List<BrValidation> getBrForValidatingTransaction(
            String targetCode, String momentCode, String requestTypeCode);

    /**
     * See {@linkplain SystemCSEJB#getBrForValidatingRrr(java.lang.String, java.lang.String)
     * SystemCSEJB.getBrForValidatingRrr}
     */
    List<BrValidation> getBrForValidatingRrr(String momentCode, String rrrType);

    /**
     * See {@linkplain SystemCSEJB#getBrForPublicDisplay()
     * SystemCSEJB.getBrForPublicDisplay}
     */
    List<BrValidation> getBrForPublicDisplay();

    /**
     * See {@linkplain SystemCSEJB#checkRuleGetResultSingle(java.lang.String, java.util.HashMap)
     * SystemCSEJB.checkRuleGetResultSingle}
     */
    Result checkRuleGetResultSingle(
            String brName, HashMap<String, Serializable> parameters);

    /**
     * See {@linkplain SystemCSEJB#checkRulesGetValidation(java.util.List, java.lang.String, java.util.HashMap)
     * SystemCSEJB.checkRulesGetValidation}
     */
    List<ValidationResult> checkRulesGetValidation(
            List<BrValidation> brListToValidate, String languageCode,
            HashMap<String, Serializable> parameters);

    /**
     * See {@linkplain SystemCSEJB#validationSucceeded(java.util.List)
     * SystemCSEJB.validationSucceeded}
     */
    boolean validationSucceeded(List<ValidationResult> validationResultList);
    
    /**
     * See {@linkplain SystemCSEJB#getBrForSpatialUnitGroupTransaction()
     * SystemCSEJB.getBrForSpatialUnitGroupTransaction}
     */
    List<BrValidation> getBrForSpatialUnitGroupTransaction();

    /**
     * See {@linkplain SystemCSEJB#getBrForConsolidation()
     * SystemCSEJB.getBrForConsolidation}
     */
    List<BrValidation> getBrForConsolidation();
    
    /**
     * See {@linkplain SystemCSEJB#getEmailsToSend()
     * SystemCSEJB.getEmailsToSend}
     */
    List<EmailTask> getEmailsToSend();
    
    /**
     * See {@linkplain SystemCSEJB#getEmails()
     * SystemCSEJB.getEmails}
     */
    List<EmailTask> getEmails();
    
    /**
     * See {@linkplain SystemCSEJB#saveEmailTask(EmailTask)
     * SystemCSEJB.saveEmailTask}
     */
    EmailTask saveEmailTask(EmailTask emailTask);
    
    /**
     * See {@linkplain SystemCSEJB#getEmailTask(String)
     * SystemCSEJB.getEmailTask}
     */
    EmailTask getEmailTask(String id);
    
    /** See {@link SystemCSEJB#sendEmail(String, String, String, String) }*/
    void sendEmail(String recipientName, String recipientAddress, String body, String subject);
    
    /** See {@link SystemCSEJB#isEmailServiceEnabled(String) }*/
    boolean isEmailServiceEnabled(String projectId);
    
    /** See {@link SystemCSEJB#getCrss()}*/
    List<Crs> getCrss();
    
    /** See {@link SystemCSEJB#getCrs(int srid)}*/
    Crs getCrs(int srid);
    
    /** See {@link SystemCSEJB#saveCrs(Crs crs)}*/
    Crs saveCrs(Crs crs);
    
    /** See {@link SystemCSEJB#getQueries(String locale)}*/
    List<Query> getQueries(String locale);
    
    /** See {@link SystemCSEJB#getQuery(String name, String locale)}*/
    Query getQuery(String name, String locale);
    
    /** See {@link SystemCSEJB#saveQuery(Query query)}*/
    Query saveQuery(Query query);
    
    /** See {@link SystemCSEJB#getConfigMapLayers(String locale)}*/
    List<ConfigMapLayer> getConfigMapLayers(String locale);
    
    /** See {@link SystemCSEJB#getConfigMapLayer(String name, String locale)}*/
    ConfigMapLayer getConfigMapLayer(String name, String locale);
    
    /** See {@link SystemCSEJB#saveConfigMapLayer(ConfigMapLayer mapLayer)}*/
    ConfigMapLayer saveConfigMapLayer(ConfigMapLayer mapLayer);
    
    /** See {@link SystemCSEJB#getProject(String id, String lang)}*/
    Project getProject(String id, String lang);
    
    /** See {@link SystemCSEJB#getProjectArea(String projectId)}*/
    String getProjectArea(String projectId);
    
    boolean canAccessProject(String projectId);
}