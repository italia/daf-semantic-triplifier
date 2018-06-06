
RDF triplifier
=========================

[README last update: 2018-06-06] *pre-alpha* version

This component provides a simple microservice for creating an RDF representation of data from a JDBC connector.

The RDF processor used is [Ontop](https://ontop.inf.unibz.it/), which implements the W3C standard [`R2RML`](https://www.w3.org/TR/r2rml/) language for *tabular to RDF conversion*.

**NOTE** (Impala)

the `ssl_impala` folder should be created under the root folder of the project, and should contain the following files:

```bash
├───ssl_impala
│       jssecacerts
│       master-impala.jks
│       master-impala.pem
```

* * *

## stateless endpoint

A simple (stateless) version of endpoint for executing the R2RML mapping process can be used as follow:


```bash
curl -X POST 'http://localhost:7777/kb/api/v1/triplify/process' \
	-H "accept: text/plain" \
	-H "content-type: application/x-www-form-urlencoded" \
	--data-binary "config=${config}" \
	--data-binary "r2rml=${r2rml}" \
	-d 'format=text/turtle'
```

**NOTE** that this version of the service expects the actual content of the mapping, so when using curl it's best to prepare it
using a shell variable such as ```r2rml=`cat r2rml_file` ``` before launching curl. 

The directory `/script` contains some example, which can be extended.


otherwise we can test the endpoint by using the example page `http://localhost:7777/static/r2rml.html`:

![http_rdf_processor](./docs/img/http_rdf_processor.png)

* * *

**SEE ALSO**: [daf-semantics](https://github.com/italia/daf-semantics) project

