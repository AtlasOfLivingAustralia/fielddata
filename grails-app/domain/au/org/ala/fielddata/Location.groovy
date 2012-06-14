package au.org.ala.fielddata

import org.bson.types.ObjectId

class Location {

    ObjectId id
    String userId
    String locality
    Float decimalLatitude
    Float decimalLongitude
    Date dateCreated

    static constraints = {}
}
