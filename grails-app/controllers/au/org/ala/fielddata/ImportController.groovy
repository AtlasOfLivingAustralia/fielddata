package au.org.ala.fielddata

class ImportController {

    def mediaService



    def index() { }

    def importFile(){

        def columns = []

        println "Starting....."
        def count = 0

        new File(params.filePath).eachCsvLine {
            count += 1
            if(count == 1){
                columns = it
            } else {
                Record r = new Record()
                it.eachWithIndex { column, idx ->
                    println("Field debug : " + columns[idx] + " : " + column)
                    if(column != null && column != "") {
                        r[columns[idx]] = column
                    }
                }
                r = r.save(flush: true)
                println(r.id)

                //r = Record.get(r.id)

                def mapOfProperties = r.dbo.toMap()
                mapOfProperties.each { println "MoP:" + it}
                if(mapOfProperties.get("associatedMedia")){
                    def associatedMediaPath = r.getProperty("dbo")?.toMap().get("associatedMedia")
                    def mediaFile = mediaService.copyToImageDir(r.id.toString(), associatedMediaPath)
                    if(mediaFile != null){
                        r['associatedMedia'] = mediaFile.getAbsolutePath()
                        r.save(flush:true)
                    } else {
                        println "Unable to import media for path: " +  associatedMediaPath
                        r['associatedMedia'] = null
                        r.save(flush:true)
                    }
                }
            }
            //println it[0]
        }

        println "Starting....."
    }
}
