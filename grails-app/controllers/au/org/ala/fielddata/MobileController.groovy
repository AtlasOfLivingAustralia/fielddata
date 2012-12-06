package au.org.ala.fielddata

import java.text.ParseException
import org.springframework.web.multipart.MultipartFile
import java.text.SimpleDateFormat
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.NameValuePair
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.commons.lang.time.DateUtils
import grails.converters.JSON
import org.apache.http.HttpResponse
import org.apache.commons.codec.binary.Base64
import org.springframework.web.multipart.MultipartHttpServletRequest

class MobileController {

    def index() { }

    def recordService

    def userService

    def submitRecordMultipart(){
        println("Mobile - submitRecordMultipart POST received...")
        try {
            def recordParams = constructRecordParams(request, params)
            Record record = recordService.createRecord(recordParams)
            //handle the multipart message.....
            if(request instanceof MultipartHttpServletRequest){
                Map<String, MultipartFile> fileMap = request.getFileMap()
                if (fileMap.containsKey("attribute_file_1")) {
                    MultipartFile multipartFile = fileMap.get("attribute_file_1")
                    byte[] imageAsBytes = multipartFile.getBytes()
                    String originalName = multipartFile.getOriginalFilename()
                    recordService.addImageToRecord(record, originalName, imageAsBytes)
                }
            }
            println "Added record: " + record.id.toString()
            response.setStatus(200)
            response.setContentType("application/json")
            [success:true, recordId:record.id.toString()]
        } catch (Exception e){
            response.setStatus(500)
            response.setContentType("application/json")
            [success:false]
        }
    }

    def submitRecord(){
        println("Mobile - submitRecord POST received...")
        try {
            def recordParams = constructRecordParams(request, params)
            Record record = recordService.createRecord(recordParams)
            //handle the base64 encoded image if supplied.....
            if(params.imageBase64 && params.imageFileName){
                byte[] imageAsBytes = Base64.decodeBase64(params.imageBase64)
                recordService.addImageToRecord(record, params.imageFileName, imageAsBytes)
            }
            println "submitRecord POST - added record: " + record.id.toString()
            response.setStatus(200)
            response.setContentType("application/json")
            [success:true, recordId:record.id.toString()]
        } catch (Exception e){
            response.setStatus(500)
            response.setContentType("application/json")
            [success:false]
        }
    }

    private def boolean checkAuthenticationKey(String userName, String authenticationKey) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost("https://m.ala.org.au/mobileauth/mobileKey/checkKey");
        post.params.setParameter("userName", userName)
        post.params.setParameter("authKey", authenticationKey)
        HttpResponse httpResponse = httpClient.execute(post);
        httpResponse.getStatusLine().getStatusCode() == 200;
    }

    private def constructRecordParams(request, params){
        def userName = params.user  //this will be an email address
        def authenticationKey = params.authenticationKey
        def dateString = params.date
        def time = params.time
        def taxonId = params.taxonID
        def taxonName = params.survey_species_search
        def number = params.number
        def accuracyInMeters = params.accuracyInMeters
        def coordinatePrecision = params.coordinatePrecision
        def imageLicence = params.imageLicence

        log.debug("Multipart record request submission received.");

        // Convert date to desired format
        def dateToUse = null
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = DateUtils.parseDate(dateString, ["yyyy-MM-dd", "yyyy/MM/dd", "dd MMM yyyy", "dd-MM-yyyy", "dd/MM/yyyy"].toArray(new String[0]));
            dateToUse = dateFormatter.format(date)
        } catch (IllegalArgumentException ex) {
            log.debug("no date supplied");
            response.sendError(400, "no date supplied");
            return;
        } catch (ParseException ex) {
            log.debug("invalid date format");
            response.sendError(400, "invalid date format");
            return;
        }

        // First, check that we have a valid user / authentication key
        // combination
        boolean authKeyValid = checkAuthenticationKey(userName, authenticationKey);
        log.debug("Authentication key is valid: " + authKeyValid)

        //get the user Id....
        def userId = userService.getUserEmailToIdMap().get(userName.toLowerCase())
        log.debug("Retrieved user ID: " + userId+ ", for user name: " + userName)

        //save the files
        def recordParams = [
               userId:userId,
               eventDate:dateToUse,
               eventTime:time,
               taxonConceptID:taxonId,
               scientificName:taxonName,
               family:params.family,
               kingdom:params.kingdom,
               decimalLongitude:params.longitude,
               decimalLatitude:params.latitude,
               individualCount:number,
               coordinateUncertaintyInMeters:accuracyInMeters,
               coordinatePrecision:coordinatePrecision,
               imageLicence:imageLicence,
               commonName:params.commonName,
               locality:params.locationName,
               device:params.deviceName,
               devicePlatform:params.devicePlatform,
               deviceId: params.deviceId,
               occurrenceRemarks:params.notes
        ]

        log.debug((recordParams as JSON).toString(true))
    }




