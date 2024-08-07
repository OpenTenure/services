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
package org.sola.cs.services.ejb.system.businesslogic;

import org.sola.cs.services.ejb.system.repository.entities.BrDefinition;
import org.sola.cs.services.ejb.system.repository.entities.EmailTask;
import org.sola.cs.services.ejb.system.repository.entities.BrValidation;
import org.sola.cs.services.ejb.system.repository.entities.BrReport;
import org.sola.cs.services.ejb.system.repository.entities.ConfigMapLayer;
import org.sola.cs.services.ejb.system.repository.entities.Br;
import org.sola.cs.services.ejb.system.repository.entities.Setting;
import org.sola.cs.services.ejb.system.repository.entities.Query;
import org.sola.cs.services.ejb.system.repository.entities.Crs;
import org.sola.cs.services.ejb.system.repository.entities.BrCurrent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.sola.common.ConfigConstants;
import org.sola.common.RolesConstants;
import org.sola.common.SOLAException;
import org.sola.common.StringUtility;
import org.sola.common.mapping.MappingManager;
import org.sola.cs.common.messaging.ServiceMessage;
import org.sola.services.common.EntityAction;
import org.sola.services.common.br.ValidationResult;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.cs.services.ejb.search.businesslogic.SearchCSEJBLocal;
import org.sola.cs.services.ejb.system.br.Result;
import org.sola.cs.services.ejb.system.repository.entities.Project;
import org.sola.cs.services.ejb.system.repository.entities.ProjectSetting;
import org.sola.cs.services.ejb.system.repository.entities.ReportDescription;

/**
 * System EJB - Provides access to SOLA System data including business rules
 */
@Stateless
@EJB(name = "java:app/SystemCSEJBLocal", beanInterface = SystemCSEJBLocal.class)
public class SystemCSEJB extends AbstractEJB implements SystemCSEJBLocal {

    @EJB
    private SearchCSEJBLocal searchEJB;

    /**
     * Sets the entity package for the EJB to Br.class.getPackage().getName().
     * This is used to restrict the save and retrieval of Code Entities.
     *
     * @see AbstractEJB#getCodeEntity(java.lang.Class, java.lang.String,
     * java.lang.String) AbstractEJB.getCodeEntity
     * @see AbstractEJB#getCodeEntityList(java.lang.Class, java.lang.String)
     * AbstractEJB.getCodeEntityList
     * @see
     * AbstractEJB#saveCodeEntity(org.sola.services.common.repository.entities.AbstractCodeEntity)
     * AbstractEJB.saveCodeEntity
     */
    @Override
    protected void postConstruct() {
        setEntityPackage(Br.class.getPackage().getName());
    }

    /**
     * Returns all configuration settings in the system.setting table.
     */
    @Override
    public List<Setting> getAllSettings() {
        // NOTE: Will return settings from the cache by default. 
        return getRepository().getEntityList(Setting.class);
    }

