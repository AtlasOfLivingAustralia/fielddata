<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="ala2" />
    <title>Fielddata web services | Atlas of Living Australia</title>
</head>
<body>
<div id="content">
    <style type="text/css">
    .code { font-family: courier new;}
    .webserviceList { margin-left:30px; }
    .paramList { margin-left:60px; }
    .paramOptionsList { margin-left:90px; }
    .exampleResponse { margin-left:60px; }
    strong { font-weight:bold; }
    </style>
    <div class="section">
        <header id="page-header">
            <div class="inner">
                <section id="content-search">
                    <h1>FieldData Web Services</h1>
                    <p>Following are a list of ALA FieldData web services.</p>
                </section>
            </div><!--inner-->
        </header>
        <div class="inner">
            <p>
                These web services provide field data capture services<br/>
                Please send any bug reports, suggestions for improvements or new services to:
                <strong>developers 'AT' ala.org.au</strong>
            </p>
            <h3>Records</h3>
            <ul class="webserviceList">
                <li>
                    <strong>Get record</strong>
                    - HTTP GET on /record/&lt;ID&gt; e.g /record/4fbda572300435cfcea321ab
                </li>
                <li>
                    <strong>Create record</strong>
                    - HTTP POST with JSON body to "/record/". The URL for the created record will be in the
                    "Content-Location" http header in the response.
                    The JSON body can support any additional key value pairs which will be persisted with the record.
                    <br/>
                    Example body:
                    <p class="code">
                    {
                    "eventDate":"2001-01-01",
                    "decimalLatitude":13.2,
                    "decimalLongitude":143.2,
                    "userId":"david.martin@csiro.au",
                    "scientificName":"Aus bus",
                    "commonName":"Feret",
                    "collector":"Aussie Bob",
                    "associatedMedia":"http://bie.ala.org.au/repo/1013/128/1280064/thumbnail.jpg"
                    }
                    </p>

                    For multiple images, use a JSON array e.g.
                    <p class="code">
                    { "eventDate":"2001-01-01", "decimalLatitude":13.2, "decimalLongitude":143.2, "userId":"david.martin@csiro.au", "scientificName":"Aus bus", "commonName":"Feret", "collector":"Aussie Bob", "associatedMedia":["http://bie.ala.org.au/repo/1013/128/1280064/thumbnail.jpg","http://bie.ala.org.au/repo/1013/128/1280064/thumbnail.jpg"] }
                    </p>
                </li>
                <li>
                    <strong>Update record</strong>
                    - HTTP POST with JSON body to "/record/&lt;ID&gt;". The URL for the updated record will be in
                    the "Content-Location" http header in the response.
                    The JSON body can support any additional key value pairs which will be persisted with the record.
                    <br/>
                    Example body:
                    <p class="code">
                        {
                        "id": "34223432432432423"
                        "eventDate":"2001-01-01",
                        "decimalLatitude":13.2,
                        "decimalLongitude":143.2,
                        "userId":"david.martin@csiro.au",
                        "scientificName":"Aus bus",
                        "commonName":"Feret",
                        "collector":"Aussie Bob",
                        "associatedMedia":"http://bie.ala.org.au/repo/1013/128/1280064/thumbnail.jpg"
                        }
                    </p>
                </li>
                <li>
                    <strong>Delete record</strong>
                    - HTTP DELETE with URL of the form /record/&lt;ID&gt;
                </li>
                <li>
                    <strong>List records</strong>
                    - HTTP GET on the URL <a href="record">/record</a>
                    <ul class="paramsList">
                        <li>sort  - the property to sort results by. Default is dateCreated</li>
                        <li>order - "asc" or "desc"</li>
                        <li>start - offset to use for paging. default is 0</li>
                        <li>pageSize - Page size to use. default is 30</li>
                    </ul>
                </li>
                <li>
                    <strong>List records for user</strong>
                    - HTTP GET on the URL /record/user/&lt;userID&gt;
                    <ul class="paramsList">
                        <li>sort  - the property to sort results by. Default is dateCreated</li>
                        <li>order - "asc" or "desc"</li>
                        <li>start - offset to use for paging. default is 0</li>
                        <li>pageSize - Page size to use. default is 30</li>
                    </ul>
                </li>
            </ul>

            <h3>Images</h3>
            <ul class="webserviceList">
                <li>
                    <strong>List records with images</strong>
                    - HTTP GET on the URL /images/
                    <ul class="paramsList">
                        <li>sort  - the property to sort results by. Default is dateCreated</li>
                        <li>order - "asc" or "desc"</li>
                        <li>start - offset to use for paging. default is 0</li>
                        <li>pageSize - Page size to use. default is 30</li>
                    </ul>
                </li>
             </ul>

            <h3>Bookmarked locations</h3>
            <ul class="webserviceList">
                <li>
                    <strong>Add location</strong>
                    - HTTP POST with JSON body to /location.
                    Note the <strong>UserId</strong> is mandatory.
                The JSON body can support any additional key value pairs, and these will be persisted with the record
                and returned in /location/&lt;ID&gt; requests.
                    <p class="code">
                        {
                        "decimalLatitude":13.2,
                        "decimalLongitude":143.2,
                        "userId":"david.martin@csiro.au",
                        "locality":"My house",
                        }
                    </p>
                </li>
                <li>
                    <strong>List for user</strong>
                    - HTTP GET /location/user
                    <ul class="paramsList">
                        <li>sort  - the property to sort results by. Default is dateCreated</li>
                        <li>order - "asc" or "desc"</li>
                        <li>start - offset to use for paging. default is 0</li>
                        <li>pageSize - Page size to use. default is 30</li>
                    </ul>
                </li>
                <li>
                    <strong>Delete</strong>
                    - HTTP DELETE on /location/&lt;ID&gt;
                </li>
                <li>
                    <strong>Delete All locations for user</strong>
                    - HTTP DELETE /location/user
                </li>
            </ul>
        </div>
    </div>
</div>
</body>
</html>
