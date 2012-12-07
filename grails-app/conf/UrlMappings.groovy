class UrlMappings {

	static mappings = {

        //"/record/testJMS"(controller: "record"){ action = [GET:"testJMS"] }
        "/submit/record"(controller: "mobile"){ action = [POST:"submitRecord"] }
        "/submit/recordMultiPart"(controller: "mobile"){ action = [POST:"submitRecordMultipart"] }
        "/mobile/submitRecord"(controller: "mobile"){ action = [POST:"submitRecord"] }
        "/mobile/submitRecordMultipart"(controller: "mobile"){ action = [POST:"submitRecordMultipart"] }
        "/record/"(controller: "record"){ action = [GET:"list", POST:"create"] }
        "/record"(controller: "record"){ action = [GET:"list", POST:"create"] }
        "/record/"(controller: "record"){ action = [GET:"list", POST:"create"] }
        "/record/count"(controller: "record"){ action = [GET:"count"] }
        "/record/user/$userId"(controller: "record", action: "listForUser")
        "/record/sync/all"(controller: "record"){ action = [GET:"resyncAll"] }
        "/record/sync/$id"(controller: "record"){ action = [GET:"resyncRecord"] }
        "/record/images"(controller: "record"){ action = [GET:"listRecordWithImages"] }
        "/record/images/"(controller: "record"){ action = [GET:"listRecordWithImages"] }
        "/record/$id"(controller: "record"){ action = [GET:"getById", PUT:"updateById", DELETE:"deleteById", POST:"updateById"] }
        "/images"(controller: "record"){ action = [GET:"listRecordWithImages"] }
        "/images/"(controller: "record"){ action = [GET:"listRecordWithImages"] }
        "/images/update"(controller: "record"){ action = [POST:"updateImages"] }
        "/location"(controller: "location"){ action = [GET:"list", POST:"create"] }
        "/location/"(controller: "location"){ action = [GET:"list", POST:"create"] }
        "/location/user/$userId"(controller: "location"){ action = [GET:"listForUser", DELETE: "deleteAllForUser"] }
        "/location/$id"(controller: "location"){ action = [GET:"getById", PUT:"updateById", DELETE:"deleteById", POST:"updateById"] }
        "/media/$dir/$fileName"(controller: "media"){ action = [GET:"getImage"] }

        "/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}


