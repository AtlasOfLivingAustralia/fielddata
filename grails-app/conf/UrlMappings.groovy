class UrlMappings {

	static mappings = {

        "/images"(controller: "record"){ action = [GET:"listRecordWithImages"] }
        "/record"(controller: "record"){ action = [GET:"list", POST:"create"] }
        "/record/"(controller: "record"){ action = [GET:"list", POST:"create"] }
        "/record/user/$userId"(controller: "record", action: "listForUser")
        "/record/$id"(controller: "record"){ action = [GET:"getById", PUT:"updateById", DELETE:"deleteById", POST:"updateById"] }

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


