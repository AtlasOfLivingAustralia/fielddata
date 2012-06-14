package au.org.ala.fielddata
//package fielddata
//
//import grails.converters.JSON
//import java.net.URLConnection
//import java.net.URL
//
//def testCreateWithPOST(){
//    def postBody = """{
//        "eventDate":"2001-01-01",
//        "decimalLatitude":13.2,
//        "decimalLongitude":143.2,
//        "userId":"david.martin@csiro.au"
//    }"""
//    def result = doPost("http://localhost", "/fielddata/record/createWithPOST", 8080, postBody)
//    assert result.status == 200
//}
//
//def testCreateWithPOST2(){
//    def postBody = """{
//        "eventDate":"2001-01-01",
//        "decimalLatitude":13.2,
//        "decimalLongitude":143.2,
//        "userId":"david.martin@csiro.au",
//        "scientificName":"Aus bus",
//        "commonName":"Aussie",
//        "collector":"Aussie"
//    }"""
//    def result = doPost("http://localhost", "/fielddata/record/createWithPOST", 8080, postBody)
//    assert result.status == 200
//}
//
//
//testCreateWithPOST()
//testCreateWithPOST2()
//
//
////create a record
////list records
////update the record
////delete the record
//
//def doPost(String url, String path, Integer port, String postBody) {
//    def portBit = ":" + port ? ":" + port : ""
//    def conn = (new URL(url + portBit + path)).openConnection()
//    try {
//        conn.setDoOutput(true)
//        conn.setRequestProperty("Content-Type", "application/json");
//        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream())
//        wr.write(postBody)
//        wr.flush()
//        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//        String line;
//        def resp = ""
//        while ((line = rd.readLine()) != null) {
//            resp += line
//        }
//        rd.close()
//        wr.close()
//        return [error: null, resp: JSON.parse(resp), status: 200, conn.getHeaderField("Content-Location")]
//    } catch (SocketTimeoutException e) {
//        def error = [error: "Timed out calling web service. URL= ${url}."]
//        println error.error
//        return error as JSON
//    } catch (Exception e) {
//        def error = [error: "Failed calling web service. ${e.getClass()} ${e.getMessage()} ${e} URL= ${url}."]
//        println error.error
//        return error as JSON
//    }
//}