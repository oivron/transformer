<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE stylesheet [
<!ENTITY UPPERCASE "ABCDEFGHIJKLMNOPQRSTUVWXYZ">
<!ENTITY LOWERCASE "abcdefghijklmnopqrstuvwxyz">
<!ENTITY UPPER_TO_LOWER " '&UPPERCASE;' , '&LOWERCASE;' ">
<!ENTITY LOWER_TO_UPPER " '&LOWERCASE;' , '&UPPERCASE;' ">
]>
<!--<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" xmlns:xhtml="http://www.w3.org/1999/xhtml" exclude-result-prefixes="xhtml">

<xsl:output method="xhtml" version="1.0" doctype-public="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" encoding="UTF-8" indent="yes"/>

	<!-- Pr. kanal, sortert på tid og med inndeling av dagen i formiddag, ettermiddag, kveld, natt. Utvalg 
	av kanaler (UPC grunnpakke). Pga. at det ikke er mulig å navigere på listeelementer i Daisy 2, har jeg 
	gått bort fra det. Bruker i stedet <h4> på programtittel og klokkeslett, mens programbeskrivelsen blir en <p>. 
	Der hvor et program består av flere programmer (eks. Distriktsnyheter), bruker jeg en <ul> i stedet for <p>. -->

	<!-- Skriver overskriftene i dokumentet og beregner dag, dato og måned for den første dagen i RadioGuiden. -->
	<xsl:template match="Workbook">
		<xsl:param name="date-time" select="Worksheet/Table/Row/Cell[1]"/>
		<xsl:param name="date" select="substring-before($date-time,'T')"/>
		<xsl:param name="month" select="substring-before(substring-after($date,'-'),'-')"/>
		<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="no">
			<head>
				<title>Radio guiden i Daisy format - inndelt etter kanal</title>
				<link href="tvguide.css" rel="stylesheet" type="text/css" media="all"/>
			</head>
			<body>
				<!-- Overskrift med dag, dato og måned. -->
				<h1>Radio guiden i Daisy format for uken som starter
					<xsl:variable name="day-of-the-week">
						<xsl:call-template name="calculate-day-of-the-week">
							<xsl:with-param name="date-time" select="$date-time"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:call-template name="get-day-of-the-week-name-case">
						<xsl:with-param name="day-of-the-week" select="$day-of-the-week"/>
					</xsl:call-template>
					<xsl:text> </xsl:text>
					<xsl:value-of select="substring(Worksheet/Table/Row/Cell, 9, 2)"/>.
					<xsl:call-template name="get-month-name">
						<xsl:with-param name="month" select="$month"/>
					</xsl:call-template>&#x2013;
					  inndelt etter kanal</h1>
				<h1>Om Radio guiden</h1>
				<p>Radio guiden er tilrettelagt av Norsk lyd og blindeskriftsbibliotek. Radio guiden er i Daisy 2 0 2 
				fulltekstformat med syntetisk tale. Lydboken må ikke videredistribueres. CD med Radio guide samt 
				medfølgende konvolutt skal ikke returneres til NLB. CD kan kastes sammen med øvrig husholdningsavfall.</p>
				<p>Vi er takknemlig for kommentarer og tilbakemeldinger, fortrinnsvis på e-post: lydavis@nlb.no.</p>
				<h1>Hvordan benytte Radio guiden</h1>
				<p>Radio-guiden gir deg programbildet for 7 Radio-kanaler. Du kan forflytte deg på 4 navigasjonsnivåer med din 
				Daisy spiller.</p>
				<p>På nivå 1 navigerer du deg på ukedag. Radio guiden inneholder programmet for perioden tirsdag til og med 
				neste tirsdag. På nivå 2 kan du navigere på Radio kanal. På nivå 3 kan du navigere på en tidsmessig del av 
				dagen. Dagen er delt inn i formiddag, ettermiddag, kveld og natt. På nivå 4 finner du programbeskrivelse. 
				Her får du informasjon om oppstartstidspunkt, programtittel, Radio-kanal samt en beskrivelse av programmet. 
				Episodenummer blir angitt i teksten og fremstår som et nummer i parentes.</p>
				<xsl:apply-templates select="/Workbook" mode="time"/>
			</body>
		</html>
	</xsl:template>

	<!-- I feeden ligger alle dagene i hver sin Worksheet. Denne lager overskrift for hver av dagene i RadioGuiden. -->
	<xsl:template match="Workbook" mode="time">
		<xsl:for-each select="Worksheet">
			<h1>
				<xsl:apply-templates select="Table" mode="time"/>
			</h1>
			<xsl:apply-templates select="Table"/>
		</xsl:for-each>
	</xsl:template>

	<!-- Beregner dag, dato og måned for hver av dagene i RadioGuiden. -->
	<xsl:template match="Table" mode="time">
		<xsl:param name="date-time" select="Row/Cell[1]"/>
		<xsl:param name="date" select="substring-before($date-time,'T')"/>
		<xsl:param name="month" select="substring-before(substring-after($date,'-'),'-')"/>

		<xsl:variable name="day-of-the-week">
			<xsl:call-template name="calculate-day-of-the-week">
				<xsl:with-param name="date-time" select="$date-time"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:call-template name="get-day-of-the-week-name">
			<xsl:with-param name="day-of-the-week" select="$day-of-the-week"/>
		</xsl:call-template>
		<xsl:text> </xsl:text>
		<xsl:value-of select="substring(Row/Cell, 9, 2)"/>.
		<xsl:call-template name="get-month-name">
			<xsl:with-param name="month" select="$month"/>
		</xsl:call-template>
	</xsl:template>

	<!-- Beregner navn på dag som står etter kanalnavnene -->
	<xsl:template match="Table" mode="day">
		<xsl:param name="date-time" select="Row/Cell[1]"/>
		<xsl:param name="date" select="substring-before($date-time,'T')"/>

		<xsl:variable name="day-of-the-week">
			<xsl:call-template name="calculate-day-of-the-week">
				<xsl:with-param name="date-time" select="$date-time"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:call-template name="get-day-of-the-week-name-case">
			<xsl:with-param name="day-of-the-week" select="$day-of-the-week"/>
		</xsl:call-template>
	</xsl:template>

	<!-- For hver kanal gjør jeg 4 utvalg der jeg sjekker på kanal og et tidsrom på dagen. Vær oppmerksom på at 
	start- og stopptidspunkt for døgnet varierer for de ulike kanalene. Noen radiokanaler går fra 05.00 til 05.00, 
	mens andre går fra 06.00 til 06.00. Derfor varierer select-statementet for de ulike kanalene under. -->
	<xsl:template match="Worksheet/Table">
		<xsl:if test="Row/Cell[2]='p1'">
			<h2>P1: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='p1'][Cell[3]&lt;12][Cell[3]&gt;=06]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='p1'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='p1'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='p1'][Cell[3]&lt;06]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='p2'">
			<h2>P2: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='p2'][Cell[3]&lt;12][Cell[3]&gt;=06]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='p2'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='p2'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='p2'][Cell[3]&lt;06]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='p3'">
			<h2>P3: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='p3'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='p3'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='p3'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='p3'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='nrkalltnyh'">
			<h2>NRK Alltid Nyheter: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrkalltnyh'][Cell[3]&lt;12][Cell[3]&gt;=06]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrkalltnyh'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrkalltnyh'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrkalltnyh'][Cell[3]&lt;06]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='nrkklass'">
			<h2>NRK Klassisk: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrkklass'][Cell[3]&lt;12][Cell[3]&gt;=06]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrkklass'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrkklass'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrkklass'][Cell[3]&lt;06]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='p4'">
			<h2>P4: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='p4'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='p4'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='p4'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='p4'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='kanal4'">
			<h2>Radio Norge: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='kanal4'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='kanal4'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='kanal4'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='kanal4'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="Row" mode="prog">
		<h4>
			<xsl:apply-templates select="." mode="progtittel"/>
		</h4>
		<p>
			<xsl:apply-templates select="." mode="progkategori"/>
		</p>
		<p>
			<xsl:apply-templates select="." mode="progbeskrivelse"/>
		</p>
	</xsl:template>

	<!-- Klokkeslett og programtittel for et program. -->
	<xsl:template match="Row" mode="progtittel">Kl <xsl:value-of select="Cell[3]"/>, <xsl:value-of select="Cell[11]"/>
		<xsl:apply-templates select="." mode="reprise"/>
	</xsl:template>

	<!-- Celle 6 inneholder "REPRISE" hvis programmet er sendt tidligere. -->
	<xsl:template match="Row" mode="reprise">
		<xsl:if test="Cell[8]!=' '">
			<xsl:text> (reprise)</xsl:text>
		</xsl:if>
	</xsl:template>

	<!-- Programkategorier for et program. Gjør om kategorinavnene slik at det blir stor forbokstav og resten små. -->
	<xsl:template match="Row" mode="progkategori">
		<!-- Behandler hovedkategori som ligger i Celle 5. -->
		<xsl:variable name="catFirst">
			<xsl:value-of select="substring(Cell[5], 1, 1)"/>
		</xsl:variable>
		<xsl:variable name="catRestCaps">
			<xsl:value-of select="substring(Cell[5], 2, string-length())"/>
		</xsl:variable>
		<xsl:variable name="catRest">
			<xsl:value-of select="translate($catRestCaps, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' , 'abcdefghijklmnopqrstuvwxyz' )"/>
		</xsl:variable>
		<xsl:value-of select="concat($catFirst, $catRest)"/>
		<!-- Setter punktum hvis det ikke kommer flere kategorier i de neste cellene. -->
		<xsl:if test="self::*[Cell[6]=' '][Cell[7]=' ']">
			<xsl:text>.</xsl:text>
		</xsl:if>

		<!-- Behandler underkategori som ligger i Celle 6. -->
		<xsl:if test="self::*[Cell[6]!=' ']">
			<xsl:variable name="secondCatFirst">
				<xsl:value-of select="substring(Cell[6], 1, 1)"/>
			</xsl:variable>
			<xsl:variable name="secondCatRestCaps">
				<xsl:value-of select="substring(Cell[6], 2, string-length())"/>
			</xsl:variable>
			<xsl:variable name="secondCatRest">
				<xsl:value-of select="translate($secondCatRestCaps, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' , 'abcdefghijklmnopqrstuvwxyz' )"/>
			</xsl:variable>
			<xsl:text>, </xsl:text>
			<xsl:value-of select="concat($secondCatFirst, $secondCatRest)"/>
			<!-- Setter punktum hvis det ikke det ikke er noen kategori i celle 7. -->
			<xsl:if test="self::*[Cell[7]=' ']">
				<xsl:text>.</xsl:text>
			</xsl:if>
		</xsl:if>

		<!-- Behandler underkategori som ligger i Celle 7. -->
		<xsl:if test="self::*[Cell[7]!=' ']">
			<xsl:variable name="thirdCatFirst">
				<xsl:value-of select="substring(Cell[7], 1, 1)"/>
			</xsl:variable>
			<xsl:variable name="thirdCatRestCaps">
				<xsl:value-of select="substring(Cell[7], 2, string-length())"/>
			</xsl:variable>
			<xsl:variable name="thirdCatRest">
				<xsl:value-of select="translate($thirdCatRestCaps, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' , 'abcdefghijklmnopqrstuvwxyz' )"/>
			</xsl:variable>
			<xsl:text>, </xsl:text>
			<xsl:value-of select="concat($thirdCatFirst, $thirdCatRest)"/>
			<xsl:text>.</xsl:text>
		</xsl:if>
	</xsl:template>

	<!-- Programbeskrivelse for et program. -->
	<xsl:template match="Row" mode="progbeskrivelse">
		<xsl:value-of select="Cell[12]"/>
	</xsl:template>

	<!-- Beregner dagnummer. -->
	<xsl:template name="calculate-day-of-the-week">
		<xsl:param name="date-time"/>
		<xsl:param name="date" select="substring-before($date-time,'T')"/>
		<xsl:param name="year" select="substring-before($date,'-')"/>
		<xsl:param name="month" select="substring-before(substring-after($date,'-'),'-')"/>
		<xsl:param name="day" select="substring-after(substring-after($date,'-'),'-')"/>

		<xsl:variable name="a" select="floor((14 - $month) div 12)"/>
		<xsl:variable name="y" select="$year - $a"/>
		<xsl:variable name="m" select="$month + 12 * $a - 2"/>

		<xsl:value-of select="($day + $y + floor($y div 4) - floor($y div 100) + floor($y div 400) + floor((31 * $m) div 12)) mod 7"/>
	</xsl:template>

	<!-- Beregner navn på dag på grunnlag av dagnummer (stor forbokstav). -->
	<xsl:template name="get-day-of-the-week-name">
		<xsl:param name="day-of-the-week"/>
		<xsl:choose>
			<xsl:when test="$day-of-the-week = 0">Søndag</xsl:when>
			<xsl:when test="$day-of-the-week = 1">Mandag</xsl:when>
			<xsl:when test="$day-of-the-week = 2">Tirsdag</xsl:when>
			<xsl:when test="$day-of-the-week = 3">Onsdag</xsl:when>
			<xsl:when test="$day-of-the-week = 4">Torsdag</xsl:when>
			<xsl:when test="$day-of-the-week = 5">Fredag</xsl:when>
			<xsl:when test="$day-of-the-week = 6">Lørdag</xsl:when>
			<xsl:otherwise>error: <xsl:value-of select="$day-of-the-week"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Beregner navn på dag på grunnlag av dagnummer (liten forbokstav). -->
	<xsl:template name="get-day-of-the-week-name-case">
		<xsl:param name="day-of-the-week"/>
		<xsl:choose>
			<xsl:when test="$day-of-the-week = 0">søndag</xsl:when>
			<xsl:when test="$day-of-the-week = 1">mandag</xsl:when>
			<xsl:when test="$day-of-the-week = 2">tirsdag</xsl:when>
			<xsl:when test="$day-of-the-week = 3">onsdag</xsl:when>
			<xsl:when test="$day-of-the-week = 4">torsdag</xsl:when>
			<xsl:when test="$day-of-the-week = 5">fredag</xsl:when>
			<xsl:when test="$day-of-the-week = 6">lørdag</xsl:when>
			<xsl:otherwise>error: <xsl:value-of select="$day-of-the-week"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Beregner navn på måned. -->
	<xsl:template name="get-month-name">
		<xsl:param name="date-time"/>
		<xsl:param name="date" select="substring-before($date-time,'T')"/>
		<xsl:param name="month" select="substring-before(substring-after($date,'-'),'-')"/>

		<xsl:choose>
			<xsl:when test="$month = 1">januar</xsl:when>
			<xsl:when test="$month = 2">februar</xsl:when>
			<xsl:when test="$month = 3">mars</xsl:when>
			<xsl:when test="$month = 4">april</xsl:when>
			<xsl:when test="$month = 5">mai</xsl:when>
			<xsl:when test="$month = 6">juni</xsl:when>
			<xsl:when test="$month = 7">juli</xsl:when>
			<xsl:when test="$month = 8">august</xsl:when>
			<xsl:when test="$month = 9">september</xsl:when>
			<xsl:when test="$month = 10">oktober</xsl:when>
			<xsl:when test="$month = 11">november</xsl:when>
			<xsl:when test="$month = 12">desember</xsl:when>
			<xsl:otherwise>error: <xsl:value-of select="$month"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c) 2004-2007. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->