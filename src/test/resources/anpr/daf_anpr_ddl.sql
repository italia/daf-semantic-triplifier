
/* 
CHECK
DETACH DATABASE `gove__amministrazione` 
;
*/

ATTACH DATABASE `gove__amministrazione` AS `gove__amministrazione`
;


/*
 * NOTA: per TEST LOCALE!
 */
DROP TABLE IF EXISTS `gove__amministrazione.default_org_o_agenziaentrate_elenco_comuni` 
;
CREATE TABLE `gove__amministrazione.default_org_o_agenziaentrate_elenco_comuni` (
	codice_nazionale STRING(32767),
	sigla_provincia STRING(32767),
	denominazione_italiana STRING(32767),
	denominazione_estera STRING(32767),
	codice_catastale STRING(32767),
	ufficio_catasto_terreni STRING(32767),
	ufficio_catasto_fabbricati STRING(32767),
	codice_conservatoria STRING(32767),
	codice_istat STRING(32767),
	data_costituzione STRING(32767),
	attesa_vct_territorio STRING(32767),
	attesa_vct_fabbricati STRING(32767),
	processing_dttm STRING(32767)
) ;

DROP TABLE IF EXISTS `gove__amministrazione.default_org_o_anpr_archivio_storico_comuni` 
;
CREATE TABLE `gove__amministrazione.default_org_o_anpr_archivio_storico_comuni` (
	id INT,
	dataistituzione STRING(32767),
	datacessazione STRING(32767),
	codistat STRING(32767),
	codcatastale STRING(32767),
	denominazione_it STRING(32767),
	denomtraslitterata STRING(32767),
	altradenominazione STRING(32767),
	altradenomtraslitterata STRING(32767),
	idprovincia INT,
	idregione INT,
	idprefettura STRING(32767),
	stato STRING(32767),
	siglaprovincia STRING(32767),
	fonte STRING(32767),
	dataultimoagg STRING(32767),
	cod_denom STRING(32767),
	processing_dttm STRING(32767)
) ;

DROP TABLE IF EXISTS `gove__amministrazione.default_org_o_anpr_elenco_stati_esteri` 
;
CREATE TABLE `gove__amministrazione.default_org_o_anpr_elenco_stati_esteri` (
	id INT,
	denominazione STRING(32767),
	denominazioneistat STRING(32767),
	denominazioneistat_en STRING(32767),
	datainiziovalidita STRING(32767),
	datafinevalidita STRING(32767),
	codiso3166_1_alpha3 STRING(32767),
	codmae STRING(32767),
	codmin STRING(32767),
	codat STRING(32767),
	codistat STRING(32767),
	cittadinanza STRING(32767),
	nascita STRING(32767),
	residenza STRING(32767),
	fonte STRING(32767),
	tipo STRING(32767),
	codisosovrano STRING(32767),
	dataultimoagg STRING(32767),
	processing_dttm STRING(32767)
) ;

DROP TABLE IF EXISTS `gove__amministrazione.default_org_o_istat_elenco_comuni_italiani` 
;
CREATE TABLE `gove__amministrazione.default_org_o_istat_elenco_comuni_italiani` (
	codice_regione STRING(32767),
	codice_citta_metropolitana STRING(32767),
	codice_provincia STRING(32767),
	progressivo_del_comune STRING(32767),
	codice_comune_formato_alfanumerico STRING(32767),
	denominazione_corrente STRING(32767),
	denominazione_altra_lingua STRING(32767),
	codice_ripartizione_geografica STRING(32767),
	ripartizione_geografica STRING(32767),
	denominazione_regione STRING(32767),
	denominazione_citta_metropolitana STRING(32767),
	denominazione_provincia STRING(32767),
	flag_comune_capoluogo_di_provincia STRING(32767),
	sigla_automobilistica STRING(32767),
	codice_comune_formato_numerico STRING(32767),
	codice_comune_numerico_con_110_province_dal_2010_al_2016 STRING(32767),
	codice_comune_numerico_con_107_province_dal_2006_al_2009 STRING(32767),
	codice_comune_numerico_con_103_province_dal_1995_al_2005 STRING(32767),
	codice_catastale_del_comune STRING(32767),
	popolazione_legale_2011 BIGINT,
	codice_nuts1_2010 STRING(32767),
	codice_nuts2_2010 STRING(32767),
	codice_nuts3_2010 STRING(32767),
	codice_nuts1_2006 STRING(32767),
	codice_nuts2_2006 STRING(32767),
	codice_nuts3_2006 STRING(32767),
	processing_dttm STRING(32767)
) ;

/*
DROP TABLE IF EXISTS `gove__amministrazione.default_org_o_test_istat_elenco_comuni` 
;
CREATE TABLE `gove__amministrazione.default_org_o_test_istat_elenco_comuni` (
	codice_regione STRING(32767),
	codice_citta_metropolitana STRING(32767),
	codice_provincia STRING(32767),
	progressivo_del_comune STRING(32767),
	codice_comune_formato_alfanumerico STRING(32767),
	denominazione_corrente STRING(32767),
	denominazione_altra_lingua STRING(32767),
	codice_ripartizione_geografica STRING(32767),
	ripartizione_geografica STRING(32767),
	denominazione_regione STRING(32767),
	denominazione_citta_metropolitana STRING(32767),
	denominazione_provincia STRING(32767),
	flag_comune_capoluogo_di_provincia STRING(32767),
	sigla_automobilistica STRING(32767),
	codice_comune_formato_numerico STRING(32767),
	codice_comune_numerico_con_110_province_dal_2010_al_2016 STRING(32767),
	codice_comune_numerico_con_107_province_dal_2006_al_2009 STRING(32767),
	codice_comune_numerico_con_103_province_dal_1995_al_2005 STRING(32767),
	codice_catastale_del_comune STRING(32767),
	popolazione_legale_2011 BIGINT,
	codice_nuts1_2010 STRING(32767),
	codice_nuts2_2010 STRING(32767),
	codice_nuts3_2010 STRING(32767),
	codice_nuts1_2006 STRING(32767),
	codice_nuts2_2006 STRING(32767),
	codice_nuts3_2006 STRING(32767),
	processing_dttm STRING(32767)
) ;
*/