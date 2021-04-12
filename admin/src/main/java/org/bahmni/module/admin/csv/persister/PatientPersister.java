package org.bahmni.module.admin.csv.persister;

import org.apache.log4j.Logger;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.csv.models.PatientRow;
import org.bahmni.module.admin.csv.service.CSVAddressService;
import org.bahmni.module.admin.csv.service.CSVPatientService;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PatientPersister implements EntityPersister<PatientRow> {
    private UserContext userContext;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PersonService personService;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    @Qualifier("adminService")
    private AdministrationService administrationService;

    private CSVAddressService csvAddressService;

    private static final Logger log = Logger.getLogger(PatientPersister.class);

    public void init(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    public Messages persist(PatientRow patientRow) {
        try {
            Context.openSession();
            Context.setUserContext(userContext);

            new CSVPatientService(patientService, personService, conceptService, administrationService, getAddressHierarchyService()).save(patientRow);

            return new Messages();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            Context.clearSession();
            return new Messages(e);
        } finally {
            Context.flushSession();
            Context.closeSession();
        }
    }

    private CSVAddressService getAddressHierarchyService() {
        if (csvAddressService == null) {
            AddressHierarchyService addressHierarchyService = Context.getService(AddressHierarchyService.class);
            this.csvAddressService = new CSVAddressService(addressHierarchyService);
        }
        return csvAddressService;
    }

    @Override
    public Messages validate(PatientRow csvEntity) {
        return new Messages();
    }
}
