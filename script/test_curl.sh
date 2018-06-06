#!/bin/bash

config_sqlite=`cat test_config_sqlite.conf`
# echo -e "\n> using CONFIG:\n${config_sqlite}\n\n"

r2rml_sqlite=`cat test_regioni.sqlite.r2rml`
# echo -e "\n> using R2RML:\n${r2rml_sqlite}\n\n"

curl -X POST 'http://localhost:7777/kb/api/v1/triplify/process' -H "accept: text/plain" -H "content-type: application/x-www-form-urlencoded" --data-binary "config=${config_sqlite}" --data-binary "r2rml=${r2rml_sqlite}" -d 'format=text/turtle'


######################################################

config_impala=`cat test_config_sqlite.conf`
# echo -e "\n> using CONFIG:\n${config_impala}\n\n"

r2rml_impala=`cat test_regioni.r2rml`
# echo -e "\n> using R2RML:\n${r2rml_impala}\n\n"

curl -X POST 'http://localhost:7777/kb/api/v1/triplify/process' -H "accept: text/plain" -H "content-type: application/x-www-form-urlencoded" --data-binary "config=${config_impala}" --data-binary "r2rml=${r2rml_impala}" -d 'format=text/turtle'	
