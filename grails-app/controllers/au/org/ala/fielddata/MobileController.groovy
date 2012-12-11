package au.org.ala.fielddata

import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair

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
        log.debug("Mobile - submitRecordMultipart POST received...userName:" + params.userName + ", authKey:" + params.authenticationKey)
        def authenticated = checkAuthenticationKey(params.userName, params.authenticationKey)
        log.debug("Mobile userName:" + params.userName + ", authKey:" +params.authenticationKey +", authenticated: " + authenticated)
        if (authenticated){
            try {
                log.debug("Mobile parsing parameters...")
                def recordParams = constructRecordParams(params)
                if (recordParams.record){
                    Record record = recordService.createRecord(recordParams.record)
                    log.debug("Mobile - record created: " + record?.id?.toString())
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
                } else {
                    response.setContentType("application/json")
                    response.sendError(400, recordParams.error)
                    [success:false]
                }
            } catch (Exception e){
                response.setStatus(500)
                response.setContentType("application/json")
                [success:false]
            }
        } else {
            response.setStatus(403)
            response.setContentType("application/json")
            [success:false]
        }
    }

    def submitRecord(){
        log.debug("Mobile - submitRecord POST received...userName:" + params.userName + ", authKey:" + params.authenticationKey)
        def authenticated = checkAuthenticationKey(params.userName, params.authenticationKey)
        log.debug("Mobile userName:" + params.userName + ", authKey:" +params.authenticationKey +", authenticated: " + authenticated)
        if (authenticated){
            try {
                log.debug("Mobile parsing parameters...")
                def recordParams = constructRecordParams(params)
                if (recordParams.record){
                    Record record = recordService.createRecord(recordParams.record)
                    log.debug("Mobile - record created: " + record?.id?.toString())
                    //handle the base64 encoded image if supplied.....
                    if(params.imageBase64 && params.imageFileName){
                        byte[] imageAsBytes = Base64.decodeBase64(params.imageBase64)
                        recordService.addImageToRecord(record, params.imageFileName, imageAsBytes)
                    }
                    log.debug "submitRecord POST - added record: " + record.id.toString()
                    response.setStatus(200)
                    response.setContentType("application/json")
                    [success:true, recordId:record.id.toString()]
                } else {
                    response.setContentType("application/json")
                    response.sendError(400, recordParams.error)
                    [success:false]
                }
            } catch (Exception e){
                response.setStatus(500)
                response.setContentType("application/json")
                [success:false]
            }
        } else {
            response.setStatus(403)
            response.setContentType("application/json")
            [success:false]
        }
    }

    private def boolean checkAuthenticationKey(String userName, String authKey) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost("https://m.ala.org.au/mobileauth/mobileKey/checkKey");
        def nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("userName", userName))
        nameValuePairs.add(new BasicNameValuePair("authKey", authKey))
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse httpResponse = httpClient.execute(post)
        log.debug("Authentication check:" + httpResponse.getStatusLine().getStatusCode() + ", for username: " + userName + ", and auth key " + authKey)
        httpResponse.getStatusLine().getStatusCode() == 200
    }

    private def constructRecordParams(params){
        log.debug("Debug params....")
        params.each { log.debug("Received params: " + it) }
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
            Date date = DateUtils.parseDate(dateString, ["yyyy-MM-dd", "yyyy/MM/dd", "dd MMM yyyy", "dd-MM-yyyy", "dd/MM/yyyy", "dd/MM/yy"].toArray(new String[0]));
            dateToUse = dateFormatter.format(date)
        } catch (IllegalArgumentException ex) {
            log.debug("no date supplied: " + dateString)
            return [error:"no date supplied: " + dateString, record:null]
        } catch (Exception ex) {
            log.debug("invalid date format: " + dateString)
            return [error:"invalid date format: " + dateString, record:null]
        }

        //get the user Id....
        log.debug("Retrieving...user ID with user name: " + params.userName)
        def userId = userService.getUserEmailToIdMap().get(params.userName.toLowerCase())
        log.debug("Retrieved user ID: " + userId+ ", for user name: " + params.userName)

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
        [error:null, record: recordParams]
    }
}
