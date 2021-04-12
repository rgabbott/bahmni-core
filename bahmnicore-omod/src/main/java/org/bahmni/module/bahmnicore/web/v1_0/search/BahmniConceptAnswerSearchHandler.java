package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.bahmnicore.service.BahmniConceptService;
import org.bahmni.module.bahmnicore.web.v1_0.contract.BahmniConceptAnswer;
import org.openmrs.ConceptAnswer;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Component
public class BahmniConceptAnswerSearchHandler implements SearchHandler {

    public static final String QUESTION_KEY = "question";
    public static final String QUERY = "q";
    private BahmniConceptService bahmniConceptService;

    @Autowired
    public BahmniConceptAnswerSearchHandler(BahmniConceptService bahmniConceptService) {
        this.bahmniConceptService = bahmniConceptService;
    }

    @Override
    public SearchConfig getSearchConfig() {
        SearchQuery searchQuery = new SearchQuery.Builder("Allows you to search for concepts based on a question").withRequiredParameters(QUESTION_KEY).build();
        return new SearchConfig("byQuestion", RestConstants.VERSION_1 + "/bahmniconceptanswer", Arrays.asList("1.9.*", "1.10.*", "1.11.*", "1.12.*","2.0.*", "2.1.*"), searchQuery);
    }

    @Override
    public PageableResult search(RequestContext requestContext) throws ResponseException {
        String questionConceptName = requestContext.getParameter(QUESTION_KEY);
        String query = requestContext.getParameter(QUERY);
        Collection<ConceptAnswer> conceptAnswers = bahmniConceptService.searchByQuestion(questionConceptName, query);

        ArrayList<BahmniConceptAnswer> bahmniConceptAnswers = new ArrayList<>();
        for (ConceptAnswer answer : conceptAnswers) {
            bahmniConceptAnswers.add(BahmniConceptAnswer.create(answer));
        }

        return new NeedsPaging<>(bahmniConceptAnswers, requestContext);
    }
}
