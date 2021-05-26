package org.bahmni.module.bahmnicore.web.v1_0.controller_ethopian_calendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.EthiopicChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EthopianCalendarController extends BaseRestController {

	private static Logger logger = Logger.getLogger(EthopianCalendarController.class);

	@Autowired
	private BahmniVisitLocationService bahmniVisitLocationService;
 

	@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/ethdatetogmt/{date}/{month}/{year}", method = RequestMethod.GET)
	public ResponseEntity<DateConversion> getEthiopianDateConversionToGMT(@PathVariable String date,
			@PathVariable String month,@PathVariable String year) {
		DateConversion dateObj = new DateConversion();
//		String month = null;
//		String year = null;
//		String dateOf = null;

		Chronology chron_eth = EthiopicChronology.getInstance(DateTimeZone.forID("Africa/Addis_Ababa"));

// Create Ethiopian Date/Time (Y, D, M, H, M, S, mS)
		DateTime dt_eth = new DateTime(Integer.parseInt(year),Integer.parseInt(date), Integer.parseInt(month), 0, 0, 0, 0, chron_eth);

// Convert to Gregorian Date/Time
		DateTime dt_greg = dt_eth.withChronology(GregorianChronology.getInstance());

		String d1 = DateTimeFormat.longDate().print(dt_greg);
//		System.out.println(d1 + "D1");
		String s1 = d1;
		String[] words = s1.split("\\s");
		dateObj.setEatDate(d1);
		int i = 0;
		for (String w : words) {
			if (i == 0) {
				dateObj.setDate(w);
			}
			if (i == 2) {
				dateObj.setYear(w);
			}
			if (i == 1) {
				if (w.equalsIgnoreCase("January")) {
					w = "01";
				} else if (w.equalsIgnoreCase("February")) {
					w = "02";
				} else if (w.equalsIgnoreCase("March")) {
					w = "03";
				} else if (w.equalsIgnoreCase("April")) {
					w = "04";
				} else if (w.equalsIgnoreCase("May")) {
					w = "05";
				} else if (w.equalsIgnoreCase("June")) {
					w = "06";
				} else if (w.equalsIgnoreCase("July")) {
					w = "07";
				} else if (w.equalsIgnoreCase("August")) {
					w = "08";
				} else if (w.equalsIgnoreCase("September")) {
					w = "09";
				} else if (w.equalsIgnoreCase("October")) {
					w = "10";
				} else if (w.equalsIgnoreCase("November")) {
					w = "11";
				} else if (w.equalsIgnoreCase("December")) {
					w = "12";
				}
			
			dateObj.setMonth(w);
			
			}

			i++;
		}

		Date date1 = null;
		try {
			date1 = new SimpleDateFormat("dd/MM/yyyy").parse(dateObj.getDate() + "/" + dateObj.getMonth() + "/" + dateObj.getYear());
			dateObj.setGmtDate(date1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
//		System.out.println("Gregorian Date new: " + dateObj.toString());
		if (dateObj != null) {
			return new ResponseEntity(dateObj, HttpStatus.OK);
		} else {
			return new ResponseEntity(null, HttpStatus.BAD_GATEWAY);
		}
	}

	@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/gmttoethiopian/{date}/{month}/{year}", method = RequestMethod.GET)
	public ResponseEntity<?> getGMTDateConversionToEthiopian(@PathVariable String date,@PathVariable String month,@PathVariable String year) {
		DateConversion dateObj = new DateConversion();
//		String month = null;
//		String year = null;
//		String dateOf = null;
		Chronology chron_eth1 = GregorianChronology.getInstance(DateTimeZone.forID("GMT"));
// Create Ethiopian Date/Time (Y, M, D, H, M, S, mS)

		DateTime dt_eth1 = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(date), 0, 0, 0, 0, chron_eth1);

// Convert to Gregorian Date/Time
		DateTime dt_greg1 = dt_eth1.withChronology(EthiopicChronology.getInstance());
		String d1 = DateTimeFormat.longDate().print(dt_greg1);

		String s1 = d1;
		String[] words = s1.split("\\s");// splits the string based on whitespace

// using java foreach loop to print elements of string array
		int i = 0;
		for (String w : words) {
			if (i == 0) {
				dateObj.setDate(w);
			}
			if (i == 2) {
				dateObj.setYear(w);
			}
			if (i == 1) {
				dateObj.setMonth(w);
			}

			i++;
		}
//		System.out.println(dateObj.getDate() + "/" + dateObj.getMonth() + "/" + dateObj.getYear());

		dateObj.setEatDate(dateObj.getMonth() + "-" + dateObj.getDate()  + "-" + dateObj.getYear());

//		System.out.println("Gregorian Date new: " + dateObj.toString());

		if (dateObj != null) {
			return new ResponseEntity<>(dateObj, HttpStatus.OK);
		} else {
			return new ResponseEntity(null, HttpStatus.BAD_GATEWAY);
		}
	}
}

