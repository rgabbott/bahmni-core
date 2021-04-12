package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class VisitFormsSearchHandlerTest {

    @InjectMocks
    private VisitFormsSearchHandler visitFormsSearchHandler = new VisitFormsSearchHandler();
    @Mock
    RequestContext context;
    @Mock
    PatientService patientService;
    @Mock
    ConceptService conceptService;
    @Mock
    EncounterService encounterService;
    @Mock
    VisitService visitService;
    @Mock
    ObsService obsService;
    @Mock
    private BahmniProgramWorkflowService programWorkflowService;
    @Mock
    private EpisodeService episodeService;

    private Patient patient;
    private Concept concept;
    private Obs obs;

    @Before
    public void before() throws Exception {
        initMocks(this);
        setUp();
    }

    public Concept createConcept(String conceptName, String locale) {
        concept = new Concept();
        concept.setFullySpecifiedName(new ConceptName(conceptName, new Locale(locale)));
        return concept;
    }

    public Obs createObs(Concept concept) {
        obs = new Obs();
        obs.setConcept(concept);
        return obs;
    }

    public void setUp() throws Exception {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(context.getLimit()).thenReturn(3);
        when(context.getRequest()).thenReturn(req);
        when(context.getRequest().getParameter("patient")).thenReturn("patientUuid");
        when(context.getRequest().getParameter("numberOfVisits")).thenReturn("10");

        String[] conceptNames = {"Vitals"};
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(conceptNames);
        patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        PowerMockito.mockStatic(Context.class);
        PowerMockito.when(Context.getPatientService()).thenReturn(patientService);
        when(patientService.getPatientByUuid("patientUuid")).thenReturn(patient);
        PowerMockito.when(Context.getConceptService()).thenReturn(conceptService);
        concept = createConcept("Vitals", "en");

        Visit visit = new Visit();
        PowerMockito.when(Context.getVisitService()).thenReturn(visitService);
        PowerMockito.when(Context.getVisitService().getVisitsByPatient(patient)).thenReturn(Arrays.asList(visit));

        PowerMockito.when(Context.getEncounterService()).thenReturn(encounterService);
        Encounter encounter = mock(Encounter.class);
        PowerMockito.when(encounterService.getEncounters(any(Patient.class), any(Location.class), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), eq(false))).thenReturn(Arrays.asList(encounter));
        PowerMockito.when(Context.getObsService()).thenReturn(obsService);
        obs = createObs(concept);
    }

    @Test
    public void testGetSearchConfig() throws Exception {
        SearchConfig searchConfig = visitFormsSearchHandler.getSearchConfig();
        assertThat(searchConfig.getId(), is(equalTo("byPatientUuid")));

    }

    @Test
    public void shouldSupportVersions1_10To1_12() {
        SearchConfig searchConfig = visitFormsSearchHandler.getSearchConfig();
        assertTrue(searchConfig.getSupportedOpenmrsVersions().contains("1.10.*"));
        assertTrue(searchConfig.getSupportedOpenmrsVersions().contains("1.11.*"));
        assertTrue(searchConfig.getSupportedOpenmrsVersions().contains("1.12.*"));
    }

    @Test
    public void shouldReturnConceptSpecificObsIfConceptNameIsSpecified() throws Exception {
        String [] conceptNames = new String[]{"Vitals"};
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(conceptNames);
        concept = createConcept("Vitals", "en");

        PowerMockito.when(conceptService.getConcept("All Observation Templates")).thenReturn(concept);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(obs));
        NeedsPaging<Obs> searchResults = (NeedsPaging<Obs>) visitFormsSearchHandler.search(context);
        assertThat(searchResults.getPageOfResults().size(), is(equalTo(1)));
    }

    @Test
    public void shouldReturnAllObsIfConceptNameIsNotSpecified() throws Exception {

        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(null);

        Concept parentConcept = new Concept();
        parentConcept.addSetMember(concept);
        Concept historyConcept = createConcept("History and Examination", "en");
        parentConcept.addSetMember(historyConcept);

        PowerMockito.when(conceptService.getConcept("All Observation Templates")).thenReturn(parentConcept);
        Obs obs2 = createObs(historyConcept);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(obs, obs2));
        NeedsPaging<Obs> searchResults = (NeedsPaging<Obs>) visitFormsSearchHandler.search(context);
        assertThat(searchResults.getPageOfResults().size(), is(equalTo(2)));
    }

    @Test
    public void shouldReturnEmptyObservationsIfAllConceptNamesAreInvalid() throws Exception {

        String[] conceptNames = {null, null};
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(conceptNames);

        Concept parentConcept = new Concept();
        parentConcept.addSetMember(concept);
        Concept historyConcept = createConcept("History and Examination", "en");
        parentConcept.addSetMember(historyConcept);

        PowerMockito.when(conceptService.getConcept("All Observation Templates")).thenReturn(parentConcept);
        PowerMockito.when(Context.getConceptService()).thenReturn(conceptService);
        PowerMockito.when(conceptService.getConceptByName(null)).thenReturn(null);

        Obs obs2 = createObs(historyConcept);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(obs, obs2));
        NeedsPaging<Obs> searchResults = (NeedsPaging<Obs>) visitFormsSearchHandler.search(context);
        assertThat(searchResults.getPageOfResults().size(), is(equalTo(0)));
    }

    @Test(expected = InvalidSearchException.class)
    public void shouldThrowExceptionIfThePatienUuidIsNull(){
        when(context.getRequest().getParameter("patient")).thenReturn(null);

        visitFormsSearchHandler.search(context);
    }

    @Test
    public void shouldGetObservationsWithinThePatientProgramIfThePatientProgramUuidIsPassed() throws Exception {
        when(conceptService.getConcept("All Observation Templates")).thenReturn(concept);
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(null);
        String patientProgramUuid = "patient-program-uuid";
        when(context.getRequest().getParameter("patientProgramUuid")).thenReturn(patientProgramUuid);
        when(Context.getService(BahmniProgramWorkflowService.class)).thenReturn(programWorkflowService);
        PatientProgram patientProgram = new BahmniPatientProgram();
        when(programWorkflowService.getPatientProgramByUuid(patientProgramUuid)).thenReturn(patientProgram);
        when(Context.getService(EpisodeService.class)).thenReturn(episodeService);
        Episode episode = new Episode();
        episode.addEncounter(new Encounter());
        when(episodeService.getEpisodeForPatientProgram(patientProgram)).thenReturn(episode);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(obs));

        visitFormsSearchHandler.search(context);

        verify(conceptService, times(1)).getConcept("All Observation Templates");
        verify(programWorkflowService, times(1)).getPatientProgramByUuid(patientProgramUuid);
        verify(episodeService, times(1)).getEpisodeForPatientProgram(patientProgram);
        verify(visitService, never()).getVisitsByPatient(patient);
        verify(encounterService, never()).getEncounters(any(Patient.class), any(Location.class), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), eq(false));
        verify(obsService, times(1)).getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false));
    }

    @Test
    public void shouldNotFetchAnyObservationsIfThereIsNoEpisodeForTheProgram() throws Exception {
        when(conceptService.getConcept("All Observation Templates")).thenReturn(concept);
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(null);
        String patientProgramUuid = "patient-program-uuid";
        when(context.getRequest().getParameter("patientProgramUuid")).thenReturn(patientProgramUuid);
        when(Context.getService(BahmniProgramWorkflowService.class)).thenReturn(programWorkflowService);
        PatientProgram patientProgram = new BahmniPatientProgram();
        when(programWorkflowService.getPatientProgramByUuid(patientProgramUuid)).thenReturn(patientProgram);
        when(Context.getService(EpisodeService.class)).thenReturn(episodeService);
        when(episodeService.getEpisodeForPatientProgram(patientProgram)).thenReturn(null);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(obs));

        visitFormsSearchHandler.search(context);

        verify(conceptService, times(1)).getConcept("All Observation Templates");
        verify(programWorkflowService, times(1)).getPatientProgramByUuid(patientProgramUuid);
        verify(episodeService, times(1)).getEpisodeForPatientProgram(patientProgram);
        verify(visitService, never()).getVisitsByPatient(patient);
        verify(encounterService, never()).getEncounters(any(Patient.class), any(Location.class), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), eq(false));
        verify(obsService, never()).getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false));
    }

    @Test
    public void shouldNotFetchAnyObservationsIfThereAreNoEncountersInEpisode() throws Exception {
        when(conceptService.getConcept("All Observation Templates")).thenReturn(concept);
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(null);
        String patientProgramUuid = "patient-program-uuid";
        when(context.getRequest().getParameter("patientProgramUuid")).thenReturn(patientProgramUuid);
        when(Context.getService(BahmniProgramWorkflowService.class)).thenReturn(programWorkflowService);
        PatientProgram patientProgram = new BahmniPatientProgram();
        when(programWorkflowService.getPatientProgramByUuid(patientProgramUuid)).thenReturn(patientProgram);
        when(Context.getService(EpisodeService.class)).thenReturn(episodeService);
        Episode episode = new Episode();
        when(episodeService.getEpisodeForPatientProgram(patientProgram)).thenReturn(episode);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(obs));

        visitFormsSearchHandler.search(context);

        verify(conceptService, times(1)).getConcept("All Observation Templates");
        verify(programWorkflowService, times(1)).getPatientProgramByUuid(patientProgramUuid);
        verify(episodeService, times(1)).getEpisodeForPatientProgram(patientProgram);
        verify(visitService, never()).getVisitsByPatient(patient);
        verify(encounterService, never()).getEncounters(any(Patient.class), any(Location.class), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), eq(false));
        verify(obsService, never()).getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false));
    }
}