package au.org.ala.fielddata

import org.apache.commons.lang.time.DateUtils
import java.text.SimpleDateFormat

class ImportService {

    def mediaService

    def serviceMethod() {}

    def loadFile(filePath){
        def columns = []
        println "Starting import of data....."
        String[] dateFormats = ["yyyy-MM-dd hh:mm:ss.s"]

        def count = 0
        def imported = 0
        def indexOfOccurrenceID = -1


        new File(filePath).eachCsvLine {
            count += 1
            println "Starting....." + count
            if(count == 1){
                columns = it
                columns.eachWithIndex { obj, i ->
                    if(obj == "occurrenceID")
                       indexOfOccurrenceID = i
                }
            } else {

                def preloaded = false
                Record r = null
                if(indexOfOccurrenceID >=0){
                    //is record already loaded ?
                    r = Record.findWhere([occurrenceID:it[indexOfOccurrenceID]])
                    preloaded = r != null
                }

                if(!r)
                    r = new Record()

                it.eachWithIndex { column, idx ->
                    println("Field debug : " + columns[idx] + " : " + column)
                    if(column != null && column != "") {
                        if(columns[idx] == "eventDate" && column){
                            try {
                                def suppliedDate = DateUtils.parseDate(column, dateFormats)
                                SimpleDateFormat yyymmdd = new SimpleDateFormat("yyyy-MM-dd")
                                SimpleDateFormat hhmm = new SimpleDateFormat("HH:mm")
                                r[columns[idx]] = yyymmdd.format(suppliedDate)
                                r[columns["eventTime"]] = hhmm.format(suppliedDate)
                            } catch (Exception e) {}
                        } else if(columns[idx] == "decimalLatitude" && column && column != "null"){
                            r[columns[idx]] = Float.parseFloat(column)
                        } else if(columns[idx] == "decimalLongitude" && column && column != "null"){
                            r[columns[idx]] = Float.parseFloat(column)
                        } else {
                            r[columns[idx]] = column
                        }
                    }
                }
                r = r.save(flush: true)

                imported ++
                log.info("Importing record: " + r.id + ", count: " + count + ", imported: " + imported + ", skipped: " + (count-imported))

                if(!preloaded){
                    def mapOfProperties = r.dbo.toMap()
                    if(mapOfProperties.get("associatedMedia")){
                        def associatedMediaPath = r.getProperty("dbo")?.toMap().get("associatedMedia")
                        def mediaFile = mediaService.copyToImageDir(r.id.toString(), associatedMediaPath)
                        if(mediaFile != null){
                            r['associatedMedia'] = mediaFile.getAbsolutePath()
                            r.save(flush:true)
                        } else {
                            log.error "Unable to import media for path: " +  associatedMediaPath
                            r['associatedMedia'] = null
                            r.save(flush:true)
                        }
                    }
                }
            }
        }
        println "Total loaded: " + count
    }
}