//    private def doSubmitRecord(String userName,
//                               String authenticationKey,
//                               String taxonID,
//                               String taxonName,
//                               double coordinatePrecision,
//                               String imageLicence,
//                               Map<String, String[]> parameterMap,
//                               Map<String, MultipartFile> fileMap,
//                               request,
//                               response) throws ParseException, IOException {
//
//        if (!fileMap.isEmpty()) {
//            for (String key : fileMap.keySet()) {
//                MultipartFile multipartFile = fileMap.get(key);
//                log.debug(String.format("Request contains file: %s (%s)", multipartFile.getOriginalFilename(), key));
//            }
//        }
//
//        // First, check that we have a valid user / authentication key
//        // combination
//        boolean authKeyValid = checkAuthenticationKey(userName, authenticationKey);
//
//        try {
//            log.debug("Checking authentication key");
//
//            log.debug("Authentication key check was successful");
//            if (authKeyValid) {
//                try {
//                    log.debug("Looking up user");
//                    User user = userDAO.getUser(userName);
//                    if (user == null) {
//                        try {
//                            user = createUser(userName);
//                            if (user == null) {
//                                response.sendError(500, "Error creating user in local database for username " + userName);
//                            }
//                        } catch (Exception ex) {
//                            log.debug("Exception occurred while creating user in local database for username " + userName, ex);
//                            response.sendError(500, "Error creating user in local database for username " + userName);
//                        }
//                    }
//
//                    log.debug("Looking up taxon");
//                    // Import the taxon from the ALA biocache if it is not
//                    // already
//                    // present in the BDRS database.
//                    IndicatorSpecies species = taxaDAO.getIndicatorSpeciesByGuid(taxonID);
//                    if (species == null) {
//                        log.debug("Taxon does not exist in local BDRS database - importing taxon definition from the biocache");
//                        Map<String, String> errorMap = (Map<String, String>) getRequestContext().getSessionAttribute("errorMap");
//                        species = atlasService.importSpecies(taxonID, true, errorMap, null);
//                    }
//
//                    if (species == null) {
//                        response.sendError(400, "could not find taxon " + taxonID);
//                        return;
//                    }
//
//                    // Add the species id to the parameter map - want this to be used when creating the record rather than
//                    // doing a name search.
//                    parameterMap.put(_recordKeyLookup.getSpeciesIdKey(), new String[] { Integer.toString(species.getId()) });
//
//                    // Remove authentication key, taxon LSID and username from the parameter map before delegating to the record
//                    // creating code - these keys are not recognised by the record creation code, remove them here to avoid any
//                    // confusion.
//                    parameterMap.remove(AUTHENTICATION_KEY_PARAMETER);
//                    parameterMap.remove(TAXON_LSID_PARAMETER);
//                    parameterMap.remove(USER_PARAMETER);
//
//                    log.debug("Submitting record");
//
//                    TrackerFormToRecordEntryTransformer transformer = new TrackerFormToRecordEntryTransformer(locationService);
//                    TrackerFormAttributeDictionaryFactory adf = new TrackerFormAttributeDictionaryFactory();
//                    AttributeParser parser = new WebFormAttributeParser();
//
//                    RecordDeserializer rds = new RecordDeserializer(_recordKeyLookup, adf, parser);
//                    List<RecordEntry> entries = transformer.httpRequestParamToRecordMap(parameterMap, fileMap);
//                    List<RecordDeserializerResult> results = rds.deserialize(user, entries);
//
//                    // there should be exactly 1 result since we are only
//                    // putting in 1
//                    // RecordEntry...
//                    if (results.size() != 1) {
//                        log.warn("Expecting only 1 deserialization result but got: " + results.size());
//                    }
//                    RecordDeserializerResult res = results.get(0);
//
//                    if (!res.getErrorMap().isEmpty()) {
//                        log.error(String.format("Invalid record data: %s", StringUtils.join(res.getErrorMap().values(), ", ")));
//                        response.sendError(400, StringUtils.join(res.getErrorMap().values(), ", "));
//                        return;
//                    }
//
//                    if (!res.isAuthorizedAccess()) {
//                        // Required since there will be an auto commit otherwise
//                        // at the
//                        // end of controller handling.
//                        requestRollback(request);
//
//                        throw new AccessDeniedException(RecordWebFormContext.MSG_CODE_EDIT_AUTHFAIL);
//                    }
//
//                    // Write additional record metadata
//                    Record rec = res.getRecord();
//
//                    log.debug("Adding coordinate precision to record");
//                    Metadata md1 = recordDAO.getRecordMetadataForKey(rec, RECORD_COORDINATE_PRECISION_KEY);
//                    md1.setValue(Double.toString(coordindatePrecision));
//                    md1 = metadataDAO.save(md1);
//                    rec.getMetadata().add(md1);
//
//                    if (imageLicence != null) {
//                        log.debug("Adding image licence to record");
//                        Metadata md2 = recordDAO.getRecordMetadataForKey(rec, RECORD_IMAGE_LICENCE_KEY);
//                        md2.setValue(imageLicence);
//                        md2 = metadataDAO.save(md2);
//                        rec.getMetadata().add(md2);
//                    }
//
//                    recordDAO.saveRecord(rec);
//
//                    log.debug("Record submitted successfully");
//                } catch (Exception ex) {
//                    log.error("Error occurred while submitting record", ex);
//                    response.sendError(500);
//                }
//            } else {
//                log.debug("Authentication key check failed");
//                response.sendError(403);
//            }
//        } catch (Exception ex) {
//            log.error("Error occurred while checking authentication key", ex);
//            response.sendError(500);
//        }
//    }


