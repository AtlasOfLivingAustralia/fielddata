package au.org.ala.fielddata

import org.apache.commons.lang.time.DateUtils

class ImportService {

    def mediaService

    def serviceMethod() {}

    def loadFile(filePath){
        def columns = []
        println "Starting import of data....."
        String[] dateFormats = ["yyyy-MM-dd hh:mm:ss.s"]

        def count = 0
        def imported = 0

        new File(filePath).eachCsvLine {
            count += 1
         //   println "Starting....." + count
            if(count == 1){
                columns = it
            } else {
                Record r = new Record()
                it.eachWithIndex { column, idx ->
                    //println("Field debug : " + columns[idx] + " : " + column)
                    if(column != null && column != "") {
                        if(columns[idx] == "eventDate"){
                            r[columns[idx]] = DateUtils.parseDate(column, dateFormats)
                        } else {
                            r[columns[idx]] = column
                        }
                    }
                }
                r = r.save(flush: true)
                imported ++
                log.info("Importing record: " + r.id + ", count: " + count + ", imported: " + imported + ", skipped: " + (count-imported))

                def mapOfProperties = r.dbo.toMap()
             //   mapOfProperties.each { println "MoP:" + it}
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
        println "Total loaded: " + count
    }
}
