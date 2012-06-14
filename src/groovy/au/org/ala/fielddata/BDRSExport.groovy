package au.org.ala.fielddata
//EXPORT
import groovy.sql.Sql
import au.com.bytecode.opencsv.CSVWriter

sql = Sql.newInstance( 'jdbc:postgresql://localhost:5432/bdrs', 'postgres', 'postgres', 'org.postgresql.Driver' )
file = new FileOutputStream("/tmp/bdrs.txt")
out = new BufferedOutputStream(file)
columns = ["scientificName","associatedMedia","occurrenceID","createdDate","userID","individualCount","decimalLongitude","decimalLatitude","coordinateUncertaintyInMeters","eventTime","eventDate","occurrenceRemarks"]

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

    newRecord = [scientificName,imagePath,"urn:lsid:cs.ala.org.au:Record:"+it.record_id, it.created_at?.toString() ?: "",it.created_by?.toString() ?: "",it.number_seen?.toString() ?: "",longitude,latitude,it.accuracy?.toString() ?: "",it.time?.toString() ?: "",it.when_date?.toString() ?: "", it.notes ?: ""]

    //newRecord.eachWithIndex { field, idx -> println(columns[idx] + ": " + field)}

    //newRecord = [scientificName]
    csvWriter.writeNext(newRecord.toArray(new String[0]))
}

csvWriter.flush()
csvWriter.close()