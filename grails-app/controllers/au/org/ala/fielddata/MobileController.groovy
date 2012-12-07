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
        log.debug("Mobile - submitRecordMultipart POST received...")
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
            log.debug "Added record: " + record.id.toString()
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
        log.debug("Mobile - submitRecord POST received...")
        try {
            def recordParams = constructRecordParams(request, params)
            Record record = recordService.createRecord(recordParams)
            //handle the base64 encoded image if supplied.....
            if(params.imageBase64 && params.imageFileName){
                byte[] imageAsBytes = Base64.decodeBase64(params.imageBase64)
                recordService.addImageToRecord(record, params.imageFileName, imageAsBytes)
            }
            log.debug "submitRecord POST - added record: " + record.id.toString()
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
        log.debug("Debug params....")
        params.each { log.debug("Received params: " + it) }
        def userName = params.userName  //this will be an email address
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
        recordParams
    }
}
