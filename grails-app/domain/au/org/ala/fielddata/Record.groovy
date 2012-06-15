package au.org.ala.fielddata

import org.bson.types.ObjectId

class Record {

    ObjectId id
    String eventDate
    String decimalLatitude
    String decimalLongitude
    String userID
    //List associatedMedia
    Date dateCreated
    Date lastUpdated

    static constraints = {
        eventDate nullable:true
        decimalLatitude nullable:true
        decimalLongitude nullable:true
        userID nullable: true
        //associatedMedia nullable: true
    }
}
