//package au.org.ala.fielddata

/*

Script for exporting BDRS data
mkdir -p /data/bdrs/filestore/au/com/gaiaresources/bdrs/model/taxa/AttributeValue/
scp -r mar759@cs.ala.org.au:/data/bdrs/filestore/au/com/gaiaresources/bdrs/model/taxa/AttributeValue/* /data/bdrs/filestore/au/com/gaiaresources/bdrs/model/taxa/AttributeValue/
scp -r /data/bdrs/filestore/au/com/gaiaresources/bdrs/model/taxa/AttributeValue/* mar759@sightings.ala.org.au:/data/bdrs/filestore/au/com/gaiaresources/bdrs/model/taxa/AttributeValue/


/data/bdrs/filestore/au/com/gaiaresources/bdrs/model/taxa/AttributeValue/


db.record.update(
    {'userEmail':'robyn.lawrence@csiro.au'},
    {
        $set:{'userId' : '45','userEmail' : 'robyn.lawrence@environment.gov.au'}
    }, false, true
);
db.record.update(
    {'userEmail':'donald.hobern@csiro.au'},
    {
        $set:{'userId' : '1664','userEmail' : 'dhobern@gmail.com'}
    }, false, true
);

 */



//EXPORT
import groovy.sql.Sql
import au.com.bytecode.opencsv.CSVWriter
import au.com.bytecode.opencsv.CSVWriter
import groovy.json.JsonSlurper
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient

//lookup CAS IDs
casUrl = "http://auth.ala.org.au/userdetails/userDetails/getUserListFull"
httpclient = new DefaultHttpClient();
post = new HttpPost(casUrl)
response = httpclient.execute(post)
content = response.getEntity().getContent()
jsonSlurper = new JsonSlurper()
json = jsonSlurper.parse(new InputStreamReader(content))
idMap = [:]
json.each { idMap.put(it.email.toLowerCase(), it.id.toString()) }
println("My ID is: " + idMap["david.martin@csiro.au"])
httpclient.getConnectionManager().shutdown()

//stragglers
idMap.put "donald.hobern@csiro.au", 1664
idMap.put "robyn.lawrence@csiro.au", 45

sql = Sql.newInstance( 'jdbc:postgresql://localhost:5432/bdrs', 'postgres', 'postgres', 'org.postgresql.Driver' )
file = new FileOutputStream("/tmp/bdrs.txt")
out = new BufferedOutputStream(file)
columns = ["scientificName","associatedMedia","occurrenceID","createdDate","userId","userEmail","individualCount","decimalLongitude","decimalLatitude","coordinateUncertaintyInMeters","eventTime","eventDate","occurrenceRemarks"]


//add headers
csvWriter = new CSVWriter(new PrintWriter(out))
csvWriter.writeNext(columns.toArray(new String[0]))

sql.eachRow('select * from record' ) {

    speciesRow = sql.firstRow("select scientific_name from indicator_species where indicator_species_id = ${it.indicator_species_id}")

    location = sql.firstRow("select ST_X(location) as longitude, ST_Y(location) as latitude from location where location_id = ${it.location_id}")

    latitude = location?.latitude.toString() ?: ''
    longitude = location?.longitude.toString() ?: ''

    imageRow = sql.firstRow("select av.attribute_value_id, av.string_value from record_attribute_value rav inner join attribute_value av on rav.attributes_attribute_value_id=av.attribute_value_id where record_record_id= ${it.record_id}")

    imagePath =  imageRow ? '/data/bdrs/filestore/au/com/gaiaresources/bdrs/model/taxa/AttributeValue/' + imageRow.attribute_value_id + "/" + imageRow.string_value : ""

    scientificName = speciesRow?.scientific_name ?: ""

    //it.created_by?.toString() ?: ""
    bdrsID = it.indicator_user_id ?: -99999
    userEmail = ""
    userID = ""
    if(bdrsID != ""){
        userEmail = sql.firstRow("select email_address from user_definition where user_definition_id = ${bdrsID}")?.get("email_address").toString()
        if(userEmail){
            userID = idMap.get(userEmail)?:"".toString()
        }
    }

    if(userEmail =="null") userEmail = ""
    if(userID =="null") userID = ""

    println("userID : " + userID + ", userEmail:" + userEmail)

    newRecord = [scientificName,imagePath,"urn:lsid:cs.ala.org.au:Record:"+it.record_id, it.created_at?.toString() ?: "",userID,userEmail,it.number_seen?.toString() ?: "",longitude,latitude,it.accuracy?.toString() ?: "",it.time?.toString() ?: "",it.when_date?.toString() ?: "", it.notes ?: ""]

    //newRecord.eachWithIndex { field, idx -> println(columns[idx] + ": " + field)}

    //newRecord = [scientificName]
    csvWriter.writeNext(newRecord.toArray(new String[0]))
}

csvWriter.flush()
csvWriter.close()