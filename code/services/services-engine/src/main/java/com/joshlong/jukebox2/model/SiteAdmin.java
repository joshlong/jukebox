package com.joshlong.jukebox2.model;
// Generated Apr 5, 2010 1:20:15 AM by Hibernate Tools 3.2.0.CR1

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * SiteAdmin generated by hbm2java
 */
@Entity
@Table(name = "site_admin"
        , schema = "public"
)
public class SiteAdmin implements java.io.Serializable {

    private long id;
    private Set<UserCredentials> userCredentialses = new HashSet<UserCredentials>(0);

    public SiteAdmin() {
    }

    public SiteAdmin(long id) {
        this.id = id;
    }

    public SiteAdmin(long id, Set<UserCredentials> userCredentialses) {
        this.id = id;
        this.userCredentialses = userCredentialses;
    }

    @Id

    @Column(name = "id", unique = true, nullable = false)
    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "siteAdmin")
    public Set<UserCredentials> getUserCredentialses() {
        return this.userCredentialses;
    }

    public void setUserCredentialses(Set<UserCredentials> userCredentialses) {
        this.userCredentialses = userCredentialses;
    }

}