    @Override
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SETTINGS)
    public Setting saveSetting(Setting setting) {
        return getRepository().saveEntity(setting);
    }

    /**
     * Retrieves the value for the named setting. Constants for each setting are
     * available in {@linkplain ConfigConstants}. If the setting does not exist,
     * the default value for the setting is returned. If project overrides the
     * settings, project related value will be returned.
     *
     * @param name The name of the setting to retrieve
     * @param projectId Project ID
     * @param defaultValue The default value for the setting if it no override
     * value is recorded in the system.settings table.
     * @return The override value for the setting or the defaultValue.
     */
    @Override
    public String getSetting(String name, String projectId, String defaultValue) {
        String result = defaultValue;
        Setting config = getSetting(name, projectId);
        if (config != null && config.getValue() != null) {
            result = config.getValue();
        }
        return result;
    }
    
    @Override
    public Setting getSetting(String name, String projectId) {
        // Use getAllSettings to obtain the cached settings. 
        List<Setting> settings = getAllSettings();
        List<ProjectSetting> projectSettings = getAllProjectsSettings();
        for (Setting config : settings) {
            if (config.getName().equals(name) && config.isActive()) {
                // Check if project has overriden setting
                if (!StringUtility.isEmpty(projectId)) {
                    for (ProjectSetting pSetting : projectSettings) {
                        if (pSetting.getProjectId().equalsIgnoreCase(projectId) && pSetting.getName().equals(name)) {
                            config = MappingManager.getMapper().map(config, Setting.class);
                            config.setValue(pSetting.getValue());
                            return config;
                        }
                    }
                }
                return config;
            }
        }
        return null;
    }

    /**
     * Returns all configuration settings in for all projects.
     * @return 
     */
    public List<ProjectSetting> getAllProjectsSettings() {
        // NOTE: Will return settings from the cache by default. 
        return getRepository().getEntityList(ProjectSetting.class);
    }
    
    /**
     * Returns the SOLA business rule matching the id.
     *
     * <p>
     * Requires the {@linkplain RolesConstants.ADMIN_MANAGE_SECURITY} role.</p>
     *
     * @param id Identifier for the business rule to return
     * @param lang The language code to use to localize the display value for
     * each Br.
     *
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_BR)
    @Override
    public Br getBr(String id, String lang) {
        if (lang == null) {
            return getRepository().getEntity(Br.class, id);
        } else {
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, lang);
            return getRepository().getEntity(Br.class, id, lang);
        }
    }

    @Override
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_BR)
    public boolean deleteBr(String brId) {
        if (StringUtility.isEmpty(brId)) {
            return true;
        }
        Br br = getBr(brId, null);
        if (br != null) {
            // Delete validations
            if (br.getBrValidationList() != null) {
                for (BrValidation validation : br.getBrValidationList()) {
                    validation.setEntityAction(EntityAction.DELETE);
                    getRepository().saveEntity(validation);
                }
            }
            // Delete definitions
            if (br.getBrDefinitionList() != null) {
                for (BrDefinition def : br.getBrDefinitionList()) {
                    def.setEntityAction(EntityAction.DELETE);
                    getRepository().saveEntity(def);
                }
            }
            br.setEntityAction(EntityAction.DELETE);
            try {
                getRepository().saveEntity(br);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Can be used to create a new business rule or save any updates to the
     * details of an existing business role.
     *
     * <p>
     * Requires the {@linkplain RolesConstants.ADMIN_MANAGE_SECURITY} role. </p>
     *
     * @param br The business rule to save.
     * @return The updated/new business rule.
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_BR)
    @Override
    public Br saveBr(Br br) {
        return getRepository().saveEntity(br);
    }

    /**
     * Retrieves the br specified by the id from the system.br_current view. The
     * view lists all br's that are currently active.
     *
     * @param id The identifier of the br to retrieve.
     * @param languageCode The language code to localize the display values and
     * validation messages for the business rule.
     * @throws SOLAException If the business rule is not found
     */
    private BrCurrent getBrCurrent(String id, String languageCode) {
        BrCurrent result = null;
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, languageCode);
        params.put(CommonSqlProvider.PARAM_WHERE_PART, BrCurrent.QUERY_WHERE_BYID);
        params.put(BrCurrent.QUERY_PARAMETER_ID, id);
        result = getRepository().getEntity(BrCurrent.class, params);
        if (result == null) {
            throw new SOLAException(ServiceMessage.RULE_NOT_FOUND, new Object[]{id});
        }
        return result;
    }

    /**
     * Returns the Br Report for the specified business rule.
     *
     * @param id Identifer of the business rule to retrieve the report for.
     */
    @Override
    public BrReport getBrReport(String id) {
        Map params = new HashMap<String, Object>();
        return getRepository().getEntity(BrReport.class, id);
    }

    /**
     * Returns a list of business rules matching the supplied ids.
     *
     * <p>
     * No role is required to execute this method.</p>
     *
     * @param ids The list of business rule ids
     */
    @Override
    public List<BrReport> getBrs(List<String> ids) {
        Map params = new HashMap<String, Object>();
        return getRepository().getEntityListByIds(BrReport.class, ids);
    }

    /**
     * Returns a br report for every business rule in the system.br table.
     *
     * <p>
     * No role is required to execute this method.</p>
     */
    @Override
    public List<BrReport> getAllBrs() {
        return getRepository().getEntityList(BrReport.class);
    }

    /**
     * Retrieves the business rules required validate an application for the
     * specified momentCode. Business rules are returned in the order indicated
     * by the system.br_validation.order_of_execution.
     *
     * @param momentCode The code indicating the action being applied to the
     * application. Used to obtain the subset of application business rules that
     * apply for a specific action. Must be <code>validate</code> or
     * <code>approve</code>.
     */
    @Override
    public List<BrValidation> getBrForValidatingApplication(String momentCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, BrValidation.QUERY_WHERE_FORAPPLICATION);
        params.put(BrValidation.QUERY_PARAMETER_MOMENTCODE, momentCode);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, BrValidation.QUERY_ORDERBY_ORDEROFEXECUTION);
        return getRepository().getEntityList(BrValidation.class, params);
    }

    /**
     * Retrieves the business rules required validate services for the specified
     * momentCode. Business rules are returned in the order indicated by the
     * system.br_validation.order_of_execution.
     *
     * @param momentCode The code indicating the action being applied to the
     * service. Used to obtain the subset of service business rules that apply
     * for a specific action. Must be <code>start</code> or
     * <code>complete</code>.
     * @param requestTypeCode The type of service being validated. Allows
     * services of different types to have different business rules applied.
     */
    @Override
    public List<BrValidation> getBrForValidatingService(
            String momentCode, String requestTypeCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, BrValidation.QUERY_WHERE_FORSERVICE);
        params.put(BrValidation.QUERY_PARAMETER_MOMENTCODE, momentCode);
        params.put(BrValidation.QUERY_PARAMETER_REQUESTTYPE, requestTypeCode);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, BrValidation.QUERY_ORDERBY_ORDEROFEXECUTION);
        return getRepository().getEntityList(BrValidation.class, params);
    }

    /**
     * Retrieves the business rules required validate rrr for the specified
     * momentCode. Business rules are returned in the order indicated by the
     * system.br_validation.order_of_execution.
     *
     * @param momentCode The code indicating the action being applied to the
     * rrr. Used to obtain the subset of rrr business rules that apply for a
     * specific action. Must be <code>current</code> or <code>pending</code>.
     * @param rrrType The type of rrr being validated. Allows rrr of different
     * types to have different business rules applied
     */
    @Override
    public List<BrValidation> getBrForValidatingRrr(String momentCode, String rrrType) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, BrValidation.QUERY_WHERE_FORRRR);
        params.put(BrValidation.QUERY_PARAMETER_MOMENTCODE, momentCode);
        params.put(BrValidation.QUERY_PARAMETER_RRRTYPE, rrrType);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, BrValidation.QUERY_ORDERBY_ORDEROFEXECUTION);
        return getRepository().getEntityList(BrValidation.class, params);
    }

    /**
     * Retrieves the business rules required validate a transaction. Business
     * rules are returned in the order indicated by the
     * system.br_validation.order_of_execution.
     *
     * @param targetCode The target to validate. Must be one of
     * <code>application</code>, <code>service</code>, <code>source</code>,
     * <code>ba_unit</code>, <code>rrr</code> or <code>cadastre_object</code>.
     * @param momentCode The code indicating the action being applied to the
     * transaction. Used to obtain the subset of business rules that apply for a
     * specific action. Must be <code>current</code> or <code>pending</code>.
     * @param requestTypeCode The type of service being validated associated
     * with the transaction.
     */
    @Override
    public List<BrValidation> getBrForValidatingTransaction(
            String targetCode, String momentCode, String requestTypeCode) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, BrValidation.QUERY_WHERE_FOR_TRANSACTION);
        params.put(BrValidation.QUERY_PARAMETER_TARGETCODE, targetCode);
        params.put(BrValidation.QUERY_PARAMETER_REQUESTTYPE, requestTypeCode);
        params.put(BrValidation.QUERY_PARAMETER_MOMENTCODE, momentCode);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, BrValidation.QUERY_ORDERBY_ORDEROFEXECUTION);
        return getRepository().getEntityList(BrValidation.class, params);
    }

    /**
     * Retrieves the business rules required to validate the printing of public
     * display report for a certain last part. <br/>
     * For this business rules, there is no needed a moment to be provided.
     */
    @Override
    public List<BrValidation> getBrForPublicDisplay() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, BrValidation.QUERY_WHERE_FOR_PUBLIC_DISPLAY);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, BrValidation.QUERY_ORDERBY_ORDEROFEXECUTION);
        return getRepository().getEntityList(BrValidation.class, params);
    }

    /**
     * Executes the rule using the appropriate rules engine. Currently only SQL
     * rules are supported, but JBOSS Drools rules could be supported in future.
     *
     * @param br The business rule to execute
     * @param parameters The parameters the business rule operates on
     * @return Hashmap containing the rule results
     * @throws SOLAException If execution of the rule fails.
     * @see
     * org.sola.services.ejb.search.businesslogic.SearchEJB#getResultObjectFromStatement(java.lang.String,
     * java.util.Map) SearchEJB.getResultObjectFromStatement
     */
    private HashMap checkRuleBasic(
            BrCurrent br, HashMap<String, Serializable> parameters) {
        HashMap ruleResult = null;
        try {
//            if (br.getTechnicalTypeCode().equals("drools")) {
//                //Here is supposed to come the code which runs the business rule using drools engine.
//            } 
            if (br.getTechnicalTypeCode().equals("sql")) {
                String sqlStatement = br.getBody();
                ruleResult = searchEJB.getResultObjectFromStatement(sqlStatement, parameters);
                if (ruleResult == null) {
                    ruleResult = new HashMap();
                }
                if (!ruleResult.containsKey(Result.VALUE_FIELD_NAME)) {
                    ruleResult.put(Result.VALUE_FIELD_NAME, null);
                }
            }
            return ruleResult;
        } catch (Exception ex) {
            throw new SOLAException(ServiceMessage.RULE_FAILED_EXECUTION, new Object[]{br.getId(), ex});
        }
    }

    /**
     * Executes a business rule an returns a single value as the result.
     *
     * @param brName The name of the business rule to execute.
     * @param parameters The parameters for the business rule.
     * @see #getBrCurrent(java.lang.String, java.lang.String) getBrCurrent
     * @see
     * #checkRuleBasic(org.sola.services.ejb.system.repository.entities.BrCurrent,
     * java.util.HashMap) checkRuleBasic
     */
    @Override
    public Result checkRuleGetResultSingle(
            String brName, HashMap<String, Serializable> parameters) {
        BrCurrent br = this.getBrCurrent(brName, "en");
        Result result = new Result();
        result.setName(brName);
        HashMap rawResult = this.checkRuleBasic(br, parameters);
        result.setValue(rawResult.get(Result.VALUE_FIELD_NAME));
        return result;
    }

    /**
     * Executes a set of business rules and returns the validation messages
     * resulting from the validation
     *
     * @param brListToValidate The list of business rules to execute
     * @param languageCode The language code to use to localize the validation
     * messages
     * @param parameters The parameters for the business rules
     * @return The list of validation messages.
     * @see
     * #checkRuleGetValidation(org.sola.services.ejb.system.repository.entities.BrValidation,
     * java.lang.String, java.util.HashMap) checkRuleGetValidation
     */
    @Override
    public List<ValidationResult> checkRulesGetValidation(
            List<BrValidation> brListToValidate, String languageCode,
            HashMap<String, Serializable> parameters) {
        List<ValidationResult> validationResultList = new ArrayList<ValidationResult>();
        if (brListToValidate != null) {
            for (BrValidation brForValidation : brListToValidate) {
                ValidationResult validationResult
                        = this.checkRuleGetValidation(brForValidation, languageCode, parameters);
                if (validationResult != null) {
                    validationResultList.add(validationResult);
                }
            }
        }
        return validationResultList;
    }

    /**
     * Obtains the current definition for the rule to execute from the database,
     * executes the rule and returns the results in the form of validation
     * result feedback.
     *
     * @param brForValidation The business rule to load and execute
     * @param languageCode The locale to use for retrieving the rule feedback
     * messages
     * @param parameters Parameters the business rule operates on
     * @return The feedback messages obtained from executing the business rules.
     */
    private ValidationResult checkRuleGetValidation(
            BrValidation brForValidation, String languageCode,
            HashMap<String, Serializable> parameters) {

        BrCurrent br = this.getBrCurrent(brForValidation.getBrId(), languageCode);
        ValidationResult result = new ValidationResult();
        result.setName(br.getId());
        HashMap rawResult = this.checkRuleBasic(br, parameters);
        // Result can be null for some checks, in that case return null. A ValidationResult that is
        //null will not be added to validation result list.
        if (rawResult.get(Result.VALUE_FIELD_NAME) == null) {
            //rawResult.put(Result.VALUE_FIELD_NAME, Boolean.TRUE);
            return null;
        }
        result.setSuccessful(rawResult.get(Result.VALUE_FIELD_NAME).equals(Boolean.TRUE));
        //Replace parameters if they exist
        String feedback = br.getFeedback();
        for (Object keyObj : rawResult.keySet()) {
            if (keyObj.equals(Result.VALUE_FIELD_NAME)) {
                continue;
            }
            feedback = feedback.replace(keyObj.toString(), rawResult.get(keyObj).toString());
        }
        result.setFeedback(feedback);
        result.setSeverity(brForValidation.getSeverityCode());
        return result;
    }

    /**
     * Checks all validation messages to determine if the validation succeeded
     * or not. The validation fails if any
     * {@linkplain BrValidation#SEVERITY_CRITICAL critical} business rule fails.
     *
     * @param validationResultList The list of validations to check.
     * @return <code>false</code> if at least one critical validation fails,
     * <code>true</code> otherwise.
     */
    @Override
    public boolean validationSucceeded(List<ValidationResult> validationResultList) {
        for (ValidationResult validationResult : validationResultList) {
            if (validationResult.getSeverity().equals(BrValidation.SEVERITY_CRITICAL)
                    && !validationResult.isSuccessful()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the business rules required to check the correctness of spatial
     * unit group. <br/>
     * For this business rules, there is no needed a moment to be provided.
     */
    @Override
    public List<BrValidation> getBrForSpatialUnitGroupTransaction() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, BrValidation.QUERY_WHERE_FOR_SPATIAL_UNIT_GROUP);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, BrValidation.QUERY_ORDERBY_ORDEROFEXECUTION);
        return getRepository().getEntityList(BrValidation.class, params);
    }

    /**
     * Retrieves the business rules required to check the correctness of
     * consolidation information.
     */
    @Override
    public List<BrValidation> getBrForConsolidation() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, BrValidation.QUERY_WHERE_FOR_CONSOLIDATION);
        params.put(CommonSqlProvider.PARAM_ORDER_BY_PART, BrValidation.QUERY_ORDERBY_ORDEROFEXECUTION);
        return getRepository().getEntityList(BrValidation.class, params);
    }

    /**
     * Returns all emails that need to be send at the current time.
     */
    @Override
    public List<EmailTask> getEmailsToSend() {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, EmailTask.WHERE_BY_NOW);
        return getRepository().getEntityList(EmailTask.class, params);
    }

    /**
     * Returns all email tasks
     */
    @Override
    public List<EmailTask> getEmails() {
        return getRepository().getEntityList(EmailTask.class);
    }

    /**
     * Saves email task.
     *
     * @param emailTask Email task to save
     * @return
     */
    @Override
    public EmailTask saveEmailTask(EmailTask emailTask) {
        return getRepository().saveEntity(emailTask);
    }

    /**
     * Saves email task.
     *
     * @param id Email task ID
     * @return
     */
    @Override
    public EmailTask getEmailTask(String id) {
        return getRepository().getEntity(EmailTask.class, id);
    }

    /**
     * Returns true if email service is enabled on the system, otherwise false.
     * @param projectId
     */
    @Override
    public boolean isEmailServiceEnabled(String projectId) {
        return getSetting(ConfigConstants.EMAIL_ENABLE_SERVICE, projectId, "0").equals("1");
    }

    /**
     * Send simple email to the given address
     *
     * @param recipientName Recipient name (full name)
     * @param recipientAddress Recipient email address
     * @param subject Subject of the message
     * @param body Message text
     */
    @Override
    public void sendEmail(String recipientName, String recipientAddress, String body, String subject) {
        EmailTask task = new EmailTask();
        task.setId(UUID.randomUUID().toString());
        task.setBody(body);
        task.setRecipient(recipientAddress);
        task.setRecipientName(recipientName);
        task.setSubject(subject);
        saveEmailTask(task);
    }

    /**
     * Returns list of available CRS
     *
     * @return
     */
    @Override
    public List<Crs> getCrss() {
        return getRepository().getEntityList(Crs.class);
    }

    /**
     * Returns CRS by provided srid
     *
     * @param srid srid of CRS
     * @return
     */
    @Override
    public Crs getCrs(int srid) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, "srid=" + srid);
        return getRepository().getEntity(Crs.class, params);
    }

    /**
     * Saves provided CRS
     *
     * @param crs CRS object to save
     * @return
     */
    @Override
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SETTINGS)
    public Crs saveCrs(Crs crs) {
        return getRepository().saveEntity(crs);
    }

    /**
     * Returns list of layer queries
     *
     * @param locale Locale code
     * @return
     */
    @Override
    public List<Query> getQueries(String locale) {
        if (locale != null) {
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
            return getRepository().getEntityList(Query.class, params);
        }
        return getRepository().getEntityList(Query.class);
    }

    /**
     * Returns layer query
     *
     * @param name Query name
     * @param locale Locale code
     * @return
     */
    @Override
    public Query getQuery(String name, String locale) {
        if (locale != null) {
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
            params.put(CommonSqlProvider.PARAM_WHERE_PART, "name='" + name + "'");
            return getRepository().getEntity(Query.class, params);
        }
        return getRepository().getEntity(Query.class, name);
    }

    /**
     * Saves layer query
     *
     * @param query Query object to save
     * @return
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SETTINGS)
    @Override
    public Query saveQuery(Query query) {
        return getRepository().saveEntity(query);
    }

    /**
     * Returns list of map layers
     *
     * @param locale Locale code
     * @return
     */
    @Override
    public List<ConfigMapLayer> getConfigMapLayers(String locale) {
        if (locale != null) {
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
            return getRepository().getEntityList(ConfigMapLayer.class, params);
        }
        return getRepository().getEntityList(ConfigMapLayer.class);
    }

    /**
     * Returns map layer
     *
     * @param name Layer name
     * @param locale Locale code
     * @return
     */
    @Override
    public ConfigMapLayer getConfigMapLayer(String name, String locale) {
        if (locale != null) {
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
            params.put(CommonSqlProvider.PARAM_WHERE_PART, "name='" + name + "'");
            return getRepository().getEntity(ConfigMapLayer.class, params);
        }
        return getRepository().getEntity(ConfigMapLayer.class, name);
    }

    /**
     * Saves map layer
     *
     * @param mapLayer Map layer to save
     * @return
     */
    @RolesAllowed(RolesConstants.ADMIN_MANAGE_SETTINGS)
    @Override
    public ConfigMapLayer saveConfigMapLayer(ConfigMapLayer mapLayer) {
        return getRepository().saveEntity(mapLayer);
    }

    /**
     * Returns all reports
     *
     * @param locale Locale code
     * @return
     */
    @Override
    public List<ReportDescription> getAllReports(String locale) {
        if (locale != null) {
            Map params = new HashMap<String, Object>();
            params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
            return getRepository().getEntityList(ReportDescription.class, params);
        }
        return getRepository().getEntityList(ReportDescription.class);
    }

    /**
     * Returns reports filtered by display in menu flag
     *
     * @param locale Locale code
     * @param isForMenu Indicates whether to return reports for displaying in
     * the menu or not.
     * @return
     */
    @Override
    public List<ReportDescription> getReports(String locale, boolean isForMenu) {
        Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, locale);
        params.put(CommonSqlProvider.PARAM_WHERE_PART, "display_in_menu='" + (isForMenu ? "t" : "f") + "'");
        return getRepository().getEntityList(ReportDescription.class, params);
    }

    /**
     * Returns report by ID
     *
     * @param id Report ID
     * @param locale Locale code
     * @return
     */
    @Override
    public ReportDescription getReportById(String id, String locale) {
        if (locale != null) {
            return getRepository().getEntity(ReportDescription.class, id, locale);
        }
        return getRepository().getEntity(ReportDescription.class, id, locale);
    }
    
    /**
     * Returns project by ID
     *
     * @param id Project ID
     * @param lang Locale code
     * @return
     */
    @Override
    public Project getProject(String id, String lang) {
        return getRepository().getEntity(Project.class, id, lang);
    }

    /**
     * Returns project area/boundary
     *
     * @param projectId Project ID
     * @return
     */
    @Override
    public String getProjectArea(String projectId) {
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, "select ST_AsText(boundary) from system.project where id = #{projectId}");
        params.put("projectId", projectId);
        return getRepository().getScalar(String.class, params);
    }
    
    /**
     * Checks if current user can access provided project
     *
     * @param projectId Project ID
     * @return
     */
    @Override
    public boolean canAccessProject(String projectId) {
        if(StringUtility.isEmpty(projectId)){
            return false;
        }
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, "select count(1) > 0 from system.project_appuser pu inner join system.appuser u on pu.appuser_id = u.id where u.username = #{userName} and pu.project_id = #{projectId}");
        params.put("projectId", projectId);
        params.put("userName", getUserName());
        return getRepository().getScalar(Boolean.class, params);
    }
}
