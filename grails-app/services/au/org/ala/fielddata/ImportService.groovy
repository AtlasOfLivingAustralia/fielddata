package au.org.ala.fielddata

import org.apache.commons.lang.time.DateUtils
import java.text.SimpleDateFormat

class ImportService {

    def mediaService

    def serviceMethod() {}

    def loadFile(filePath){
        def columns = []
        println "Starting import of data....."
        String[] dateFormats = ["yyyy-MM-dd HH:mm:ss.s"]

        def count = 0
        def imported = 0
        def indexOfOccurrenceID = -1
        def associatedMediaIdx = -1


        new File(filePath).eachCsvLine {
            count += 1
            println "Starting....." + count
            if(count == 1){
                columns = it
                columns.eachWithIndex { obj, i ->
                    if(obj == "occurrenceID")
                       indexOfOccurrenceID = i
                    if(obj == "associatedMedia")
                        associatedMediaIdx = i
                }
            } else {

                def preloaded = false
                Record r = null
                if(indexOfOccurrenceID >=0){
                    //is record already loaded ?
                    r = Record.findWhere([occurrenceID:it[indexOfOccurrenceID]])
                    preloaded = (r != null)
                }

                if(!r){
                    r = new Record()
                }

                it.eachWithIndex { column, idx ->
                    println("Field debug : " + columns[idx] + " : " + column)
                    if(column != null && column != "" && column != "associatedMedia" && column != "eventTime") {
                        if(columns[idx] == "eventDate" && column){
                            try {
                                def suppliedDate = DateUtils.parseDate(column, dateFormats)
                                SimpleDateFormat yyymmdd = new SimpleDateFormat("yyyy-MM-dd")
                                SimpleDateFormat hhmm = new SimpleDateFormat("HH:mm")
                                r[columns[idx]] = yyymmdd.format(suppliedDate)
                                def eventTimeFormatted = hhmm.format(suppliedDate)
                                println eventTimeFormatted
                                r[columns["eventTime"]] = eventTimeFormatted
                            } catch (Exception e) {
                                e.printStackTrace()
                            }
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
                    if(associatedMediaIdx>=0 && it[associatedMediaIdx]){
                        if(it[associatedMediaIdx].endsWith("C:fakepathIMG_20120208_154135.jpg")){
                            println "bad image: " + it[associatedMediaIdx]
                        }
                        try {
                            def mediaFile = mediaService.copyToImageDir(r.id.toString(), it[associatedMediaIdx])
                            println "Media filepath: " + mediaFile.getPath()
                            if(mediaFile){
                                r['associatedMedia'] = mediaFile.getPath()
                                r.save(flush:true)
                            } else {
                                println "Unable to import media for path: " +  it[associatedMediaIdx]
                            }
                        } catch(Exception e){
                            e.printStackTrace()
                            println("Error loading images.")
                            r['associatedMedia'] = "[]"
                            r.save(flush:true)
                        }
                    }
                }
            }
        }
        println "Total loaded: " + count
    }
}
