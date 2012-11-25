package au.org.ala.fielddata

import org.bson.types.ObjectId

class Record {

    static mapping = { version false }

    ObjectId id
    Date eventDate
    String decimalLatitude
    String decimalLongitude
    String userId
    //List associatedMedia
    Date dateCreated
    Date lastUpdated

    static constraints = {
        eventDate nullable:true
        decimalLatitude nullable:true
        decimalLongitude nullable:true
        userId nullable: true
        //associatedMedia nullable: true
    }
}
