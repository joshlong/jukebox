package com.joshlong.jukebox2.model;
// Generated Apr 5, 2010 1:20:15 AM by Hibernate Tools 3.2.0.CR1


import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Performer generated by hbm2java
 */
@Entity
@Table(name="performer"
    ,schema="public"
)
public class Performer  implements java.io.Serializable {


     private long id;
     private String firstName;
     private String position;
     private String lastName;
     private Set<PerformanceGroupAffiliation> performanceGroupAffiliations = new HashSet<PerformanceGroupAffiliation>(0);

    public Performer() {
    }

	
    public Performer(long id, String firstName, String position, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.position = position;
        this.lastName = lastName;
    }
    public Performer(long id, String firstName, String position, String lastName, Set<PerformanceGroupAffiliation> performanceGroupAffiliations) {
       this.id = id;
       this.firstName = firstName;
       this.position = position;
       this.lastName = lastName;
       this.performanceGroupAffiliations = performanceGroupAffiliations;
    }
   
     @Id 
    
    @Column(name="id", unique=true, nullable=false)
    public long getId() {
        return this.id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    @Column(name="first_name", nullable=false)
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    @Column(name="position", nullable=false, length=10)
    public String getPosition() {
        return this.position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    @Column(name="last_name", nullable=false)
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="performer")
    public Set<PerformanceGroupAffiliation> getPerformanceGroupAffiliations() {
        return this.performanceGroupAffiliations;
    }
    
    public void setPerformanceGroupAffiliations(Set<PerformanceGroupAffiliation> performanceGroupAffiliations) {
        this.performanceGroupAffiliations = performanceGroupAffiliations;
    }




}


