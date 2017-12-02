#### Latest Changes ####
* 2.0.0
    * added "queries file" support analogical to "headers file". 
    * added ability to put headers in separate file. If both "headers file" and "headers" are present in config for request "headers" will have priority.
    * ability to put request in separate file, by putting filepath to requests array (instead of full object)
    * failure to start server will now throw exception instead of returning null
    * If `port` is not specified in the config file ports in inclusive range 8099-8104 will be tried 
* 1.4.2 
    * When returning code 404 also log what path was requested. 
* 1.4.1
    * Fix invalid response returned when ```NONE``` ```NetworkType``` was used.
* 1.4.0
    * If folder is requested and it contains index.html it will be returned instead of 404.
    * ConfigReader requires now implementing isFolder method.
