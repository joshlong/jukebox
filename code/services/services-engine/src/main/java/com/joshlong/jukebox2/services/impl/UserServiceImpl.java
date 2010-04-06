package com.joshlong.jukebox2.services.impl;

import com.joshlong.jukebox2.services.impl.util.BaseService;

import com.joshlong.jukebox2.services.workflow.WorkflowService;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class UserServiceImpl extends BaseService /*implements UserService*/ {
  /*  @Autowired
    private PromoterService promoterService;

    @Autowired
    private WorkflowService workflowService;

    public Customer createCustomerAndUserCredentials(String user, String email,
                                                     String password, String first, String last) {
        UserCredentials userCredentials = null;
        Customer customer = null;
        try {

            userCredentials = createUserCredentials(user, email, password,
                    first, last);
            customer = createCustomerForUserCredentials(userCredentials.getId());
            setUserCredentialsProvidedFtpAccount(userCredentials.getId(), false);

            // TODO we should provide the ability to send a welcome email
            // now we send them an email
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("customerId", customer.getId());
            params.put("userCredentialsId", userCredentials.getId());
            workflowService.createAndStartProcessInstance(
                    "verify-customer-email", params);

            // create-customer-agent
            workflowService.completeAllTaskInstancesByActorAndCriteria(
                    "create-customer-agent", params);

            return customer;
        } catch (Throwable t) {
            throw new QuietRiotException(t);
        }
    }



    private SiteAdmin createSiteAdminForUserCredentials(long userCredentialsId) {
        SiteAdmin siteAdmin = new SiteAdmin();

        UserCredentials uc = getUserCredentialsById(userCredentialsId);
        hibernateTemplate.saveOrUpdate(siteAdmin);
        uc.setSiteAdmin(siteAdmin);

        hibernateTemplate.saveOrUpdate(uc);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteAdminId", siteAdmin.getId());

        workflowService.completeAllTaskInstancesByActorAndCriteria(
                "create-siteAdmin-agent", params);

        return siteAdmin;
    }


    public UserCredentials getUserCredentialsById(long ucId) {
        return (UserCredentials) hibernateTemplate.get(
                UserCredentials.class, ucId);

    }

    public UserCredentials getUserCredentialsByEmail(String email) {
        if (!StringUtils.isEmpty(email)) {
            UserCredentials uc = firstOrNull(hibernateTemplate
                    .findByNamedParam(
                    "select uc from "
                            + UserCredentials.class.getName()
                            + " uc where  LTRIM(RTRIM(LOWER(uc.email))) =  :uc",
                    "uc",
                    StringUtils.defaultString(email).trim()
                            .toLowerCase()));
            return uc;
        }
        return null;
    }


    public UserCredentials getUserCredentialsByLogin(String userName) {
        return firstOrNull(hibernateTemplate.findByNamedParam(
                "select uc from " + UserCredentials.class.getName()
                        + " uc where uc.userName = :uc", "uc", userName));
    }

    public UserCredentials login(String user, String pw) {
        // todo one way hashing of pw entered into form and as stored in the db;
        // how would that affect the ftp user functionality? Can we configure
        // the hashing to be a psql crypto function?
        UserCredentials u = getUserCredentialsByLogin(user);
        return (null != u && u.getPassphrase().equals(pw)) ? u : null;
    }

    public boolean isUserCredentialEmailDuplicate(String email) {
        return (getUserCredentialsByEmail(email) != null);
    }

    public boolean isUserCredentialActive(long ucId) {
        UserCredentials u = getUserCredentialsById(ucId);
        return u.isActive();
    }
    public UserCredentials createUserCredentials(String userName, String email,
                                                 String password, String firstName, String lastName) {

        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setFirstName(firstName);
        userCredentials.setLastName(lastName);
        userCredentials.setEmail(email);
        userCredentials.setPassphrase(password);
        userCredentials.setActive(false);
        userCredentials.setUserName(userName);
        userCredentials.setProvideFtpAccount(true);

        // first we check for dupes
        if (getUserCredentialsByEmail(email) == null
                && getUserCredentialsByLogin(userName) == null) {
            hibernateTemplate.saveOrUpdate(userCredentials);
            return userCredentials;
        }
        throw new QuietRiotException("Couldn't createUserCredential"
                + "s! Credentials (password, email) already exists");
    }

    public void setUserCredentialsActive(long ucId, boolean activeState) {
        UserCredentials userCredentials = getUserCredentialsById(ucId);
        userCredentials.setActive(activeState);
        hibernateTemplate.saveOrUpdate(userCredentials);

    }
    // simple for now - this is mainly only used in communiques sent to the user
    public String createHashForUserCredentials(long userCredentials) {
        return Long.toBinaryString(userCredentials);
    }

    public long getUserCredentialsIdFromHash(String hash) {
        return Long.parseLong(hash, 2);
    }
*/
}
