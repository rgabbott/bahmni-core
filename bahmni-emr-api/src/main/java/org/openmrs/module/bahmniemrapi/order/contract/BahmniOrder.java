package org.openmrs.module.bahmniemrapi.order.contract;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Collection;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BahmniOrder {
    private String orderUuid;
    private String orderNumber;
    private String orderTypeUuid;
    private String provider;
    private Date orderDate;
    private EncounterTransaction.Concept concept;
    private Boolean hasObservations;
    private Collection<BahmniObservation> bahmniObservations;
    private String commentToFulfiller;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Boolean getHasObservations() {
        return hasObservations;
    }

    public void setHasObservations(Boolean hasObservations) {
        this.hasObservations = hasObservations;
    }

    public Collection<BahmniObservation> getBahmniObservations() {
        return bahmniObservations;
    }

    public void setBahmniObservations(Collection<BahmniObservation> bahmniObservations) {
        this.bahmniObservations = bahmniObservations;
    }

    public String getOrderUuid() {
        return orderUuid;
    }

    public void setOrderUuid(String orderUuid) {
        this.orderUuid = orderUuid;
    }

    public String getOrderTypeUuid() {
        return orderTypeUuid;
    }

    public void setOrderTypeUuid(String orderTypeUuid) {
        this.orderTypeUuid = orderTypeUuid;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDateTime(Date orderDate) {
        this.orderDate = orderDate;
    }

    public EncounterTransaction.Concept getConcept() {
        return concept;
    }

    public void setConcept(EncounterTransaction.Concept concept) {
        this.concept = concept;
    }

    public String getCommentToFulfiller() {
        return commentToFulfiller;
    }

    public void setCommentToFulfiller(String commentToFulfiller) {
        this.commentToFulfiller = commentToFulfiller;
    }
}