//   // @RequestMapping(value = "/webservice/submitRecord.htm", method = RequestMethod.POST)
//    public void submitRecordOrdinaryPost(@RequestParam(value = USER_PARAMETER, required = true) String userName,
//            @RequestParam(value = AUTHENTICATION_KEY_PARAMETER, required = true) String authenticationKey,
//            @RequestParam(value = "surveyId", required = false, defaultValue = "1") String surveyId,
//            @RequestParam(value = "latitude", required = false) String latitude,
//            @RequestParam(value = "longitude", required = false) String longitude,
//            @RequestParam(value = "date", required = true) String date,
//            @RequestParam(value = "time", required = true) String time,
//            @RequestParam(value = TAXON_LSID_PARAMETER, required = true) String taxonID,
//            @RequestParam(value = "survey_species_search", required = true) String taxonName,
//            @RequestParam(value = "number", required = true) String number,
//            @RequestParam(value = "notes", required = false) String notes,
//            @RequestParam(value = "locationName", required = false) String locationName,
//            @RequestParam(value = "accuracyInMeters", required = true) String accuracyInMeters,
//            @RequestParam(value = "coordinatePrecision", required = true) double coordindatePrecision,
//            @RequestParam(value = "imageLicence", required = false) String imageLicence,
//            @RequestParam(value = "imageBase64", required = false) String imageBase64,
//            @RequestParam(value = "imageFileName", required = false) final String imageFileName,
//            @RequestParam(value = "imageContentType", required = false) final String imageContentType,
//            HttpServletRequest request, HttpServletResponse response) throws ParseException, IOException {
//
//        log.debug("Ordinary record request submission received");
//
//        // usernames must be converted to lowercase for use with BDRS database -
//        // OK to do this because the ALA authentication service
//        // ignores case
//        userName = userName.toLowerCase();
//
//        Map<String, String[]> parameterMap = new HashMap<String, String[]>();
//        parameterMap.put(_recordKeyLookup.getSurveyIdKey(), new String[] { surveyId });
//
//        if (latitude != null) {
//            parameterMap.put(_recordKeyLookup.getLatitudeKey(), new String[] { latitude });
//        }
//
//        if (longitude != null) {
//            parameterMap.put(_recordKeyLookup.getLongitudeKey(), new String[] { longitude });
//        }
//
//        parameterMap.put(_recordKeyLookup.getDateKey(), new String[] { date });
//        parameterMap.put(_recordKeyLookup.getTimeKey(), new String[] { time });
//        parameterMap.put(_recordKeyLookup.getSpeciesNameKey(), new String[] { taxonName });
//        parameterMap.put(_recordKeyLookup.getIndividualCountKey(), new String[] { number });
//
//        if (notes != null) {
//            parameterMap.put(_recordKeyLookup.getNotesKey(), new String[] { notes });
//        }
//
//        if (locationName != null) {
//            parameterMap.put(_recordKeyLookup.getLocationNameKey(), new String[] { locationName });
//        }
//
//        parameterMap.put(_recordKeyLookup.getAccuracyKey(), new String[] { accuracyInMeters });
//
//        // empty string parameter "attribute_2" is expected when a file
//        // (attribute_file_2) is submitted, not sure why.
//        parameterMap.put(FILE_ATTRIBUTE_PARAMETER, new String[] { "" });
//
//        // Convert date to desired format
//        try {
//            convertDateFormat(parameterMap);
//        } catch (IllegalArgumentException ex) {
//            log.debug("no date supplied");
//            response.sendError(400, "no date supplied");
//            return;
//        } catch (ParseException ex) {
//            log.debug("invalid date format");
//            response.sendError(400, "invalid date format");
//            return;
//        }
//
//        Map<String, MultipartFile> fileMap = new HashMap<String, MultipartFile>();
//
//        if (!(imageBase64 != null && imageFileName != null && imageContentType != null || imageBase64 == null && imageFileName == null && imageContentType == null)) {
//            log.debug("image data, file name or content type missing - all three must be provided");
//            response.sendError(400, "to submit and image, the image data and file name must both be provided");
//            return;
//        }
//
//        // Wrap the received image data in the MultiPartFile interface - the
//        // BDRS core only handles images in this form.
//        if (imageBase64 != null) {
//            log.debug("Decoding base64 image data");
//            final byte[] imageData = Base64.decodeBase64(imageBase64);
//
//            MultipartFile multipartFile = new MultipartFile() {
//
//                @Override
//                public byte[] getBytes() throws IOException {
//                    return imageData;
//                }
//
//                @Override
//                public String getContentType() {
//                    return imageContentType;
//                }
//
//                @Override
//                public InputStream getInputStream() throws IOException {
//                    return new ByteArrayInputStream(imageData);
//                }
//
//                @Override
//                public String getName() {
//                    return FILE_ATTRIBUTE_FILE_NAME_PARAMETER;
//                }
//
//                @Override
//                public String getOriginalFilename() {
//                    return imageFileName;
//                }
//
//                @Override
//                public long getSize() {
//                    return imageData.length;
//                }
//
//                @Override
//                public boolean isEmpty() {
//                    return imageData.length == 0;
//                }
//
//                @Override
//                public void transferTo(File arg0) throws IOException, IllegalStateException {
//                    throw new NotImplementedException();
//                }
//            };
//
//            fileMap = new HashMap<String, MultipartFile>();
//            fileMap.put(FILE_ATTRIBUTE_FILE_NAME_PARAMETER, multipartFile);
//
//        }
//
//        doSubmitRecord(userName, authenticationKey, taxonID, taxonName, coordindatePrecision, imageLicence, parameterMap, fileMap, request, response);
//    }


}
