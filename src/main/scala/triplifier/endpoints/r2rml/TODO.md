IDEA: 
+ extract the SQL from SQL views in the mappings (in case a table is directly mapped, we could extract a generic `SELECT * FROM ...` query)
+ expose the query by a custom endpoint
+ expose an analytics summary from the query:
	- datatype guessing, by heuristics
	- distinct values, by column
	- candidate keys: all the columns which has the same distinct as the total available rows of a dataset
	- more...
