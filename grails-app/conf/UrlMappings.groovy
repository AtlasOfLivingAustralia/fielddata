class UrlMappings {

	static mappings = {

        //"/record/testJMS"(controller: "record"){ action = [GET:"testJMS"] }
        "/record"(controller: "record"){ action = [GET:"list", POST:"create"] }
        "/record/"(controller: "record"){ action = [GET:"list", POST:"create"] }
        "/record/count"(controller: "record"){ action = [GET:"count"] }
        "/record/user/$userId"(controller: "record", action: "listForUser")
        "/record/sync/all"(controller: "record"){ action = [GET:"resyncAll"] }
        "/record/sync/$id"(controller: "record"){ action = [GET:"resyncRecord"] }
        "/record/$id"(controller: "record"){ action = [GET:"getById", PUT:"updateById", DELETE:"deleteById", POST:"updateById"] }
        "/images/update"(controller: "record"){ action = [POST:"updateImages"] }
        "/images/"(controller: "record"){ action = [GET:"listRecordWithImages"] }
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


