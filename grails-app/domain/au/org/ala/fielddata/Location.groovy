package au.org.ala.fielddata

import org.bson.types.ObjectId

class Location {

    static mapping = { version false }

    ObjectId id
    String userId
    String locality
    String decimalLatitude
    String decimalLongitude
    String geodeticDatum
    Date dateCreated

    static constraints = {
        geodeticDatum nullable:true
    }
}
