package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.BahmniCoreException;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplate;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplateConfig;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplatesConfig;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.ObservationTemplate;
import org.bahmni.module.bahmnicore.mapper.ObservationTemplateMapper;
import org.bahmni.module.bahmnicore.service.BahmniConceptService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.service.BahmniVisitService;
import org.bahmni.module.bahmnicore.service.DiseaseTemplateService;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class DiseaseTemplateServiceImpl implements DiseaseTemplateService {

    private BahmniObsService bahmniObsService;
    private BahmniVisitService bahmniVisitService;
    private ConceptService conceptService;
    private VisitService visitService;
    private PatientService patientService;
    private ConceptMapper conceptMapper;
    private ObservationTemplateMapper observationTemplateMapper;
    private BahmniConceptService bahmniConceptService;
    private final String CASE_INTAKE_CONCEPT_CLASS = "Case Intake";
    private static final org.apache.log4j.Logger log = Logger.getLogger(DiseaseTemplateServiceImpl.class);

    @Autowired
    public DiseaseTemplateServiceImpl(BahmniObsService bahmniObsService, BahmniVisitService bahmniVisitService,
                                      ConceptService conceptService,
                                      PatientService patientService, VisitService visitService,
                                      BahmniConceptService bahmniConceptService) {
        this.bahmniObsService = bahmniObsService;
        this.bahmniVisitService = bahmniVisitService;
        this.conceptService = conceptService;
        this.bahmniConceptService = bahmniConceptService;
        this.conceptMapper = new ConceptMapper();
        this.observationTemplateMapper = new ObservationTemplateMapper(conceptMapper);
        this.patientService = patientService;
        this.visitService = visitService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiseaseTemplate> allDiseaseTemplatesFor(DiseaseTemplatesConfig diseaseTemplatesConfig) {
        List<DiseaseTemplate> diseaseTemplates = new ArrayList<>();

        for (DiseaseTemplateConfig diseaseTemplateConfig : diseaseTemplatesConfig.getDiseaseTemplateConfigList()) {
            String templateName = diseaseTemplateConfig.getTemplateName();
            Concept diseaseTemplateConcept = conceptService.getConceptByName(templateName);
            DiseaseTemplate diseaseTemplate = new DiseaseTemplate(mapToETConcept(diseaseTemplateConcept, templateName));
            diseaseTemplate.addObservationTemplates(createObservationTemplates(diseaseTemplatesConfig.getPatientUuid(),
                    diseaseTemplateConcept, diseaseTemplatesConfig.getStartDate(), diseaseTemplatesConfig.getEndDate()));
            List<String> showOnlyConceptsForTheDisease = getShowOnlyConceptsForTheDisease(diseaseTemplate, diseaseTemplatesConfig);
            if (CollectionUtils.isNotEmpty(showOnlyConceptsForTheDisease)) {
                filterObs(diseaseTemplate, showOnlyConceptsForTheDisease);
            }
            diseaseTemplates.add(diseaseTemplate);
        }
        return diseaseTemplates;
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseTemplate diseaseTemplateFor(DiseaseTemplatesConfig diseaseTemplatesConfig) {
        if(CollectionUtils.isEmpty(diseaseTemplatesConfig.getDiseaseTemplateConfigList())){
            throw new BahmniCoreException("Disease template not found");
        }
        String templateName = diseaseTemplatesConfig.getDiseaseTemplateConfigList().get(0).getTemplateName();
        Concept diseaseTemplateConcept = conceptService.getConceptByName(templateName);
        DiseaseTemplate diseaseTemplate = new DiseaseTemplate(mapToETConcept(diseaseTemplateConcept, templateName));
        if (diseaseTemplateConcept == null) {
            log.warn("Disease template concept " + templateName + " not found. Please check your configuration");
            return diseaseTemplate;
        }
        List<Concept> observationTemplateConcepts = diseaseTemplateConcept.getSetMembers();
        for (Concept concept : observationTemplateConcepts) {
            Collection<BahmniObservation> observations = bahmniObsService.observationsFor(diseaseTemplatesConfig.getPatientUuid(),
                                    Arrays.asList(concept), null, null, false, null, diseaseTemplatesConfig.getStartDate(), diseaseTemplatesConfig.getEndDate());
            List<ObservationTemplate> observationTemplates = observationTemplateMapper.map(observations, concept);
            diseaseTemplate.addObservationTemplates(observationTemplates);
        }

        return diseaseTemplate;
    }

    private EncounterTransaction.Concept mapToETConcept(Concept concept, String conceptName) {
        if (concept == null) {
            return new EncounterTransaction.Concept(null, conceptName, false, null, null, null, null,null);
        }
        return conceptMapper.map(concept);
    }

    private List<String> getShowOnlyConceptsForTheDisease(DiseaseTemplate diseaseTemplate, DiseaseTemplatesConfig diseaseTemplatesConfig) {
        if (diseaseTemplate.getConcept().getName() == null) return new ArrayList<>();

        for (DiseaseTemplateConfig diseaseTemplateConfig : diseaseTemplatesConfig.getDiseaseTemplateConfigList()) {
            if (diseaseTemplateConfig.getTemplateName().equals(diseaseTemplate.getConcept().getName())) {
                return diseaseTemplateConfig.getShowOnly();
            }
        }
        return new ArrayList<>();
    }

    private void filterObs(DiseaseTemplate diseaseTemplate, List<String> showOnly) {
        List<ObservationTemplate> removableObservationTemplate = new ArrayList<>();
        for (ObservationTemplate observationTemplate : diseaseTemplate.getObservationTemplates()) {
            if (!isExists(observationTemplate.getConcept(), showOnly)) {
                filterObs(observationTemplate, showOnly);
                if (observationTemplate.getBahmniObservations().size() == 0) {
                    removableObservationTemplate.add(observationTemplate);
                }
            }
        }
        diseaseTemplate.getObservationTemplates().removeAll(removableObservationTemplate);
    }

    private void filterObs(ObservationTemplate observationTemplate, List<String> conceptNames) {
        Collection<BahmniObservation> removableObservation = new ArrayList<>();
        for (BahmniObservation bahmniObservation : observationTemplate.getBahmniObservations()) {
            if (!isExists(bahmniObservation.getConcept(), conceptNames)) {
                if (bahmniObservation.getGroupMembers().size() > 0) {
                    filterObsGroupMembers(bahmniObservation, conceptNames);
                }
                if (bahmniObservation.getGroupMembers().size() == 0) {
                    removableObservation.add(bahmniObservation);
                }
            }
        }
        observationTemplate.removeBahmniObservations(removableObservation);
    }

    private void filterObsGroupMembers(BahmniObservation parent, List<String> conceptNames) {
        Collection<BahmniObservation> removableObservation = new ArrayList<>();
        for (BahmniObservation bahmniObservation : parent.getGroupMembers()) {
            if (!isExists(bahmniObservation.getConcept(), conceptNames)) {
                if (bahmniObservation.getGroupMembers().size() > 0) {
                    filterObsGroupMembers(bahmniObservation, conceptNames);
                }
                if (bahmniObservation.getGroupMembers().size() == 0) {
                    removableObservation.add(bahmniObservation);
                }
            }
        }

        parent.removeGroupMembers(removableObservation);
    }

    private boolean isExists(EncounterTransaction.Concept concept, List<String> conceptNames) {
        return conceptNames.contains(concept.getName());
    }

    private List<ObservationTemplate> createObservationTemplates(String patientUuid, Concept diseaseTemplateConcept, Date startDate, Date endDate) {
        List<ObservationTemplate> observationTemplates = new ArrayList<>();
        Patient patient = patientService.getPatientByUuid(patientUuid);
        List<Visit> visits = visitService.getVisitsByPatient(patient);
        if (null != diseaseTemplateConcept && CollectionUtils.isNotEmpty(diseaseTemplateConcept.getSetMembers())) {
            for (Concept concept : diseaseTemplateConcept.getSetMembers()) {
                if (concept.getConceptClass().getName().equals(CASE_INTAKE_CONCEPT_CLASS) && CollectionUtils.isNotEmpty(visits)) {
                    Collection<BahmniObservation> observations = bahmniObsService.observationsFor(patientUuid, Arrays.asList(concept), null, null, false, null, startDate, endDate);
                    observationTemplates.addAll(observationTemplateMapper.map(observations, concept));
                } else {
                    Visit latestVisit = bahmniVisitService.getLatestVisit(patientUuid, concept.getName().getName());
                    if (latestVisit != null) {
                        getObservationTemplate(observationTemplates, patientUuid, concept, latestVisit, startDate, endDate);
                    }
                }
            }
        }
        return observationTemplates;
    }

    private void getObservationTemplate(List<ObservationTemplate> observationTemplates, String patientUuid, Concept concept, Visit latestVisit, Date startDate, Date endDate) {
        Collection<BahmniObservation> observations =
                bahmniObsService.getLatestObsForConceptSetByVisit(patientUuid, concept.getName(Context.getLocale()).getName(), latestVisit.getVisitId());
        for (Iterator<BahmniObservation> iterator = observations.iterator(); iterator.hasNext();) {
            BahmniObservation observation = iterator.next();
            if((startDate != null && observation.getObservationDateTime().before(startDate)) || (endDate != null && observation.getObservationDateTime().after(endDate))) {
                    iterator.remove();
            }
        }
        if (CollectionUtils.isNotEmpty(observations)) {
            observationTemplates.add(createObservationTemplate(concept, latestVisit, observations));
        }
    }

    private ObservationTemplate createObservationTemplate(Concept concept, Visit visit, Collection<BahmniObservation> observations) {
        ObservationTemplate observationTemplate = new ObservationTemplate();
        observationTemplate.setVisitStartDate(visit.getStartDatetime());
        observationTemplate.setConcept(conceptMapper.map(concept));
        observationTemplate.setBahmniObservations(observations);
        return observationTemplate;
    }

}
