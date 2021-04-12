package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptSearchResult;
import org.openmrs.api.ConceptService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class BahmniConceptSearchByDataTypeHandler implements SearchHandler {

    public static final String NAME = "name";
    public static final String DATA_TYPES = "dataTypes";

    @Autowired
    @Qualifier("conceptService")
    private ConceptService conceptService;

    @Override
    public SearchConfig getSearchConfig() {
        SearchQuery searchQuery = new SearchQuery.Builder("Allows you to search for concepts by data types").withRequiredParameters(NAME, DATA_TYPES).build();
        return new SearchConfig("byDataType", RestConstants.VERSION_1 + "/concept", Arrays.asList("1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*"), searchQuery);
    }

    @Override
    public PageableResult search(RequestContext context) throws ResponseException {

        String conceptName = context.getParameter(NAME);
        String dataTypes = context.getParameter(DATA_TYPES);
        List<String> supportedDataTypes = Arrays.asList(dataTypes.split(","));

        List<ConceptDatatype> conceptDatatypes =new ArrayList<>();

        for( String dataType: supportedDataTypes){
            ConceptDatatype conceptDatatype = conceptService.getConceptDatatypeByName(dataType.trim());
            if(conceptDatatype != null) {
                conceptDatatypes.add(conceptDatatype);
            }
        }

        List<Concept> concepts = new ArrayList<>();

        List<ConceptSearchResult> conceptsByName =
                conceptService.getConcepts(conceptName, null, false, null, null, conceptDatatypes, null, null, context.getStartIndex(), context.getLimit());

        for (ConceptSearchResult csr : conceptsByName) {
            if (csr.getConcept() != null)
                concepts.add(csr.getConcept());
        }

        return new NeedsPaging<Concept>(concepts, context);

    }

}